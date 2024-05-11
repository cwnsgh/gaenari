package com.example.gaenari.activity.iactivity

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.gaenari.dto.request.HeartRates
import com.example.gaenari.dto.request.IntervalInfo
import com.example.gaenari.dto.request.Program
import com.example.gaenari.dto.request.Ranges
import com.example.gaenari.dto.request.Record
import com.example.gaenari.dto.request.SaveDataRequestDto
import com.example.gaenari.dto.request.Speeds
import com.example.gaenari.dto.response.ApiResponseDto
import com.example.gaenari.dto.response.FavoriteResponseDto
import com.example.gaenari.util.AccessToken
import com.example.gaenari.util.Retrofit
import com.google.android.gms.location.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime


class IntervalService : Service(), SensorEventListener {
    private val binder = LocalBinder()
    private var isServiceRunning = false
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var sensorManager: SensorManager? = null
    private var heartRateSensor: Sensor? = null

    private var programData: FavoriteResponseDto? = null
    private lateinit var requestDto: SaveDataRequestDto

    private var lastLocation: Location? = null

    private var totalDistance = 0.0
    private var currentHeartRate = 0f // 현재 심박수
    private var oneMinuteSpeed = 0.0
    private var oneMinuteHeartRate = 0f
    private var rangeSpeed = 0.0
    private var rangeSpeedCnt = 0
    private var speedCount = 0
    private var heartRateCount = 0

    private var currentSetCount = 0
    private var currentRangeIndex = 0
    private var currentRangeTime: Long = 60000
    private var currentRunningType: Boolean = false

    private var startTime: Long = 0

    private var oneMinuteDistance = 0.0
    private var elapsedTime: Long = 0

    private var gpsUpdateIntervalMillis: Long = 2500
    private var timerIntervalMillis: Long = 1000

    // 일시정지 관련 변수
    private var totalPausedTime: Long = 0
    private var lastPauseTime: Long = 0

    private var wakeLock: PowerManager.WakeLock? = null
    private var isPaused = false

    /**
     * 타이머 핸들러를 통해 1초마다 시간을 갱신
     */
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!isPaused) {
                elapsedTime = SystemClock.elapsedRealtime() - startTime - totalPausedTime
                sendTimeBroadcast(elapsedTime)

                // 심박수 브로드캐스트
                sendHeartRateBroadcast(currentHeartRate)
            }
            timerHandler.postDelayed(this, timerIntervalMillis)
        }
    }

    /**
     * 1분 평균 계산 핸들러
     */
    private val oneMinuteHandler = Handler(Looper.getMainLooper())
    private val oneMinuteRunnable = object : Runnable {
        override fun run() {
            calculateOneMinuteAverages()
            oneMinuteHandler.postDelayed(this, 60000) // 1분 간격 실행
        }
    }

    /**
     * 구간 평균 계산 핸들러
     */
    private val rangeHandler = Handler(Looper.getMainLooper())
    private val rangeRunnable = object : Runnable {
        override fun run() {
            Log.d("Delay", "Range Delay Time Check : $currentRangeTime")
            calculateRangeAverages()
            rangeHandler.postDelayed(this, currentRangeTime)
        }
    }

    private val notification: Notification
        get() {
            val notificationIntent = Intent(this, IActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Builder(this, "ForegroundServiceChannel")
                .setContentTitle("Exercise Running")
                .setContentText("Tracking your exercise")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build()
        }

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        startTime = SystemClock.elapsedRealtime()
        if (!isServiceRunning) {
            isServiceRunning = true
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock =
                powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakeLockTag")
            wakeLock?.acquire() // WakeLock 활성화
            try {
                Log.d("IRunningService", "Service started")
                createNotificationChannel()
                startForeground(1, notification)
                setupLocationTracking()
                setupHeartRateSensor()
                startTime = SystemClock.elapsedRealtime()
                timerHandler.postDelayed(timerRunnable, 1000) // 타이머 시작
                oneMinuteHandler.postDelayed(oneMinuteRunnable, 60000) // 1분 평균 계산 타이머 시작
            } catch (e: Exception) {
                Log.e("IRunningService", "Error in onCreate: ${e.message}")
            }
        }
    }

    /**
     * saveRequestDto 초기화
     */
    private fun initRequestDto() {
        Log.d("Check", "initRequestDto() 들어옴")
        Log.d("Check", "ProgramData in InitRequestDto Method : $programData")
        val intervalInfo = IntervalInfo(ArrayList())
        val program = Program(programData?.programId!!, intervalInfo)
        val record = Record(0.0, 0.0)
        val speed = Speeds(0.0, ArrayList())
        val heartRate = HeartRates(0, ArrayList())

        requestDto = SaveDataRequestDto(
            date = LocalDateTime.now(),
            exerciseType = "P", programType = "I", program = program,
            record = record, speeds = speed, heartrates = heartRate
        )

        Log.d("Check", "Init RequestDto : $requestDto")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        programData = intent?.getParcelableExtra("programData", FavoriteResponseDto::class.java)

        initRequestDto()

        currentRangeTime =
            programData?.program?.intervalInfo?.ranges?.get(currentRangeIndex)?.time?.toLong()!! * 1000
        currentRunningType =
            programData?.program?.intervalInfo?.ranges?.get(currentRangeIndex)?.isRunning!!

        sendRangeInfoBroadcast()

        currentRangeIndex += 1

        rangeHandler.postDelayed(rangeRunnable, currentRangeTime)

        return super.onStartCommand(intent, flags, startId)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (isPaused) return
            try {
                locationResult.locations.forEach { location ->
                    val speed = location.speed.toDouble()
                    // 분 당 속도
                    oneMinuteSpeed += speed
                    speedCount++

                    // 구간 당 속도
                    rangeSpeed += speed
                    rangeSpeedCnt++

                    val distance = location.distanceTo(lastLocation ?: location).toDouble()
                    totalDistance += distance
                    oneMinuteDistance += distance
                    sendGpsBroadcast(totalDistance, elapsedTime, speed.toFloat())
                    lastLocation = location
                }
            } catch (e: Exception) {
                Log.e("IRunningService", "Error processing location update: ${e.message}")
            }
        }
    }

    /**
     * 분 당 운동 기록 계산
     */
    private fun calculateOneMinuteAverages() {
        Log.d("Check", "1분 당 평균 값 계산 시작")
        val averageSpeed = if (speedCount > 0) oneMinuteSpeed / speedCount else 0.0
        val averageHeartRate =
            if (heartRateCount > 0) (oneMinuteHeartRate / heartRateCount).toInt() else 0
        Log.d(
            "Check",
            "OneMinuteAverage Info : $averageSpeed , $averageHeartRate ,$oneMinuteDistance"
        )

        /* Update requestDto */
        requestDto.speeds.average.plus(averageSpeed)
        requestDto.speeds.addSpeed(averageSpeed)
        requestDto.heartrates.average.plus(averageHeartRate)
        requestDto.heartrates.addHeartRate(averageHeartRate)

        /* 다음 1분을 위한 초기화 */
        oneMinuteSpeed = 0.0
        speedCount = 0
        oneMinuteHeartRate = 0f
        heartRateCount = 0
        oneMinuteDistance = 0.0
    }

    /**
     * 구간 운동 기록 계산
     */
    private fun calculateRangeAverages() {
        val isRunning = currentRunningType
        val rangeTime = currentRangeTime
        val averageSpeed = if (rangeSpeedCnt > 0) rangeSpeed / rangeSpeedCnt else 0.0
        Log.d("Check", "Calculate Range Average : $isRunning, ${rangeTime}, $averageSpeed")

        /* Update requestDto Range Info */
        val range = Ranges(
            isRunning = isRunning,
            time = (rangeTime / 1000).toInt(),
            speed = averageSpeed
        )
        Log.d("Check", "Input Range Info to RequestDto : $range")
        requestDto.program.intervalInfo.addRange(range)
        Log.d("Check", "RequestDto Status : $requestDto")

        // 속도 정보 초기화
        rangeSpeed = 0.0
        rangeSpeedCnt = 0

        isEndOfProgram()
        updateNextRangeInfo()
    }

    /**
     * 인터벌 프로그램 종료조건 확인
     */
    private fun isEndOfProgram() {
        /* 모든 세트의 완료 여부 확인 */
        if (currentSetCount == programData?.program?.intervalInfo?.setCount) {
            Log.d("Check", "Interval Service Stop")
            stopService()
        }
    }

    /**
     * 다음 인터벌 구간 정보 Update
     */
    private fun updateNextRangeInfo() {
        Log.d("Check", "Before Update Range Info Index : $currentRangeIndex")

        /* 다음 구간 정보 */
        currentRangeTime =
            programData?.program?.intervalInfo?.ranges?.get(currentRangeIndex)?.time?.toLong()!! * 1000
        currentRunningType =
            programData?.program?.intervalInfo?.ranges?.get(currentRangeIndex)?.isRunning!!

        sendRangeInfoBroadcast()

        currentRangeIndex += 1

        Log.d(
            "Check",
            "Check Range Count : current($currentRangeIndex), total(${programData?.program?.intervalInfo?.rangeCount})"
        )

        /* 세트 종료 시 구간 정보 초기화 */
        if (currentRangeIndex == programData?.program?.intervalInfo?.rangeCount) {
            currentRangeIndex = 0
            currentSetCount++
        }

        Log.d(
            "Check",
            "Check Set Count : current($currentSetCount), total(${programData?.program?.intervalInfo?.setCount})"
        )
    }

    /**
     * 구간 정보 브로드 캐스트
     */
    private fun sendRangeInfoBroadcast() {
        val intent = Intent("com.example.sibal.UPDATE_RANGE_INFO").apply {
            putExtra("rangeIndex", currentRangeIndex)
            putExtra("setCount", currentSetCount)
            putExtra("isRunning", currentRunningType)
            putExtra("rangeTime", currentRangeTime)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onSensorChanged(event: SensorEvent) {
        Log.d("Check", "onSensorChanged: 님아됨 ? ㅋㅋㅋㅋ ")
        if (!isPaused && event.sensor.type == Sensor.TYPE_HEART_RATE) {
            currentHeartRate = event.values[0]
            oneMinuteHeartRate += currentHeartRate
            heartRateCount++
        }
    }

    override fun onDestroy() {
        /* 분 당 정보 누적합을 누적 개수로 나누어 전체 평균 계산 */
        requestDto.speeds.average.div(requestDto.speeds.arr.size)
        requestDto.heartrates.average.div(requestDto.heartrates.arr.size)

        /* record 정보 추가 */
        requestDto.record.distance = totalDistance
        requestDto.record.time = programData?.program?.intervalInfo?.duration!!

        wakeLock?.release()
        isServiceRunning = false
        sensorManager?.unregisterListener(this)
        fusedLocationClient?.removeLocationUpdates(locationCallback)
        timerHandler.removeCallbacks(timerRunnable)
        oneMinuteHandler.removeCallbacks(oneMinuteRunnable)
        Log.d("IRunningService", "Service destroyed")

        saveExerciseRecordData()
        sendEndProgramBroadcast()
        super.onDestroy()
    }

    /**
     * 운동 기록 저장 API 호출 및 결과 확인
     */
    private fun saveExerciseRecordData() {
        Log.d("Check", "Exercise Record Data : $requestDto")

        val call = Retrofit.getApiService()
            .saveRunningData(AccessToken.getInstance().accessToken, requestDto)

        call.enqueue(object : Callback<ApiResponseDto<String>> {
            override fun onResponse(
                call: Call<ApiResponseDto<String>>,
                response: Response<ApiResponseDto<String>>
            ) {
                if (response.body()?.status == "SUCCESS")
                    Toast.makeText(
                        this@IntervalService,
                        "운동 기록 전송 성공",
                        Toast.LENGTH_SHORT
                    ).show()
                else
                    Toast.makeText(
                        this@IntervalService,
                        response.body()?.message,
                        Toast.LENGTH_SHORT
                    ).show()
            }

            override fun onFailure(call: Call<ApiResponseDto<String>>, t: Throwable) {
                Toast.makeText(
                    this@IntervalService,
                    "API 연결 실패.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    inner class LocalBinder : Binder() {
        fun getService(): IntervalService = this@IntervalService
    }

    override fun onBind(intent: Intent): IBinder = binder

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            "ForegroundServiceChannel",
            "Exercise Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun setupLocationTracking() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, gpsUpdateIntervalMillis)
                .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun setupHeartRateSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        heartRateSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * GPS 브로드캐스트 통해 거리, 시간, 속도 전송
     */
    private fun sendGpsBroadcast(distance: Double, time: Long, speed: Float) {
        val intent = Intent("com.example.sibal.UPDATE_INFO").apply {
            putExtra("distance", distance)
            putExtra("time", time)
            putExtra("speed", speed)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * 타이머 정보를 브로드캐스트
     */
    private fun sendTimeBroadcast(time: Long) {
        val intent = Intent("com.example.sibal.UPDATE_TIMER").apply {
            putExtra("time", time)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * 심박수를 브로드캐스트하는 함수
     */
    private fun sendHeartRateBroadcast(currentHeartRate: Float) {
        val intent = Intent("com.example.sibal.UPDATE_HEART_RATE").apply {
            putExtra("heartRate", currentHeartRate)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * 인터벌 프로그램 종료 브로드 캐스트
     */
    private fun sendEndProgramBroadcast(){
        val intent = Intent("com.example.sibal.EXIT_INTERVAL_PROGRAM").apply {
            putExtra("isEnd", true)
            putExtra("totalHeartRateAvg", requestDto.heartrates.average)
            putExtra("totalSpeedAvg", requestDto.speeds.average)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun stopService() {
        stopSelf()
    }
}
