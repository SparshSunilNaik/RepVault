package com.example.workouttracker.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.RepVaultApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetDisplay(
    val exerciseName: String,
    val weight: Double,
    val reps: Int,
    val setNumber: Int
)

data class WorkoutUiState(
    val exerciseName: String = "",
    val weight: String = "",
    val reps: String = "",
    val currentSets: List<SetDisplay> = emptyList(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val recentExercises: List<String> = emptyList(),
    val showExerciseDropdown: Boolean = false
)

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RepVaultApp).repository

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var setCounter = 0

    init {
        loadRecentExercises()
    }

    private fun loadRecentExercises() {
        viewModelScope.launch {
            val exercises = repository.getAllExercises()
            _uiState.update {
                it.copy(
                    recentExercises = exercises.map { e -> e.name }
                )
            }
        }
    }

    fun updateExerciseName(name: String) {
        _uiState.update {
            it.copy(
                exerciseName = name,
                showExerciseDropdown = name.isNotEmpty() && it.recentExercises.any { e ->
                    e.startsWith(name, ignoreCase = true) && e != name
                }
            )
        }
    }

    fun selectExercise(name: String) {
        _uiState.update {
            it.copy(
                exerciseName = name,
                showExerciseDropdown = false
            )
        }
    }

    fun dismissDropdown() {
        _uiState.update { it.copy(showExerciseDropdown = false) }
    }

    fun updateWeight(weight: String) {
        if (weight.isEmpty() || weight.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(weight = weight) }
        }
    }

    fun updateReps(reps: String) {
        if (reps.isEmpty() || reps.matches(Regex("^\\d*$"))) {
            _uiState.update { it.copy(reps = reps) }
        }
    }

    fun addSet() {
        val state = _uiState.value
        val weight = state.weight.toDoubleOrNull() ?: return
        val reps = state.reps.toIntOrNull() ?: return
        val name = state.exerciseName.trim().ifBlank { return }

        setCounter++
        _uiState.update {
            it.copy(
                currentSets = it.currentSets + SetDisplay(
                    exerciseName = name,
                    weight = weight,
                    reps = reps,
                    setNumber = setCounter
                ),
                weight = "",
                reps = ""
            )
        }
    }

    fun removeSet(index: Int) {
        val current = _uiState.value.currentSets.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.update {
                it.copy(currentSets = current)
            }
        }
    }

    fun saveWorkout(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val sessionId = repository.createSession()
            val sets = _uiState.value.currentSets

            for (set in sets) {
                val exercise = repository.getOrCreateExercise(set.exerciseName)
                repository.addSet(sessionId, exercise.id, set.weight, set.reps)
            }

            _uiState.update {
                WorkoutUiState(
                    isSaving = false,
                    savedSuccessfully = true,
                    recentExercises = it.recentExercises
                )
            }
            onComplete()
        }
    }

    fun resetState() {
        setCounter = 0
        loadRecentExercises()
        _uiState.update {
            WorkoutUiState(recentExercises = it.recentExercises)
        }
    }
}
