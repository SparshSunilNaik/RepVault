package com.example.workouttracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.workouttracker.ui.detail.WorkoutDetailScreen
import com.example.workouttracker.ui.home.HomeScreen
import com.example.workouttracker.ui.history.HistoryScreen
import com.example.workouttracker.ui.profile.ProfileScreen
import com.example.workouttracker.ui.stats.ExerciseStatsScreen
import com.example.workouttracker.ui.workout.WorkoutScreen

object NavRoutes {
    const val HOME = "home"
    const val WORKOUT = "workout"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val WORKOUT_DETAIL = "detail/{sessionId}"
    const val EXERCISE_STATS = "stats/{exerciseId}"

    fun workoutDetail(sessionId: Long) = "detail/$sessionId"
    fun exerciseStats(exerciseId: Long) = "stats/$exerciseId"
}

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
        composable(BottomNavItem.History.route) {
            HistoryScreen(
                onWorkoutClick = { sessionId ->
                    navController.navigate(NavRoutes.workoutDetail(sessionId))
                }
            )
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen()
        }
        composable(
            route = NavRoutes.WORKOUT_DETAIL,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
            WorkoutDetailScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() },
                onExerciseClick = { exerciseId ->
                    navController.navigate(NavRoutes.exerciseStats(exerciseId))
                }
            )
        }
        composable(
            route = NavRoutes.EXERCISE_STATS,
            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: return@composable
            ExerciseStatsScreen(
                exerciseId = exerciseId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
