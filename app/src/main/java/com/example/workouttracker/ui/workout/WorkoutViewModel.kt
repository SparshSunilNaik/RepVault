package com.example.workouttracker.ui.workout

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.RepVaultApp
import com.example.workouttracker.data.db.entities.ExerciseEntity
import com.example.workouttracker.data.repository.WorkoutRepository
import com.example.workouttracker.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetDisplay(
    val weight: Double,
    val reps: Int,
    val setNumber: Int
)

data class ExerciseInWorkout(
    val exercise: ExerciseEntity,
    val sets: MutableList<SetDisplay> = mutableListOf(),
    val currentWeight: String = "",
    val currentReps: String = ""
)

data class WorkoutUiState(
    val templateName: String? = null,
    val templateType: WorkoutTemplate? = null,
    val exercises: List<ExerciseInWorkout> = emptyList(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val showTemplatePicker: Boolean = true,
    val showExerciseSearch: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<ExerciseEntity> = emptyList(),
    val recentExercises: List<ExerciseEntity> = emptyList(),
    val previousWorkout: WorkoutRepository.PreviousExerciseWorkout? = null,
    val showPreviousWorkout: Boolean = false,
    val previousWorkoutExerciseId: Long = -1L
)

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "RepVault"
    }

    private val repository = (application as RepVaultApp).repository

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var exerciseSetCounters = mutableMapOf<Long, Int>()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            val all = repository.getAllExercises()
            _uiState.update { it.copy(recentExercises = all) }
        }
    }

    fun selectTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            val exercises = if (template == WorkoutTemplate.CUSTOM) {
                emptyList()
            } else {
                repository.getExercisesByTemplate(template)
            }
            val exerciseStates = exercises.map { exercise ->
                exerciseSetCounters[exercise.id] = 0
                ExerciseInWorkout(exercise = exercise)
            }
            _uiState.update {
                it.copy(
                    templateName = template.displayName,
                    templateType = template,
                    exercises = exerciseStates,
                    showTemplatePicker = false
                )
            }
            Log.d(TAG, "Template selected: ${template.displayName} (${exercises.size} exercises)")
        }
    }

    fun dismissTemplatePicker() {
        _uiState.update { it.copy(showTemplatePicker = false) }
    }

    fun showExerciseSearch() {
        _uiState.update { it.copy(showExerciseSearch = true, searchQuery = "", searchResults = emptyList()) }
    }

    fun dismissExerciseSearch() {
        _uiState.update { it.copy(showExerciseSearch = false, searchQuery = "", searchResults = emptyList(), previousWorkout = null, showPreviousWorkout = false) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            val results = if (query.isBlank()) {
                emptyList()
            } else {
                repository.searchExercises(query)
            }
            _uiState.update { it.copy(searchResults = results) }
        }
    }

    fun addExerciseToWorkout(exercise: ExerciseEntity) {
        val current = _uiState.value.exercises.toMutableList()
        if (current.none { it.exercise.id == exercise.id }) {
            exerciseSetCounters[exercise.id] = 0
            current.add(ExerciseInWorkout(exercise = exercise))
            _uiState.update {
                it.copy(exercises = current, showExerciseSearch = false)
            }
            Log.d(TAG, "Added exercise to workout: ${exercise.name}")

            // Check for previous workout data
            viewModelScope.launch {
                val previous = repository.getPreviousWorkoutForExercise(exercise.id)
                if (previous != null && previous.sets.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            previousWorkout = previous,
                            showPreviousWorkout = true,
                            previousWorkoutExerciseId = exercise.id
                        )
                    }
                }
            }
        } else {
            _uiState.update { it.copy(showExerciseSearch = false) }
        }
    }

    fun dismissPreviousWorkout() {
        _uiState.update { it.copy(showPreviousWorkout = false, previousWorkout = null) }
    }

    fun applyPreviousSets() {
        val previous = _uiState.value.previousWorkout ?: return
        val exerciseId = _uiState.value.previousWorkoutExerciseId
        val current = _uiState.value.exercises.toMutableList()
        val index = current.indexOfFirst { it.exercise.id == exerciseId }
        if (index < 0) return

        val ex = current[index]
        val newSets = mutableListOf<SetDisplay>()
        var counter = 0
        for (set in previous.sets) {
            counter++
            newSets.add(SetDisplay(weight = set.weight, reps = set.reps, setNumber = counter))
        }
        exerciseSetCounters[exerciseId] = counter
        current[index] = ex.copy(sets = newSets)

        _uiState.update {
            it.copy(
                exercises = current,
                showPreviousWorkout = false,
                previousWorkout = null
            )
        }
        Log.d(TAG, "Applied ${previous.sets.size} previous sets for exercise $exerciseId")
    }

    fun removeExercise(exerciseIndex: Int) {
        val current = _uiState.value.exercises.toMutableList()
        if (exerciseIndex in current.indices) {
            val removed = current.removeAt(exerciseIndex)
            exerciseSetCounters.remove(removed.exercise.id)
            _uiState.update { it.copy(exercises = current) }
        }
    }

    fun updateExerciseWeight(exerciseIndex: Int, weight: String) {
        if (weight.isNotEmpty() && !weight.matches(Regex("^\\d*\\.?\\d{0,2}$"))) return
        val current = _uiState.value.exercises.toMutableList()
        if (exerciseIndex in current.indices) {
            current[exerciseIndex] = current[exerciseIndex].copy(currentWeight = weight)
            _uiState.update { it.copy(exercises = current) }
        }
    }

    fun updateExerciseReps(exerciseIndex: Int, reps: String) {
        if (reps.isNotEmpty() && !reps.matches(Regex("^\\d*$"))) return
        val current = _uiState.value.exercises.toMutableList()
        if (exerciseIndex in current.indices) {
            current[exerciseIndex] = current[exerciseIndex].copy(currentReps = reps)
            _uiState.update { it.copy(exercises = current) }
        }
    }

    fun addSetToExercise(exerciseIndex: Int) {
        val current = _uiState.value.exercises.toMutableList()
        if (exerciseIndex !in current.indices) return

        val ex = current[exerciseIndex]
        val weight = ex.currentWeight.toDoubleOrNull() ?: return
        val reps = ex.currentReps.toIntOrNull() ?: return

        val counter = exerciseSetCounters.getOrDefault(ex.exercise.id, 0) + 1
        exerciseSetCounters[ex.exercise.id] = counter

        val newSets = ex.sets.toMutableList()
        newSets.add(SetDisplay(weight = weight, reps = reps, setNumber = counter))
        current[exerciseIndex] = ex.copy(sets = newSets, currentWeight = "", currentReps = "")

        _uiState.update { it.copy(exercises = current) }
    }

    fun removeSetFromExercise(exerciseIndex: Int, setIndex: Int) {
        val current = _uiState.value.exercises.toMutableList()
        if (exerciseIndex !in current.indices) return

        val ex = current[exerciseIndex]
        val newSets = ex.sets.toMutableList()
        if (setIndex in newSets.indices) {
            newSets.removeAt(setIndex)
            current[exerciseIndex] = ex.copy(sets = newSets)
            _uiState.update { it.copy(exercises = current) }
        }
    }

    fun createAndAddExercise(name: String) {
        viewModelScope.launch {
            val exercise = repository.getOrCreateExercise(name)
            addExerciseToWorkout(exercise)
        }
    }

    fun saveWorkout(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val exercises = _uiState.value.exercises
            val templateType = _uiState.value.templateType
            val totalSets = exercises.sumOf { it.sets.size }

            val sessionId = repository.createSession(
                workoutType = _uiState.value.templateName ?: ""
            )

            for (exercise in exercises) {
                for (set in exercise.sets) {
                    repository.addSet(sessionId, exercise.exercise.id, set.weight, set.reps)
                }
            }

            _uiState.update {
                WorkoutUiState(
                    isSaving = false,
                    savedSuccessfully = true,
                    showTemplatePicker = false,
                    recentExercises = it.recentExercises
                )
            }
            onComplete()
        }
    }

    fun resetState() {
        exerciseSetCounters.clear()
        loadExercises()
        _uiState.update {
            WorkoutUiState(
                showTemplatePicker = true,
                recentExercises = it.recentExercises
            )
        }
    }
}
