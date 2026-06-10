package com.example.workouttracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    data object Workout : BottomNavItem("workout", "Workout", Icons.Filled.Add)
    data object Progress : BottomNavItem("progress", "Progress", Icons.Filled.Star)
    data object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person)

    companion object {
        val items = listOf(Home, Workout, Progress, Profile)
    }
}
