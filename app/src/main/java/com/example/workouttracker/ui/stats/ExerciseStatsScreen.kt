package com.example.workouttracker.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.workouttracker.ui.theme.Accent
import com.example.workouttracker.ui.theme.SecondaryText
import com.example.workouttracker.ui.components.SimpleLineChart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseStatsScreen(
    exerciseId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ExerciseStatsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(exerciseId) {
        viewModel.loadExercise(exerciseId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = uiState.exercise?.name ?: "Exercise",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (uiState.exercise?.muscleGroup?.isNotEmpty() == true) {
                        Text(
                            text = uiState.exercise!!.muscleGroup,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val stats = uiState.stats
            if (stats != null) {
                StatsGrid(stats = stats)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.weightHistory.size >= 2) {
                Text(
                    text = "Weight Progress",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                SimpleLineChart(
                    data = uiState.weightHistory,
                    label = "kg",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (uiState.volumeHistory.size >= 2) {
                Text(
                    text = "Volume Progress",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                SimpleLineChart(
                    data = uiState.volumeHistory,
                    label = "kg",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(stats: com.example.workouttracker.data.repository.WorkoutRepository.ExerciseStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Best Weight",
            value = "${stats.bestWeight.toLong()} kg",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Total Volume",
            value = "${stats.totalVolume.toLong()} kg",
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Sessions",
            value = "${stats.totalSessions}",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Total Sets",
            value = "${stats.totalSets}",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Avg Reps",
            value = String.format("%.1f", stats.avgReps),
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    StatCard(
        label = "Last Trained",
        value = if (stats.lastTrainedDate > 0) {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(stats.lastTrainedDate))
        } else {
            "Never"
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
