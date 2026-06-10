package com.example.workouttracker.ui.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.RepVaultApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userName: String = "",
    val totalWorkouts: Int = 0,
    val totalSets: Int = 0,
    val bestLiftName: String = "",
    val bestLiftWeight: String = "",
    val currentStreak: Int = 0,
    val isLoading: Boolean = true
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "RepVault"
    }

    private val repository = (application as RepVaultApp).repository
    private val userPreferences = (application as RepVaultApp).userPreferences

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeData()
        observeUserName()
    }

    private fun observeUserName() {
        viewModelScope.launch {
            userPreferences.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getTotalWorkoutsFlow(),
                repository.getTotalSetsFlow(),
                repository.getAllSessionsFlow()
            ) { totalWorkouts, totalSets, _ ->
                Pair(totalWorkouts, totalSets)
            }.collect { (totalWorkouts, totalSets) ->
                recalculateStats(totalWorkouts, totalSets)
            }
        }
    }

    private suspend fun recalculateStats(totalWorkouts: Int, totalSets: Int) {
        val streak = repository.getCurrentStreak()
        val pr = repository.getRecentPR()
        val bestLiftName = pr?.first ?: ""
        val bestLiftWeight = if (pr != null) formatWeight(pr.second) else ""

        Log.d(TAG, "Profile refreshed: workouts=$totalWorkouts, sets=$totalSets, streak=$streak, best=${pr?.first}")

        _uiState.update {
            it.copy(
                totalWorkouts = totalWorkouts,
                totalSets = totalSets,
                bestLiftName = bestLiftName,
                bestLiftWeight = bestLiftWeight,
                currentStreak = streak,
                isLoading = false
            )
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
