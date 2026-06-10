package com.example.workouttracker.ui.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workouttracker.data.repository.WorkoutRepository
import com.example.workouttracker.ui.components.ExerciseEmptyState
import com.example.workouttracker.ui.theme.Accent
import com.example.workouttracker.ui.theme.SecondaryText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutScreen(
    onNavigateBack: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    if (uiState.showTemplatePicker) {
        TemplatePickerSheet(
            onDismiss = { viewModel.dismissTemplatePicker() },
            onTemplateSelected = { template -> viewModel.selectTemplate(template) }
        )
    }

    if (uiState.showExerciseSearch) {
        ExerciseSearchSheet(
            query = uiState.searchQuery,
            results = uiState.searchResults,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onExerciseSelected = { exercise -> viewModel.addExerciseToWorkout(exercise) },
            onCreateExercise = { name -> viewModel.createAndAddExercise(name) },
            onDismiss = { viewModel.dismissExerciseSearch() }
        )
    }

    if (uiState.showPreviousWorkout && uiState.previousWorkout != null) {
        PreviousWorkoutSheet(
            previousWorkout = uiState.previousWorkout!!,
            onUsePrevious = { viewModel.applyPreviousSets() },
            onDismiss = { viewModel.dismissPreviousWorkout() }
        )
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
                .padding(top = 24.dp, bottom = 24.dp)
        ) {
            Text(
                text = uiState.templateName ?: "New Workout",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.exercises.isEmpty()) {
                ExerciseEmptyState()
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.showExerciseSearch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Exercise",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                uiState.exercises.forEachIndexed { exerciseIndex, exerciseState ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        ExerciseCard(
                            exerciseIndex = exerciseIndex,
                            exerciseState = exerciseState,
                            onRemoveExercise = { viewModel.removeExercise(exerciseIndex) },
                            onWeightChange = { viewModel.updateExerciseWeight(exerciseIndex, it) },
                            onRepsChange = { viewModel.updateExerciseReps(exerciseIndex, it) },
                            onAddSet = {
                                focusManager.clearFocus()
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.addSetToExercise(exerciseIndex)
                            },
                            onRemoveSet = { setIndex ->
                                viewModel.removeSetFromExercise(exerciseIndex, setIndex)
                            },
                            onMoveFocusDown = { focusManager.moveFocus(FocusDirection.Down) },
                            onAddSetImmediate = {
                                focusManager.clearFocus()
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.addSetToExercise(exerciseIndex)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.showExerciseSearch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Exercise",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveWorkout { onNavigateBack() }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    enabled = !uiState.isSaving
                ) {
                    Text(
                        text = if (uiState.isSaving) "Saving..." else "Save Workout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exerciseIndex: Int,
    exerciseState: ExerciseInWorkout,
    onRemoveExercise: () -> Unit,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onMoveFocusDown: () -> Unit,
    onAddSetImmediate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exerciseState.exercise.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = exerciseState.exercise.muscleGroup,
                        style = MaterialTheme.typography.labelMedium,
                        color = SecondaryText
                    )
                }
                IconButton(onClick = onRemoveExercise) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove exercise",
                        tint = SecondaryText
                    )
                }
            }

            AnimatedVisibility(
                visible = exerciseState.sets.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(modifier = Modifier.animateContentSize()) {
                    exerciseState.sets.forEachIndexed { setIndex, set ->
                        SetRow(
                            setNumber = set.setNumber,
                            weight = set.weight,
                            reps = set.reps,
                            onRemove = { onRemoveSet(setIndex) }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = exerciseState.currentWeight,
                    onValueChange = onWeightChange,
                    placeholder = { Text("kg", color = SecondaryText.copy(alpha = 0.4f)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onMoveFocusDown() }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = exerciseState.currentReps,
                    onValueChange = onRepsChange,
                    placeholder = { Text("reps", color = SecondaryText.copy(alpha = 0.4f)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onAddSet() }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = onAddSet,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = exerciseState.currentWeight.isNotBlank() &&
                            exerciseState.currentReps.isNotBlank(),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SetRow(
    setNumber: Int,
    weight: Double,
    reps: Int,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$setNumber",
            style = MaterialTheme.typography.titleMedium,
            color = SecondaryText,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp)
        )

        Text(
            text = formatWeight(weight),
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
            text = "$reps",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(40.dp)
        )

        Text(
            text = "reps",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove set",
                tint = SecondaryText,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviousWorkoutSheet(
    previousWorkout: WorkoutRepository.PreviousExerciseWorkout,
    onUsePrevious: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Last Session",
                style = MaterialTheme.typography.titleMedium,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateFormat.format(Date(previousWorkout.sessionDate)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            previousWorkout.sets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onUsePrevious,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Use Previous Sets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Start Fresh",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

private fun formatWeight(weight: Double): String {
    return if (weight == weight.toLong().toDouble()) {
        "${weight.toLong()} kg"
    } else {
        "${String.format("%.1f", weight)} kg"
    }
}
