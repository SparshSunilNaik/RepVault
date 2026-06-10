package com.example.workouttracker.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workouttracker.ui.components.WorkoutEmptyState
import com.example.workouttracker.ui.profile.NamePromptSheet
import com.example.workouttracker.ui.theme.Accent
import com.example.workouttracker.ui.theme.SecondaryText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit,
    onViewHistory: (() -> Unit)? = null,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showNamePrompt) {
        NamePromptSheet(onSave = { viewModel.saveUserName(it) })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 24.dp)
        ) {
            GreetingSection(
                greeting = uiState.greeting,
                userName = uiState.userName
            )

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = !uiState.isLoading,
                enter = fadeIn() + slideInVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    StreakSection(
                        currentStreak = uiState.streak,
                        longestStreak = uiState.longestStreak
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.recentPRName.isNotEmpty()) {
                            RecentPRCard(
                                exerciseName = uiState.recentPRName,
                                weight = uiState.recentPRWeight,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        QuickStatsCard(
                            totalWorkouts = uiState.totalWorkouts,
                            totalVolume = uiState.totalVolume,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (uiState.totalWorkouts > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MiniStatCard(
                                label = "Total Sets",
                                value = "${uiState.totalSets}",
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.favoriteExercise != null) {
                                MiniStatCard(
                                    label = "Favorite",
                                    value = uiState.favoriteExercise!!.name,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (uiState.recentSessions.isNotEmpty()) {
                        WorkoutHistoryCard(sessions = uiState.recentSessions)
                    }

                    if (!uiState.isLoading && uiState.totalWorkouts == 0) {
                        WorkoutEmptyState()
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStartWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(
                    text = "Start Workout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GreetingSection(greeting: String, userName: String) {
    val displayName = if (userName.isNotBlank()) userName else "Athlete"

    Text(
        text = "$greeting,",
        style = MaterialTheme.typography.titleLarge,
        color = SecondaryText
    )
    Text(
        text = displayName,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun StreakSection(currentStreak: Int, longestStreak: Int) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Current Streak",
                    style = MaterialTheme.typography.labelLarge,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$currentStreak",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (currentStreak == 1) "day" else "days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Longest Streak",
                    style = MaterialTheme.typography.labelLarge,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$longestStreak",
                    style = MaterialTheme.typography.displayLarge,
                    color = Accent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (longestStreak == 1) "day" else "days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
            }
        }
        if (currentStreak == 0 && longestStreak == 0) {
            Text(
                text = "Start a workout to begin your streak",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
                modifier = Modifier.padding(start = 24.dp, bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun RecentPRCard(exerciseName: String, weight: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Recent PR",
                style = MaterialTheme.typography.labelLarge,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = weight,
                style = MaterialTheme.typography.headlineMedium,
                color = Accent
            )
        }
    }
}

@Composable
private fun QuickStatsCard(totalWorkouts: Int, totalVolume: Double, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Workouts",
                style = MaterialTheme.typography.labelLarge,
                color = SecondaryText
            )
            Text(
                text = "$totalWorkouts",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Volume",
                style = MaterialTheme.typography.labelLarge,
                color = SecondaryText
            )
            Text(
                text = "${totalVolume.toLong()} kg",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MiniStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WorkoutHistoryCard(sessions: List<com.example.workouttracker.data.repository.WorkoutRepository.SessionSummary>) {
    val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Recent Workouts",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            sessions.forEachIndexed { index, session ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dateFormat.format(Date(session.date)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText
                        )
                        if (session.exerciseNames.isNotEmpty()) {
                            Text(
                                text = session.exerciseNames.joinToString(", "),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${session.setCount} sets",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (index < sessions.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
