package com.example.workouttracker.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workouttracker.data.repository.WorkoutRepository
import com.example.workouttracker.ui.components.CalendarHeatmap
import com.example.workouttracker.ui.components.WorkoutEmptyState
import com.example.workouttracker.ui.theme.Accent
import com.example.workouttracker.ui.theme.Divider
import com.example.workouttracker.ui.theme.SecondaryText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onWorkoutClick: (Long) -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 56.dp, bottom = 24.dp)
    ) {
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!uiState.isLoading) {
            CalendarHeatmap(
                workoutDates = uiState.workoutDates,
                currentStreak = uiState.currentStreak,
                longestStreak = uiState.longestStreak
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.dateGroups.isEmpty() && !uiState.isLoading) {
            WorkoutEmptyState()
        } else {
            uiState.dateGroups.forEach { group ->
                DateGroupCard(
                    group = group,
                    onClick = { sessionId ->
                        onWorkoutClick(sessionId)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DateGroupCard(
    group: WorkoutRepository.DateGroup,
    onClick: (Long) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault())

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateFormat.format(Date(group.date)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dayOfWeek.format(Date(group.date)),
                style = MaterialTheme.typography.labelMedium,
                color = SecondaryText
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        group.sessions.forEach { session ->
            SessionCard(session = session, onClick = { onClick(session.sessionId) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SessionCard(
    session: WorkoutRepository.SessionDetail,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.workoutType.ifEmpty { "Workout" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = timeFormat.format(Date(session.date)),
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            session.exercises.forEach { exercise ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = exercise.exerciseName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row {
                        exercise.sets.take(3).forEach { set ->
                            Text(
                                text = "${set.weight.toLong()}x${set.reps}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryText,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        if (exercise.sets.size > 3) {
                            Text(
                                text = "+${exercise.sets.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Accent,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Divider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${session.totalExercises} exercises",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryText
                )
                Text(
                    text = "${session.totalSets} sets",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryText
                )
                Text(
                    text = "${session.totalVolume.toLong()} kg",
                    style = MaterialTheme.typography.labelMedium,
                    color = Accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
