package com.example.petrunning2

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import com.example.petrunning2.data.RouteHolder
import com.example.petrunning2.ui.decoration.DecorationScreen
import com.example.petrunning2.ui.home.HomeScreen
import com.example.petrunning2.ui.login.LoginScreen
import com.example.petrunning2.ui.splash.SplashScreen
import com.example.petrunning2.ui.profile.ProfileScreen
import com.example.petrunning2.ui.result.ResultScreen
import com.example.petrunning2.ui.running.CountdownScreen
import com.example.petrunning2.ui.running.RunningScreen
import com.example.petrunning2.ui.running.RunningViewModel
import com.example.petrunning2.ui.statics.StaticsScreen
import com.example.petrunning2.ui.theme.PetRunning2Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            PetRunning2Theme {
                AppNavigation()
            }
        }
    }
}

@Composable
private fun AppNavigation() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("petrunning", android.content.Context.MODE_PRIVATE) }
    val isLoggedIn = remember { prefs.getBoolean("is_logged_in", false) }
    val startDestination = if (isLoggedIn) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

        // ── 스플래시 (비활성화 중) ──
//        composable("splash") {
//            SplashScreen(
//                onFinished = {
//                    val dest = if (isLoggedIn) "home" else "login"
//                    navController.navigate(dest) {
//                        popUpTo("splash") { inclusive = true }
//                    }
//                },
//            )
//        }

        // ── 로그인 ──
        composable("login") {
            LoginScreen(
                onGuestLogin = {
                    prefs.edit().putBoolean("is_logged_in", true).apply()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }

        // ── 홈 ──
        composable("home") {
            var permissionGranted by remember { mutableStateOf(false) }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                if (fineGranted || coarseGranted) {
                    permissionGranted = true
                }
            }

            LaunchedEffect(permissionGranted) {
                if (permissionGranted) {
                    permissionGranted = false
                    navController.navigate("countdown")
                }
            }

            HomeScreen(
                onNavigateToRunning = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        )
                    )
                },
                onNavigateToDecoration = { navController.navigate("decoration") },
                onNavigateToStats = { navController.navigate("statics") },
                onNavigateToProfile = { navController.navigate("profile") },
            )
        }

        // ── 꾸미기 ──
        composable("decoration") {
            DecorationScreen(
                onNavigateToHome = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                onNavigateToStats = { navController.navigate("statics") },
                onNavigateToProfile = { navController.navigate("profile") },
            )
        }

        // ── 통계 ──
        composable("statics") {
            StaticsScreen(
                onNavigateToHome = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                onNavigateToDecoration = { navController.navigate("decoration") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToRecordDetail = { distanceKm, elapsedSeconds, paceSecPerKm, routePoints ->
                    // RouteHolder에 좌표 저장 후 결과 화면으로 이동
                    val points = if (routePoints.isBlank()) emptyList()
                    else routePoints.split("|").mapNotNull { seg ->
                        val parts = seg.split(",")
                        if (parts.size == 2) {
                            val lat = parts[0].toDoubleOrNull()
                            val lng = parts[1].toDoubleOrNull()
                            if (lat != null && lng != null) com.example.petrunning2.ui.running.LatLngPoint(lat, lng)
                            else null
                        } else null
                    }
                    RouteHolder.set(points)
                    navController.navigate("result_history/$distanceKm/$elapsedSeconds/$paceSecPerKm")
                },
            )
        }

        // ── 프로필 ──
        composable("profile") {
            ProfileScreen(
                onNavigateToHome = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                onNavigateToDecoration = { navController.navigate("decoration") },
                onNavigateToStats = { navController.navigate("statics") },
                onLogout = {
                    prefs.edit().putBoolean("is_logged_in", false).apply()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        // ── 카운트다운 ──
        composable("countdown") {
            CountdownScreen(
                onCountdownFinished = {
                    navController.navigate("running") {
                        popUpTo("countdown") { inclusive = true }
                    }
                },
            )
        }

        // ── 러닝 ──
        composable("running") {
            val viewModel: RunningViewModel = hiltViewModel()

            LaunchedEffect(Unit) {
                viewModel.startRun()
            }

            RunningScreen(
                onNavigateToEndRun = {
                    val state = viewModel.uiState.value
                    RouteHolder.set(viewModel.getRouteSnapshot())
                    val route = "result" +
                            "/${state.distanceKm}" +
                            "/${state.elapsedSeconds}" +
                            "/${state.paceSecPerKm}"
                    navController.navigate(route) {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                viewModel = viewModel,
            )
        }

        // ── 결과 ──
        composable(
            route = "result/{distanceKm}/{elapsedSeconds}/{paceSecPerKm}",
            arguments = listOf(
                navArgument("distanceKm") { type = NavType.FloatType },
                navArgument("elapsedSeconds") { type = NavType.LongType },
                navArgument("paceSecPerKm") { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val distanceKm = backStackEntry.arguments?.getFloat("distanceKm")?.toDouble() ?: 0.0
            val elapsedSeconds = backStackEntry.arguments?.getLong("elapsedSeconds") ?: 0L
            val paceSecPerKm = backStackEntry.arguments?.getLong("paceSecPerKm") ?: 0L
            // RouteHolder에서 경로 좌표 가져오기
            val routePoints = remember {
                val points = RouteHolder.get()
                val str = points.joinToString("|") { "${it.lat},${it.lng}" }
                RouteHolder.clear()
                str
            }

            ResultScreen(
                distanceKm = distanceKm,
                elapsedSeconds = elapsedSeconds,
                paceSecPerKm = paceSecPerKm,
                routePoints = routePoints,
                onNavigateToHome = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
            )
        }

        // ── 결과 (통계에서 진입) ──
        composable(
            route = "result_history/{distanceKm}/{elapsedSeconds}/{paceSecPerKm}",
            arguments = listOf(
                navArgument("distanceKm") { type = NavType.FloatType },
                navArgument("elapsedSeconds") { type = NavType.LongType },
                navArgument("paceSecPerKm") { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val distanceKm = backStackEntry.arguments?.getFloat("distanceKm")?.toDouble() ?: 0.0
            val elapsedSeconds = backStackEntry.arguments?.getLong("elapsedSeconds") ?: 0L
            val paceSecPerKm = backStackEntry.arguments?.getLong("paceSecPerKm") ?: 0L
            val routePoints = remember {
                val points = RouteHolder.get()
                val str = points.joinToString("|") { "${it.lat},${it.lng}" }
                RouteHolder.clear()
                str
            }

            ResultScreen(
                distanceKm = distanceKm,
                elapsedSeconds = elapsedSeconds,
                paceSecPerKm = paceSecPerKm,
                routePoints = routePoints,
                isFromHistory = true,
                onNavigateToHome = {
                    navController.popBackStack()
                },
            )
        }
    }
}
