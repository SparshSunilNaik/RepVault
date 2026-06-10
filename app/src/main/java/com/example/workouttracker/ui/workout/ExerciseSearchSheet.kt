package com.example.workouttracker.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workouttracker.data.db.entities.ExerciseEntity
import com.example.workouttracker.ui.theme.Accent
import com.example.workouttracker.ui.theme.Divider
import com.example.workouttracker.ui.theme.SecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSearchSheet(
    query: String,
    results: List<ExerciseEntity>,
    onQueryChange: (String) -> Unit,
    onExerciseSelected: (ExerciseEntity) -> Unit,
    onCreateExercise: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Add Exercise",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search exercises...", color = SecondaryText.copy(0.4f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (query.isBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Type to search exercises",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    if (results.isEmpty()) {
                        item {
                            CreateExerciseRow(
                                query = query,
                                onClick = { onCreateExercise(query) }
                            )
                        }
                    } else {
                        items(results) { exercise ->
                            ExerciseResultRow(
                                exercise = exercise,
                                onClick = { onExerciseSelected(exercise) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(
                                color = Divider,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            CreateExerciseRow(
                                query = query,
                                onClick = { onCreateExercise(query) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateExerciseRow(
    query: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Create \"$query\"",
                style = MaterialTheme.typography.titleMedium,
                color = Accent,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Custom exercise \u2022 Tap to add to workout",
                style = MaterialTheme.typography.labelMedium,
                color = SecondaryText
            )
        }
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Create and add exercise",
            tint = Accent
        )
    }
}

@Composable
private fun ExerciseResultRow(
    exercise: ExerciseEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = exercise.muscleGroup,
                style = MaterialTheme.typography.labelMedium,
                color = SecondaryText
            )
        }
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add exercise",
            tint = Accent
        )
    }
}
