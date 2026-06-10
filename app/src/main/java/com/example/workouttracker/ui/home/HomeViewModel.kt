package com.example.workouttracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.RepVaultApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val greeting: String = "",
    val streak: Int = 0,
    val recentPRName: String = "",
    val recentPRWeight: String = "",
    val totalWorkouts: Int = 0,
    val totalSets: Int = 0,
    val isLoading: Boolean = true
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RepVaultApp).repository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val greeting = getGreeting()
            val streak = repository.getCurrentStreak()
            val pr = repository.getRecentPR()
            val totalWorkouts = repository.getTotalWorkouts()
            val totalSets = repository.getTotalSets()

            _uiState.update {
                it.copy(
                    greeting = greeting,
                    streak = streak,
                    recentPRName = pr?.first ?: "",
                    recentPRWeight = if (pr != null) formatWeight(pr.second) else "",
                    totalWorkouts = totalWorkouts,
                    totalSets = totalSets,
                    isLoading = false
                )
            }
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
