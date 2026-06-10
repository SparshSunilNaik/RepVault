package com.example.workouttracker.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.RepVaultApp
import com.example.workouttracker.data.db.entities.ExerciseEntity
import com.example.workouttracker.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val userName: String = "",
    val showNamePrompt: Boolean = false,
    val greeting: String = "",
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val recentPRName: String = "",
    val recentPRWeight: String = "",
    val totalWorkouts: Int = 0,
    val totalSets: Int = 0,
    val totalVolume: Double = 0.0,
    val recentSessions: List<WorkoutRepository.SessionSummary> = emptyList(),
    val favoriteExercise: ExerciseEntity? = null,
    val isLoading: Boolean = true
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "RepVault"
    }

    private val repository = (application as RepVaultApp).repository
    private val userPreferences = (application as RepVaultApp).userPreferences

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
        observeUserName()
    }

    private fun observeUserName() {
        viewModelScope.launch {
            userPreferences.userName.collect { name ->
                _uiState.update { it.copy(userName = name, showNamePrompt = name.isBlank()) }
            }
        }
    }

    fun saveUserName(name: String) {
        viewModelScope.launch {
            userPreferences.setUserName(name)
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getAllSessionsFlow(),
                repository.getTotalWorkoutsFlow(),
                repository.getTotalSetsFlow()
            ) { sessions, totalWorkouts, totalSets ->
                Triple(sessions, totalWorkouts, totalSets)
            }.collect { (sessions, totalWorkouts, totalSets) ->
                recalculateStats(sessions, totalWorkouts, totalSets)
            }
        }
    }

    private suspend fun recalculateStats(
        sessions: List<com.example.workouttracker.data.db.entities.WorkoutSessionEntity>,
        totalWorkouts: Int,
        totalSets: Int
    ) {
        val greeting = getGreeting()
        val streak = repository.getCurrentStreak()
        val longestStreak = repository.getLongestStreak()
        val pr = repository.getRecentPR()
        val totalVolume = repository.getTotalVolume()
        val favoriteExercise = repository.getMostFrequentExercise()
        val recentSessions = repository.getRecentSessions(5)

        Log.d(TAG, "Home refreshed: workouts=$totalWorkouts, sets=$totalSets, streak=$streak, longest=${longestStreak}, volume=$totalVolume")

        _uiState.update {
            it.copy(
                greeting = greeting,
                streak = streak,
                longestStreak = longestStreak,
                recentPRName = pr?.first ?: "",
                recentPRWeight = if (pr != null) formatWeight(pr.second) else "",
                totalWorkouts = totalWorkouts,
                totalSets = totalSets,
                totalVolume = totalVolume,
                recentSessions = recentSessions,
                favoriteExercise = favoriteExercise,
                isLoading = false
            )
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    private fun formatWeight(weight: Double): String {
        return if (weight == weight.toLong().toDouble()) {
            "${weight.toLong()} kg"
        } else {
            "${String.format("%.1f", weight)} kg"
        }
    }
}
