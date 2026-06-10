package com.example.workouttracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.workouttracker.ui.home.HomeScreen
import com.example.workouttracker.ui.profile.ProfileScreen
import com.example.workouttracker.ui.progress.ProgressScreen
import com.example.workouttracker.ui.workout.WorkoutScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                onStartWorkout = {
                    navController.navigate(BottomNavItem.Workout.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(BottomNavItem.Workout.route) {
            WorkoutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(BottomNavItem.Progress.route) {
            ProgressScreen()
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen()
        }
    }
}
