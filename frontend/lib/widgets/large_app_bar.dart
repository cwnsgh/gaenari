import 'package:flutter/material.dart';
import 'package:forsythia/screens/coin/coin_screen.dart';
import 'package:forsythia/theme/color.dart';
import 'package:forsythia/theme/text.dart';
import 'package:forsythia/widgets/slide_page_route.dart';
import 'package:intl/intl.dart';

class LargeAppBar extends StatelessWidget implements PreferredSizeWidget {
  final String title;
  final String sentence;
  final bool coin;
  final int? coinvalue;

  const LargeAppBar({
    super.key,
    required this.title,
    required this.sentence,
    this.coin = false,
    this.coinvalue = 0,
  });

  // 앱바 높이 지정
  @override
  Size get preferredSize => Size.fromHeight(178);

  @override
  Widget build(BuildContext context) {
    return AppBar(
      backgroundColor: myBackground,

      // 뒤로가기 버튼
      leading: Padding(
        padding: const EdgeInsets.all(15.0),
        child: GestureDetector(
          onTap: () async {
            Navigator.pop(context, "update"); // 이미지 클릭 시 뒤로 가기
          },
          child: Image.asset(
            "assets/icons/common_back.png",
            filterQuality: FilterQuality.none,
            fit: BoxFit.cover,
          ), // 여기에 네가 사용하는 이미지 경로 넣어줘
        ),
      ),

      // 코인
      actions: [
        GestureDetector(
          onTap: () {
            Navigator.of(context).push(SlidePageRoute(nextPage: CoinScreen()));
          },
          child: Padding(
              padding: const EdgeInsets.fromLTRB(0, 0, 20, 5),
              child: coin
                  ? Row(
                      children: [
                        Text16(
                            text: NumberFormat('#,###,###').format(coinvalue),
                            bold: true),
                        SizedBox(width: 5),
                        Image(
                          image: AssetImage('assets/color_icons/icon_coin.png'),
                          width: 18,
                          height: 18,
                          fit: BoxFit.cover,
                        ),
                      ],
                    )
                  : null),
        )
      ],

      // 앱바 이미지
      centerTitle: true,
      flexibleSpace: Stack(
        children: [
          Container(
            decoration: BoxDecoration(
              image: DecorationImage(
                image: AssetImage("assets/images/largeappbar.png"),
                fit: BoxFit.fill,
              ),
            ),
          ),

          // 텍스트
          Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                SizedBox(
                  height: 60,
                ),
                Text(
                  title,
                  style: TextStyle(
                    color: Colors.black,
                    fontSize: 30,
                    fontWeight: FontWeight.bold,
                    shadows: const <Shadow>[
                      Shadow(
                        offset: Offset(0, 5),
                        blurRadius: 20.0,
                        color: Color(0X99777777),
                      ),
                    ],
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.symmetric(
                      vertical: 10.0, horizontal: 40.0),
                  child: Text(
                    sentence,
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 14,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
