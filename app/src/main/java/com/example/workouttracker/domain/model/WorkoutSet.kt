package com.example.workouttracker.domain.model

data class WorkoutSet(
    val id: Long,
    val sessionId: Long,
    val exerciseId: Long,
    val weight: Double,
    val reps: Int
)
