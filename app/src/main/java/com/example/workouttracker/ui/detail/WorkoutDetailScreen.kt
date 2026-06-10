package com.example.workouttracker.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workouttracker.data.repository.WorkoutRepository
import com.example.workouttracker.ui.theme.Accent
import com.example.workouttracker.ui.theme.Danger
import com.example.workouttracker.ui.theme.Divider
import com.example.workouttracker.ui.theme.SecondaryText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutDetailScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    onExerciseClick: (Long) -> Unit,
    viewModel: WorkoutDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onNavigateBack()
        }
    }

    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteSessionDialog() },
            title = { Text("Delete Workout") },
            text = { Text("This will permanently delete this workout and all its sets.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteSession() }) {
                    Text("Delete", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteSessionDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showDeleteExerciseConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteExerciseDialog() },
            title = { Text("Remove Exercise") },
            text = { Text("Remove this exercise and all its sets from the workout.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteExercise() }) {
                    Text("Remove", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteExerciseDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showDeleteSetConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteSetDialog() },
            title = { Text("Delete Set") },
            text = { Text("Delete this set?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteSet() }) {
                    Text("Delete", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteSetDialog() }) {
                    Text("Cancel")
                }
            }
        )
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!uiState.editMode) {
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = { viewModel.showDeleteSessionDialog() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete workout",
                        tint = Danger
                    )
                }
            }

            val detail = uiState.sessionDetail
            if (detail != null) {
                if (!uiState.editMode) {
                    WorkoutHeader(detail = detail)
                } else {
                    EditableWorkoutHeader(
                        workoutType = uiState.editableType,
                        onTypeChange = { viewModel.updateWorkoutType(it) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                StatsBar(detail = detail)

                Spacer(modifier = Modifier.height(20.dp))

                detail.exercises.forEachIndexed { index, exercise ->
                    if (uiState.editMode) {
                        EditableExerciseCard(
                            exercise = exercise,
                            onDeleteExercise = { viewModel.showDeleteExerciseDialog(index) },
                            onWeightChange = { setId, weight -> viewModel.updateSetWeight(setId, weight) },
                            onRepsChange = { setId, reps -> viewModel.updateSetReps(setId, reps) },
                            onDeleteSet = { setId -> viewModel.showDeleteSetDialog(setId) }
                        )
                    } else {
                        ExerciseDetailCard(
                            exercise = exercise,
                            onClick = { onExerciseClick(exercise.exerciseId) }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.editMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.saveChanges()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = !uiState.isSaving
                    ) {
                        Text(
                            text = if (uiState.isSaving) "Saving..." else "Save Changes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutHeader(detail: WorkoutRepository.SessionDetail) {
    val dateFormat = SimpleDateFormat("MMMM d, yyyy 'at' HH:mm", Locale.getDefault())

    Text(
        text = detail.workoutType.ifEmpty { "Workout" },
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = dateFormat.format(Date(detail.date)),
        style = MaterialTheme.typography.bodyMedium,
        color = SecondaryText
    )
}

@Composable
private fun EditableWorkoutHeader(workoutType: String, onTypeChange: (String) -> Unit) {
    OutlinedTextField(
        value = workoutType,
        onValueChange = onTypeChange,
        label = { Text("Workout Type") },
        placeholder = { Text("e.g. Push, Pull, Legs", color = SecondaryText.copy(alpha = 0.4f)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        textStyle = MaterialTheme.typography.titleLarge
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun StatsBar(detail: WorkoutRepository.SessionDetail) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "${detail.totalExercises}", label = "Exercises")
            StatItem(value = "${detail.totalSets}", label = "Sets")
            StatItem(value = "${detail.totalVolume.toLong()}", label = "Volume (kg)")
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SecondaryText
        )
    }
}

@Composable
private fun ExerciseDetailCard(
    exercise: WorkoutRepository.ExerciseDetail,
    onClick: () -> Unit
) {
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
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${exercise.volume.toLong()} kg",
                    style = MaterialTheme.typography.labelMedium,
                    color = Accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            exercise.sets.forEachIndexed { setIndex, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${setIndex + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText,
                        modifier = Modifier.width(24.dp)
                    )
                    Text(
                        text = "${set.weight.toLong()} kg",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(80.dp)
                    )
                    Text(
                        text = "x",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${set.reps}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "reps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableExerciseCard(
    exercise: WorkoutRepository.ExerciseDetail,
    onDeleteExercise: () -> Unit,
    onWeightChange: (Long, Double) -> Unit,
    onRepsChange: (Long, Int) -> Unit,
    onDeleteSet: (Long) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDeleteExercise) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove exercise",
                        tint = Danger,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            exercise.sets.forEach { set ->
                EditableSetRow(
                    setId = set.id,
                    weight = set.weight,
                    reps = set.reps,
                    setNumber = exercise.sets.indexOf(set) + 1,
                    onWeightChange = onWeightChange,
                    onRepsChange = onRepsChange,
                    onDelete = onDeleteSet
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun EditableSetRow(
    setId: Long,
    weight: Double,
    reps: Int,
    setNumber: Int,
    onWeightChange: (Long, Double) -> Unit,
    onRepsChange: (Long, Int) -> Unit,
    onDelete: (Long) -> Unit
) {
    var weightText by remember(weight) { mutableStateOf(weight.toLong().toString()) }
    var repsText by remember(reps) { mutableStateOf(reps.toString()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$setNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            modifier = Modifier.width(24.dp)
        )

        OutlinedTextField(
            value = weightText,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    weightText = newValue
                    newValue.toDoubleOrNull()?.let { onWeightChange(setId, it) }
                }
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Divider,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "x",
            style = MaterialTheme.typography.bodyLarge,
            color = SecondaryText,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        OutlinedTextField(
            value = repsText,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*$"))) {
                    repsText = newValue
                    newValue.toIntOrNull()?.let { onRepsChange(setId, it) }
                }
            },
            modifier = Modifier
                .width(60.dp)
                .padding(horizontal = 4.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Divider,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        IconButton(onClick = { onDelete(setId) }, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete set",
                tint = Danger,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
