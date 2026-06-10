package com.example.workouttracker.ui.detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.RepVaultApp
import com.example.workouttracker.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val sessionDetail: WorkoutRepository.SessionDetail? = null,
    val editMode: Boolean = false,
    val isSaving: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val showDeleteExerciseConfirm: Boolean = false,
    val deleteExerciseIndex: Int = -1,
    val showDeleteSetConfirm: Boolean = false,
    val deleteSetId: Long = -1L,
    val isLoading: Boolean = true,
    val deleted: Boolean = false,
    val editableType: String = ""
)

class WorkoutDetailViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "RepVault"
    }

    private val repository = (application as RepVaultApp).repository

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var sessionId: Long = -1L

    fun loadSession(id: Long) {
        sessionId = id
        viewModelScope.launch {
            val detail = repository.getFullSessionDetail(id)
            _uiState.update {
                it.copy(
                    sessionDetail = detail,
                    editableType = detail?.workoutType ?: "",
                    isLoading = false
                )
            }
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(editMode = !it.editMode) }
    }

    fun updateWorkoutType(type: String) {
        _uiState.update { it.copy(editableType = type) }
    }

    fun updateSetWeight(setId: Long, weight: Double) {
        viewModelScope.launch {
            repository.updateSetWeight(setId, weight)
            refreshSession()
        }
    }

    fun updateSetReps(setId: Long, reps: Int) {
        viewModelScope.launch {
            repository.updateSetReps(setId, reps)
            refreshSession()
        }
    }

    fun showDeleteExerciseDialog(exerciseIndex: Int) {
        _uiState.update { it.copy(showDeleteExerciseConfirm = true, deleteExerciseIndex = exerciseIndex) }
    }

    fun dismissDeleteExerciseDialog() {
        _uiState.update { it.copy(showDeleteExerciseConfirm = false, deleteExerciseIndex = -1) }
    }

    fun confirmDeleteExercise() {
        val index = _uiState.value.deleteExerciseIndex
        val detail = _uiState.value.sessionDetail ?: return
        if (index in detail.exercises.indices) {
            val exercise = detail.exercises[index]
            viewModelScope.launch {
                repository.deleteSetsByExerciseAndSession(sessionId, exercise.exerciseId)
                dismissDeleteExerciseDialog()
                refreshSession()
            }
        }
    }

    fun showDeleteSetDialog(setId: Long) {
        _uiState.update { it.copy(showDeleteSetConfirm = true, deleteSetId = setId) }
    }

    fun dismissDeleteSetDialog() {
        _uiState.update { it.copy(showDeleteSetConfirm = false, deleteSetId = -1L) }
    }

    fun confirmDeleteSet() {
        val setId = _uiState.value.deleteSetId
        viewModelScope.launch {
            repository.deleteSet(setId)
            dismissDeleteSetDialog()
            refreshSession()
        }
    }

    fun showDeleteSessionDialog() {
        _uiState.update { it.copy(showDeleteConfirm = true) }
    }

    fun dismissDeleteSessionDialog() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun confirmDeleteSession() {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            _uiState.update { it.copy(showDeleteConfirm = false, deleted = true) }
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            repository.updateWorkoutType(sessionId, _uiState.value.editableType)
            _uiState.update { it.copy(isSaving = false, editMode = false) }
            refreshSession()
        }
    }

    private suspend fun refreshSession() {
        val detail = repository.getFullSessionDetail(sessionId)
        _uiState.update { it.copy(sessionDetail = detail, isLoading = false) }
    }
}
