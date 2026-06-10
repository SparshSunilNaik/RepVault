package com.example.workouttracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.workouttracker.ui.theme.SecondaryText

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SecondaryText.copy(alpha = 0.3f),
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun WorkoutEmptyState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Default.Favorite,
        title = "No workouts yet",
        subtitle = "Complete your first workout to see it here",
        modifier = modifier
    )
}

@Composable
fun ProgressEmptyState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Default.Star,
        title = "No data yet",
        subtitle = "Complete a workout to see your progress",
        modifier = modifier
    )
}

@Composable
fun ExerciseEmptyState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Default.Favorite,
        title = "No exercises yet",
        subtitle = "Tap the button below to add your first exercise",
        modifier = modifier
    )
}
