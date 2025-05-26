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

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object PickDoctor : Screen("pick_doctor/{userId}")
    object ElderHome : Screen("elder_home/{userId}")
    object DoctorHome : Screen("doctor_home/{userId}")

    fun createRoute(vararg args: String): String {
        var result = route
        args.forEachIndexed { index, arg ->
            result = result.replace("{userId}", arg)
        }
        return result
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
                    onHealthRecords = { /* TODO */ },
                    onMedication = { /* TODO */ },
                    onAlerts = { /* TODO */ }
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
                onElderClick = { /* TODO */ }
            )
        }
    }
} 