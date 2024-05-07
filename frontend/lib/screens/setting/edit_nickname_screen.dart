import 'package:flutter/material.dart';
import 'package:forsythia/models/users/nickname_check.dart';
import 'package:forsythia/models/users/login_user.dart';
import 'package:forsythia/provider/login_info_provider.dart';
import 'package:forsythia/service/secure_storage_service.dart';
import 'package:forsythia/theme/color.dart';
import 'package:forsythia/theme/text.dart';
import 'package:forsythia/widgets/slide_page_route.dart';
import 'package:forsythia/widgets/small_app_bar.dart';
import 'package:flutter/services.dart';
import 'package:forsythia/service/member_service.dart';
import 'package:provider/provider.dart';

class EditNickName extends StatefulWidget {
  const EditNickName({super.key});

  @override
  State<EditNickName> createState() => _EditNickNameState();
}

class _EditNickNameState extends State<EditNickName> {
  late LoginInfoProvider _loginInfoProvider;
  final TextEditingController _nicknamecontroller = TextEditingController();

  String check = "";

  bool _showErrorMessage = false;

  String _errorText = '';
  @override
  void initState() {
    super.initState();
    _errorText = '';
    _showErrorMessage = false;
    _loginInfoProvider = Provider.of<LoginInfoProvider>(context, listen: false);
  }

  @override
  Widget build(BuildContext context) {
    var nickName = Provider.of<LoginInfoProvider>(context).loginInfo?.nickname;
    print(nickName);

    return Scaffold(
      appBar: SmallAppBar(
        title: '닉네임',
        back: true,
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            SizedBox(height: 30),
            maintext,
            SizedBox(height: 30),
            _nickname(nickName),
          ],
        ),
      ),
      bottomNavigationBar: _button(),
    );
  }

  var maintext = Center(
    child: RichText(
      textAlign: TextAlign.center,
      text: TextSpan(
        children: const [
          TextSpan(
            text: '수정할 닉네임',
            style: TextStyle(
                color: myMainGreen,
                fontSize: 22,
                fontWeight: FontWeight.bold,
                fontFamily: 'TheJamsil'),
          ),
          TextSpan(
            text: '을 \n입력해주세요.',
            style: TextStyle(
                color: myBlack,
                fontSize: 22,
                fontWeight: FontWeight.bold,
                height: 1.5,
                fontFamily: 'TheJamsil'),
          ),
        ],
      ),
    ),
  );

  Widget _nickname(nickname) {
    final nickname = _loginInfoProvider.loginInfo?.nickname;

    return Padding(
      padding: const EdgeInsets.fromLTRB(30, 10, 30, 20),
      child: Column(
        children: [
          Row(
            children: const [
              Image(
                image: AssetImage('assets/emoji/pensil.png'),
                width: 20,
                height: 20,
                fit: BoxFit.cover,
              ),
              Text16(text: '  닉네임'),
            ],
          ),
          Row(
            children: [
              Expanded(
                // Expanded 위젯을 추가하여 Row의 너비를 확장합니다.
                child: TextField(
                  controller: _nicknamecontroller,
                  onChanged: (value) {
                    if (value.length < 2 && value.length <= 10) {
                      setState(() {
                        _errorText = '2자 이상 10자 이하';
                      });
                    } else {
                      setState(() {
                        _errorText = '';
                      });
                    }
                    setState(() {
                      check = ""; // 값이 변경될 때마다 check 변수를 초기화해줘
                    });
                  },
                  decoration: InputDecoration(
                    contentPadding: EdgeInsets.only(left: 5),
                    hintText: nickname,
                    hintStyle: TextStyle(color: Colors.grey),
                    // tap 시 borderline 색상 지정
                    focusedBorder: UnderlineInputBorder(
                        borderSide: BorderSide(color: myBlack)),
                    errorText: _errorText.isNotEmpty ? _errorText : null,
                  ),
                  inputFormatters: [
                    FilteringTextInputFormatter.allow(RegExp(
                        r'[a-zA-Z0-9_ㄱ-힣]')), // 영어 대소문자, 숫자, 언더바, 한글 허용 // 최대 10자까지 입력 허용
                  ],
                  maxLength: 10,
                ),
              ),
            ],
          ),
          SizedBox(
            height: 10,
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              ElevatedButton(
                  onPressed: () async {
                    final Check idCheck =
                        await MemberService.fetchNickNameCheck(
                            _nicknamecontroller.text);
                    setState(() {
                      if (idCheck.data != null && idCheck.data == false) {
                        check = "사용 가능한 닉네임";
                      } else {
                        check = "중복된 닉네임";
                      }
                    });
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: myLightYellow,
                    elevation: 0,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(10),
                    ),
                  ),
                  child: Text12(text: check == "" ? '닉네임 중복검사' : check))
            ],
          )
        ],
      ),
    );
  }

  // _fetcheditnickname() async {
  //   print(_nicknamecontroller.text);
  //   await MemberService.fetchPutData(_nicknamecontroller.text).then((data) {
  //     Navigator.of(context).push(SlidePageRoute(nextPage: EditNickName()));
  //   }).catchError((error) {
  //     print(error);
  //   });
  // }

  void _fetcheditnickname() async {
    String newNickname = _nicknamecontroller.text;

    try {
      // 닉네임 업데이트 요청 보내기
      await MemberService.fetchEditNickName(newNickname);

      // 프로바이더에 업데이트된 닉네임 반영
      LoginInfoProvider loginInfoProvider =
          Provider.of<LoginInfoProvider>(context, listen: false);
      loginInfoProvider.updateLoginInfo(
        LoginInfo(
          memberId: loginInfoProvider.loginInfo?.memberId,
          email: loginInfoProvider.loginInfo?.email,
          nickname: newNickname, // 변경된 닉네임 설정
          birthday: loginInfoProvider.loginInfo?.birthday,
          gender: loginInfoProvider.loginInfo?.gender,
          height: loginInfoProvider.loginInfo?.height,
          weight: loginInfoProvider.loginInfo?.weight,
          coin: loginInfoProvider.loginInfo?.coin,
          myPetDto: loginInfoProvider.loginInfo?.myPetDto,
        ),
      );

      // 업데이트 성공 시 EditNickName 화면으로 이동
      Navigator.of(context).push(SlidePageRoute(nextPage: EditNickName()));
    } catch (error) {
      print(error);
    }
  }

  Widget _button() {
    return Padding(
      padding: const EdgeInsets.only(bottom: 20),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (_showErrorMessage) // 상태에 따라 텍스트를 표시하거나 숨김
            Text(
              '모든 입력이 올바른지 확인해주세요.',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontFamily: 'TheJamsil',
                color: myRed,
                fontSize: 16,
              ),
            ),
          SizedBox(
            height: 10,
          ),
          ElevatedButton(
            onPressed: () {
              if (_nicknamecontroller.text.isNotEmpty &&
                  check != "" && //닉네임 체크
                  _nicknamecontroller.text.length >= 2) {
                _fetcheditnickname();
                setState(() {
                  _showErrorMessage = false; // 에러 메시지 표시
                });
              } else {
                setState(() {
                  _showErrorMessage = true; // 에러 메시지 표시
                });
              }
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: myLightGreen,
              elevation: 0,
              minimumSize: Size(MediaQuery.of(context).size.width - 50, 50),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10),
              ),
            ),
            child: Text16(
              text: '수정 완료',
            ),
          ),
        ],
      ),
    );
  }
}
