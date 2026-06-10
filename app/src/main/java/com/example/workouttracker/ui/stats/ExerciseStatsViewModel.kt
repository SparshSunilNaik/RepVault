package com.example.workouttracker.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.RepVaultApp
import com.example.workouttracker.data.db.entities.ExerciseEntity
import com.example.workouttracker.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatsUiState(
    val exercise: ExerciseEntity? = null,
    val stats: WorkoutRepository.ExerciseStats? = null,
    val weightHistory: List<Pair<Long, Double>> = emptyList(),
    val volumeHistory: List<Pair<Long, Double>> = emptyList(),
    val isLoading: Boolean = true
)

class ExerciseStatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RepVaultApp).repository

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    fun loadExercise(exerciseId: Long) {
        viewModelScope.launch {
            val allExercises = repository.getAllExercises()
            val exercise = allExercises.find { it.id == exerciseId }
            val stats = repository.getExerciseStats(exerciseId)
            val allSessions = repository.getAllSessions()
            val weightHistory = mutableListOf<Pair<Long, Double>>()
            val volumeHistory = mutableListOf<Pair<Long, Double>>()

            for (session in allSessions.sortedBy { it.date }) {
                val sets = repository.getSetsForSession(session.id)
                    .filter { it.exerciseId == exerciseId }
                if (sets.isNotEmpty()) {
                    val maxWeight = sets.maxOf { it.weight }
                    val volume = sets.sumOf { it.weight * it.reps }
                    weightHistory.add(session.date to maxWeight)
                    volumeHistory.add(session.date to volume)
                }
            }

            _uiState.update {
                it.copy(
                    exercise = exercise,
                    stats = stats,
                    weightHistory = weightHistory,
                    volumeHistory = volumeHistory,
                    isLoading = false
                )
            }
        }
    }
}
