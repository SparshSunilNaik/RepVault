package com.example.workouttracker.data.repository

import com.example.workouttracker.data.db.AppDatabase
import com.example.workouttracker.data.db.entities.ExerciseEntity
import com.example.workouttracker.data.db.entities.WorkoutSessionEntity
import com.example.workouttracker.data.db.entities.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WorkoutRepository(private val db: AppDatabase) {

    private val exerciseDao = db.exerciseDao()
    private val sessionDao = db.workoutSessionDao()
    private val setDao = db.workoutSetDao()

    suspend fun getOrCreateExercise(name: String, muscleGroup: String = ""): ExerciseEntity {
        val existing = exerciseDao.getByName(name.trim())
        if (existing != null) return existing
        val id = exerciseDao.insert(
            ExerciseEntity(name = name.trim(), muscleGroup = muscleGroup)
        )
        return ExerciseEntity(id = id, name = name.trim(), muscleGroup = muscleGroup)
    }

    suspend fun getAllExercises(): List<ExerciseEntity> {
        return exerciseDao.getAll()
    }

    suspend fun createSession(date: Long = System.currentTimeMillis()): Long {
        return sessionDao.insert(WorkoutSessionEntity(date = date))
    }

    suspend fun getAllSessions(): List<WorkoutSessionEntity> {
        return sessionDao.getAllSessionsList()
    }

    fun getAllSessionsFlow(): Flow<List<WorkoutSessionEntity>> {
        return sessionDao.getAllSessions()
    }

    suspend fun addSet(sessionId: Long, exerciseId: Long, weight: Double, reps: Int) {
        setDao.insert(
            WorkoutSetEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                weight = weight,
                reps = reps
            )
        )
    }

    suspend fun getSetsForSession(sessionId: Long): List<WorkoutSetEntity> {
        return setDao.getSetsForSession(sessionId)
    }

    suspend fun getTotalWorkouts(): Int {
        return sessionDao.getAllSessionsList().size
    }

    suspend fun getTotalSets(): Int {
        val sessions = sessionDao.getAllSessionsList()
        var total = 0
        for (session in sessions) {
            total += setDao.getSetsForSession(session.id).size
        }
        return total
    }

    suspend fun getCurrentStreak(): Int {
        val sessions = sessionDao.getAllSessionsList()
        if (sessions.isEmpty()) return 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = sessions.map { dateFormat.format(Date(it.date)) }
            .distinct()
            .sortedDescending()

        if (dates.isEmpty()) return 0

        val today = dateFormat.format(Date())
        val yesterday = dateFormat.format(Date(System.currentTimeMillis() - 86_400_000L))

        if (dates[0] != today && dates[0] != yesterday) return 0

        var streak = 0
        for (i in dates.indices) {
            val expected = dateFormat.format(Date(System.currentTimeMillis() - i * 86_400_000L))
            if (dates[i] == expected) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    suspend fun getRecentPR(): Pair<String, Double>? {
        val sessions = sessionDao.getAllSessionsList()
        if (sessions.isEmpty()) return null

        var maxWeight = 0.0
        var maxExercise = ""

        for (session in sessions) {
            val sets = setDao.getSetsForSession(session.id)
            for (set in sets) {
                if (set.weight > maxWeight) {
                    maxWeight = set.weight
                    val exercise = exerciseDao.getById(set.exerciseId)
                    maxExercise = exercise?.name ?: "Unknown"
                }
            }
        }

        return if (maxWeight > 0.0) Pair(maxExercise, maxWeight) else null
    }
}
