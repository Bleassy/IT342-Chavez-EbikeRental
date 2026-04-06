package com.ebike.mobile.ui.screens

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ebike.mobile.MainActivity
import com.ebike.mobile.ui.viewmodels.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object BikeList : Screen("bikes")
    object BikeDetail : Screen("bike_detail/{bikeId}") {
        fun createRoute(bikeId: Long) = "bike_detail/$bikeId"
    }
    object BookingConfirmation : Screen("booking_confirmation/{bookingId}") {
        fun createRoute(bookingId: Long) = "booking_confirmation/$bookingId"
    }
    object BookingHistory : Screen("booking_history")
    object Profile : Screen("profile")
    object AdminPanel : Screen("admin")
}

@Composable
fun AppNavigation(
    mainActivity: MainActivity? = null,
    googleSignInLauncher: ActivityResultLauncher<android.content.Intent>? = null
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel = AuthViewModel(context)
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    val startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                navController,
                authViewModel,
                mainActivity,
                googleSignInLauncher
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController, authViewModel)
        }
        
        // App Screens
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController, authViewModel)
        }
        
        composable(Screen.BikeList.route) {
            BikeListScreen(navController)
        }
        
        composable(Screen.BikeDetail.route) { backStackEntry ->
            val bikeId = backStackEntry.arguments?.getString("bikeId")?.toLongOrNull()
            if (bikeId != null) {
                BikeDetailScreen(navController, bikeId)
            }
        }
        
        composable(Screen.BookingConfirmation.route) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId")?.toLongOrNull()
            if (bookingId != null) {
                BookingConfirmationScreen(navController, bookingId)
            }
        }
        
        composable(Screen.BookingHistory.route) {
            BookingHistoryScreen(navController)
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(navController, authViewModel)
        }
    }
}
