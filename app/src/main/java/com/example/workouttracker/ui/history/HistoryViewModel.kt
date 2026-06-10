package com.example.workouttracker.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.RepVaultApp
import com.example.workouttracker.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val dateGroups: List<WorkoutRepository.DateGroup> = emptyList(),
    val workoutDates: List<Long> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isLoading: Boolean = true
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RepVaultApp).repository

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val groups = repository.getSessionsGroupedByDate()
            val dates = repository.getAllWorkoutDates()
            val currentStreak = repository.getCurrentStreak()
            val longestStreak = repository.getLongestStreak()
            _uiState.update {
                it.copy(
                    dateGroups = groups,
                    workoutDates = dates,
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadData()
    }
}
