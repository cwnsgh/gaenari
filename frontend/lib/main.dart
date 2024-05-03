import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:forsythia/provider/footer_provider.dart';
import 'package:forsythia/provider/login_info_provider.dart';
import 'package:forsythia/provider/signup_provider.dart';
import 'package:forsythia/provider/token_provider.dart';
import 'package:forsythia/screens/dashboard/dashboard_screen.dart';
import 'package:forsythia/screens/doghouse/doghouse_screen.dart';
import 'package:forsythia/screens/login/login_screen.dart';
import 'package:forsythia/screens/login/welcome_screen.dart';
import 'package:forsythia/screens/program/program_screen.dart';
import 'package:forsythia/screens/record/record_screen.dart';
import 'package:forsythia/screens/setting/setting_screen.dart';
import 'package:forsythia/theme/color.dart';
import 'package:forsythia/widgets/footer.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized(); // Flutter 엔진과 위젯 트리 바인딩
  // await Firebase.initializeApp(
  //   options: DefaultFirebaseOptions.currentPlatform,
  // );
  // await dotenv.load(fileName: ".env"); // .env 파일 로드

  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  late final GoRouter _router;
  final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

  @override
  void initState() {
    super.initState();
    _router = GoRouter(
      debugLogDiagnostics: true,
      initialLocation: '/welcome',
      navigatorKey: navigatorKey,
      routes: [
        GoRoute(
          path: '/welcome',
          builder: (context, state) => const WelcomeScreen(),
        ),
        GoRoute(
          path: '/login',
          builder: (context, state) => const LoginScreen(),
        ),
        GoRoute(
          path: '/home',
          builder: (context, state) => const MainNavigation(),
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
        providers: [
          ChangeNotifierProvider(create: (context) => FooterProvider()),
          ChangeNotifierProvider(create: (context) => TokenProvider()),
          ChangeNotifierProvider(create: (context) => SignupProvider()),
          ChangeNotifierProvider(create: (context) => LoginInfoProvider()),
        ],
        child: MaterialApp.router(
          routerDelegate: _router.routerDelegate,
          routeInformationParser: _router.routeInformationParser,
          routeInformationProvider: _router.routeInformationProvider,
          title: '개나리',
          localizationsDelegates: const [
            GlobalMaterialLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: const [
            Locale('en', ''), // English, no country code
            Locale('ko', ''), // Korean, no country code
          ],
          theme: ThemeData(
            fontFamily: 'TheJamsil', // 사용할 폰트 패밀리 지정
            scaffoldBackgroundColor: myBackground,
            splashColor: Colors.transparent,
            highlightColor: Colors.transparent,
            // visualDensity: VisualDensity.adaptivePlatformDensity,
            // pageTransitionsTheme: PageTransitionsTheme(
            //   builders: {
            //     TargetPlatform.android: CupertinoPageTransitionsBuilder(),
            //     TargetPlatform.iOS: CupertinoPageTransitionsBuilder(),
            //   },
            // ),
          ),
          debugShowCheckedModeBanner: false,
        ));
  }
}

class MainNavigation extends StatefulWidget {
  const MainNavigation({super.key});

  @override
  State<MainNavigation> createState() => _MainNavigationState();
}

class _MainNavigationState extends State<MainNavigation> {
  int _selectedIndex = 0;

  final List<Widget> _widgetOptions = [
    DashBoardScreen(),
    RecodScreen(),
    ProgramScreen(),
    DogHouseScreen(),
    SettingScreen(),
  ];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: AnimatedIndexedStack(
        index: _selectedIndex,
        children: _widgetOptions,
      ),
      bottomNavigationBar: CustomBottomNavigationBar(
        selectedIndex: _selectedIndex,
        onItemSelected: (index) {
          _onItemTapped(index);
        },
      ),
    );
  }
}
