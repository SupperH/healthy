package com.example.healthmanagecenter.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.healthmanagecenter.ui.screen.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import com.example.healthmanagecenter.ui.screen.ElderHealthDetailScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object PickDoctor : Screen("pick_doctor/{userId}") {
        fun createRoute(userId: String) = "pick_doctor/$userId"
    }
    object ElderHome : Screen("elder_home/{userId}") {
        fun createRoute(userId: String) = "elder_home/$userId"
    }
    object DoctorHome : Screen("doctor_home/{userId}") {
        fun createRoute(userId: String) = "doctor_home/$userId"
    }
    object HealthRecord : Screen("health_record/{userId}") {
        fun createRoute(userId: String) = "health_record/$userId"
    }
    object DoctorFeedback : Screen("doctor_feedback/{elderId}/{healthRecordId}/{doctorId}") {
        fun createRoute(elderId: String, healthRecordId: String, doctorId: String) = 
            "doctor_feedback/$elderId/$healthRecordId/$doctorId"
    }
    object ElderFeedback : Screen("elder_feedback/{elderId}") {
        fun createRoute(elderId: String) = "elder_feedback/$elderId"
    }
    object ElderHealthDetail : Screen("elder_health_detail/{elderId}/{doctorId}") {
        fun createRoute(elderId: String, doctorId: String) = "elder_health_detail/$elderId/$doctorId"
    }
    object MedicationReminder : Screen("medication_reminder")
    object HealthTrend : Screen("health_trend/{userId}") {
        fun createRoute(userId: String) = "health_trend/$userId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { userId: Long, isDoctor: Boolean, needPickDoctor: Boolean ->
                    when {
                        needPickDoctor -> navController.navigate(Screen.PickDoctor.createRoute(userId.toString()))
                        isDoctor -> navController.navigate(Screen.DoctorHome.createRoute(userId.toString()))
                        else -> navController.navigate(Screen.ElderHome.createRoute(userId.toString()))
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { _, _ -> /* 不再自动跳转主页 */ }
            )
        }

        composable(
            route = Screen.PickDoctor.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
            PickDoctorScreen(
                userId = userId,
                onDoctorPicked = { elderId ->
                    navController.navigate(Screen.ElderHome.createRoute(elderId.toString())) {
                        popUpTo(Screen.PickDoctor.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ElderHome.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
            val viewModel: com.example.healthmanagecenter.viewmodel.LoginRegisterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val user by viewModel.getUserByIdState(userId).collectAsState()
            val doctor by viewModel.getDoctorByIdState(user?.doctorId).collectAsState()
            if (user?.role == "elder" && (user?.doctorId == null || user?.doctorId == 0L)) {
                LaunchedEffect(userId) {
                    navController.navigate(Screen.PickDoctor.createRoute(userId.toString())) {
                        popUpTo(Screen.ElderHome.route) { inclusive = true }
                    }
                }
            } else {
                ElderHomeScreen(
                    userId = userId,
                    userName = user?.name ?: "",
                    doctorName = doctor?.name ?: "",
                    doctorPhone = doctor?.phone ?: "",
                    onLogout = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) { popUpTo(0) }
                    },
                    onHealthRecords = {
                        navController.navigate("health_record/$userId")
                    },
                    onMedication = {
                        navController.navigate(Screen.MedicationReminder.route)
                    },
                    onAlerts = {
                        navController.navigate("notification/$userId")
                    },
                    onNotification = {
                        navController.navigate("notification/$userId")
                    },
                    onNavigateToElderFeedback = {
                        navController.navigate(Screen.ElderFeedback.createRoute(userId.toString()))
                    }
                )
            }
        }

        composable(
            route = Screen.DoctorHome.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
            val viewModel: com.example.healthmanagecenter.viewmodel.LoginRegisterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val doctor by viewModel.getUserByIdState(userId).collectAsState()
            val elders by viewModel.getEldersByDoctorIdState(userId).collectAsState()
            DoctorHomeScreen(
                userId = userId,
                userName = doctor?.name ?: "",
                boundElders = elders.map {
                    com.example.healthmanagecenter.ui.screen.ElderBrief(
                        userId = it.userId,
                        name = it.name,
                        lastRecordTime = "-", // 可补充健康数据时间
                        healthStatus = "-" // 可补充健康状态
                    )
                },
                alertsCount = 0, // 可补充异常提醒数量
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                },
                onElderClick = { elderId ->
                    navController.navigate(Screen.ElderHealthDetail.createRoute(elderId.toString(), userId.toString()))
                },
                onHealthDetail = { elderId ->
                    // This navigation is now handled by onElderClick
                    // navController.navigate(Screen.ElderHealthDetail.createRoute(elderId.toString(), userId.toString()))
                }
            )
        }

        composable(
            route = Screen.HealthRecord.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
            HealthRecordScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onNotification = { navController.navigate("notification/$userId") },
                onNavigateToHealthTrend = { userId ->
                    navController.navigate(Screen.HealthTrend.createRoute(userId.toString()))
                }
            )
        }

        composable(
            route = Screen.HealthTrend.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
            HealthTrendScreen(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "notification/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
            NotificationScreen(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DoctorFeedback.route,
            arguments = listOf(
                navArgument("elderId") { type = NavType.LongType },
                navArgument("healthRecordId") { type = NavType.LongType },
                navArgument("doctorId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val elderId = backStackEntry.arguments?.getLong("elderId") ?: return@composable
            val healthRecordId = backStackEntry.arguments?.getLong("healthRecordId") ?: return@composable
            val doctorId = backStackEntry.arguments?.getLong("doctorId") ?: return@composable
            
            DoctorFeedbackScreen(
                elderId = elderId,
                healthRecordId = healthRecordId,
                doctorId = doctorId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ElderHealthDetail.route,
            arguments = listOf(
                navArgument("elderId") { type = NavType.LongType },
                navArgument("doctorId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val elderId = backStackEntry.arguments?.getLong("elderId") ?: return@composable
            val doctorId = backStackEntry.arguments?.getLong("doctorId") ?: return@composable
            
            ElderHealthDetailScreen(
                elderId = elderId,
                doctorId = doctorId,
                onBack = { navController.popBackStack() },
                onNavigateToFeedback = { healthRecordId, eldId, docId ->
                    navController.navigate(Screen.DoctorFeedback.createRoute(eldId.toString(), healthRecordId.toString(), docId.toString()))
                }
            )
        }

        composable(
            route = Screen.ElderFeedback.route,
            arguments = listOf(
                navArgument("elderId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val elderId = backStackEntry.arguments?.getLong("elderId") ?: return@composable
            
            ElderFeedbackScreen(
                elderId = elderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.MedicationReminder.route) {
            MedicationReminderScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 