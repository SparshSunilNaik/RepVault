package com.example.workouttracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.workouttracker.ui.theme.SecondaryText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarHeatmap(
    workoutDates: List<Long>,
    currentStreak: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier
) {
    val weeks = remember(workoutDates) {
        buildWeeks(workoutDates)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StreakBadge(label = "Current", value = currentStreak)
                StreakBadge(label = "Longest", value = longestStreak)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
            val months = weeks.map { it.monthLabel }.distinct()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                months.forEach { month ->
                    Text(
                        text = month,
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val dayLabels = listOf("M", "W", "F")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.width(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    dayLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryText.copy(alpha = 0.5f),
                            modifier = Modifier.height(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    weeks.forEach { week ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            week.days.forEach { day ->
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (day.active) MaterialTheme.colorScheme.primary.copy(
                                                alpha = if (day.intensity > 0.7f) 0.9f
                                                else if (day.intensity > 0.3f) 0.6f
                                                else 0.3f
                                            )
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakBadge(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SecondaryText
        )
    }
}

private data class HeatmapDay(
    val active: Boolean,
    val intensity: Float = 0f
)

private data class HeatmapWeek(
    val monthLabel: String,
    val days: List<HeatmapDay>
)

private fun buildWeeks(workoutDates: List<Long>): List<HeatmapWeek> {
    val cal = Calendar.getInstance()
    val dateSet = workoutDates.map { date ->
        cal.time = Date(date)
        "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }.toSet()

    cal.time = Date()
    val endCal = Calendar.getInstance()
    endCal.time = cal.time

    cal.add(Calendar.MONTH, -3)
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    if (cal.timeInMillis > endCal.timeInMillis) {
        cal.add(Calendar.WEEK_OF_YEAR, -1)
    }

    val weeks = mutableListOf<HeatmapWeek>()
    val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

    while (cal.timeInMillis <= endCal.timeInMillis) {
        val weekDays = mutableListOf<HeatmapDay>()
        val monthLabel = monthFormat.format(cal.time)

        for (i in 0..6) {
            val key = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
            val active = key in dateSet
            val totalWorkouts = if (active) 1 else 0
            weekDays.add(
                HeatmapDay(
                    active = active,
                    intensity = if (active) 0.6f else 0f
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        weeks.add(HeatmapWeek(monthLabel = monthLabel, days = weekDays))
    }

    return weeks
}
