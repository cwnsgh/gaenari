import 'package:flutter/material.dart';
import 'package:forsythia/models/users/LoginForm.dart';
import 'package:forsythia/models/users/LoginUser.dart';
import 'package:forsythia/screens/dashboard/dashboard_screen.dart';
import 'package:forsythia/screens/signup/signup_screen.dart';
import 'package:forsythia/service/user_service.dart';
import 'package:forsythia/theme/color.dart';
import 'package:forsythia/theme/text.dart';
import 'package:go_router/go_router.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => LoginScreenState();
}

class LoginScreenState extends State<LoginScreen> {
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  String _loginStatus = ''; // 로그인 상태를 나타내는 변수

  void _fetchLogin() async {
    String email = _emailController.text;
    String password = _passwordController.text;

    // login 모델 객체 생성
    LoginForm loginInfo = LoginForm(email: email, password: password);
    await UserService.fetchLogin(loginInfo).then((loginUser) {
      print(loginUser);
      setState(() {
        _loginStatus = '로그인 성공!';
      });
      context.go('/home');
    }).catchError((error) {
      print(error);
      setState(() {
        _loginStatus = '로그인 실패!';
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Image(
              image: AssetImage('assets/images/logo.png'),
              width: 200,
              height: 50,
              fit: BoxFit.cover,
            ),
            Padding(
              padding: const EdgeInsets.all(40),
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
                      Text16(text: '  이메일')
                    ],
                  ),
                  TextField(
                    controller: _emailController,
                    decoration: InputDecoration(
                        hintText: '이메일을 입력해주세요.',
                        hintStyle: TextStyle(color: Colors.grey),
                        focusedBorder: UnderlineInputBorder(
                            borderSide: BorderSide(color: myBlack))),
                  ),
                  SizedBox(height: 30),
                  Row(
                    children: const [
                      Image(
                        image: AssetImage('assets/emoji/pensil.png'),
                        width: 20,
                        height: 20,
                        fit: BoxFit.cover,
                      ),
                      Text16(text: '  비밀번호')
                    ],
                  ),
                  TextField(
                    controller: _passwordController,
                    decoration: InputDecoration(
                        hintText: '비밀번호를 입력해주세요.',
                        hintStyle: TextStyle(color: Colors.grey),
                        focusedBorder: UnderlineInputBorder(
                            borderSide: BorderSide(color: myBlack))),
                  ),
                ],
              ),
            ),
            Text(
              _loginStatus,
              style: TextStyle(
                fontSize: 12,
                color: _loginStatus == '로그인 성공!' ? Colors.green : Colors.red,
              ),
            ),
            ElevatedButton(
              onPressed: () async {
                try {
                  _fetchLogin();
                } catch (e) {
                  // 로그인 실패 시 처리
                  setState(() {
                    _loginStatus = '로그인 실패!';
                  });
                }
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: myYellow,
                elevation: 0,
                minimumSize: Size(MediaQuery.of(context).size.width - 200, 40),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10),
                ),
              ),
              child: Text20(
                text: '로그인',
                textColor: Colors.white,
                bold: true,
              ),
            ),
            SizedBox(height: 20),
            GestureDetector(
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => singupScreen()),
                );
              },
              child: Text(
                '회원가입',
                style: TextStyle(
                  color: myGrey,
                  decoration: TextDecoration.underline,
                  decorationColor: myGrey,
                  decorationThickness: 2,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}