import 'dart:convert';
import 'package:forsythia/models/programs/program_add.dart';
import 'package:forsythia/models/programs/program_detail.dart';
import 'package:forsythia/models/programs/program_list.dart';
import 'package:forsythia/models/programs/program_res.dart';
import 'package:forsythia/service/secure_storage_service.dart';
import 'package:http/http.dart' as http;

class ProgramService {
  static const String baseUrl = 'https://api.gaenari.kr/api/program-service/';
  static final SecureStorageService secureStorageService =
      SecureStorageService();

  // 운동프로그램 상세조회
  static Future<ProgramDetail> fetchProgramDetail(number) async {
    return fetchGetData('program/$number')
        .then((data) => ProgramDetail.fromJson(data));
  }

  // 운동프로그램 리스트 불러오기
  static Future<ProgramList> fetchProgramList() async {
    return fetchGetData('program').then((data) => ProgramList.fromJson(data));
  }

  // 운동프로그램 삭제하기
  static Future<ProgramRes> fetchDeleteProgram(id) async {
    return fetchDeleteData('program/$id')
        .then((data) => ProgramRes.fromJson(data));
  }

  // 프로그램 추가하기
  static Future<dynamic> fetchProgramAdd(ProgramAdd program) async {
    String? token = await secureStorageService.getToken();
    final response = await http.post(
      Uri.parse('$baseUrl/program'),
      headers: {
        'Content-Type': 'application/json',
        'authorization': 'Bearer $token'
      },
      body: json.encode(program.toJson()),
    );

    if (response.statusCode == 200) {
      final dynamic data = json.decode(utf8.decode(response.bodyBytes));
      if (data['status'] == "SUCCESS") {
        return data;
      } else {
        throw Exception('내잘못');
      }
    } else {
      throw Exception('백잘못 ${response.statusCode}');
    }
  }

  // get요청
  static Future<dynamic> fetchGetData(String endpoint) async {
    String? token = await secureStorageService.getToken();
    final response = await http.get(
      Uri.parse('$baseUrl/$endpoint'),
      headers: {
        'Content-Type': 'application/json',
        'authorization': 'Bearer $token'
      },
    );

    if (response.statusCode == 200) {
      final dynamic data = json.decode(utf8.decode(response.bodyBytes));
      if (data['status'] == "SUCCESS") {
        return data;
      } else {
        throw Exception('Failed to load data');
      }
    } else {
      throw Exception('Failed to load data');
    }
  }

  // delete요청
  static Future<dynamic> fetchDeleteData(String endpoint) async {
    String? token = await secureStorageService.getToken();
    final response = await http.delete(
      Uri.parse('$baseUrl/$endpoint'),
      headers: {
        'Content-Type': 'application/json',
        'authorization': 'Bearer $token'
      },
    );

    if (response.statusCode == 200) {
      final dynamic data = json.decode(utf8.decode(response.bodyBytes));
      if (data['status'] == "SUCCESS") {
        return data;
      } else {
        throw Exception('Failed to load data');
      }
    } else {
      throw Exception('Failed to load data');
    }
  }
}
