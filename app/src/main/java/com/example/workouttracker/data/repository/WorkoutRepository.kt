package com.example.workouttracker.data.repository

import android.util.Log
import com.example.workouttracker.data.db.AppDatabase
import com.example.workouttracker.data.db.ExerciseSeeder
import com.example.workouttracker.data.db.entities.ExerciseEntity
import com.example.workouttracker.data.db.entities.WorkoutSessionEntity
import com.example.workouttracker.data.db.entities.WorkoutSetEntity
import com.example.workouttracker.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WorkoutRepository(private val db: AppDatabase) {

    companion object {
        private const val TAG = "RepVault"
    }

    private val exerciseDao = db.exerciseDao()
    private val sessionDao = db.workoutSessionDao()
    private val setDao = db.workoutSetDao()

    suspend fun seedExercisesIfEmpty() {
        if (exerciseDao.count() == 0) {
            Log.d(TAG, "Seeding exercise catalog (${ExerciseSeeder.exercises.size} exercises)...")
            exerciseDao.insertAll(ExerciseSeeder.toEntities())
            Log.d(TAG, "Seeding complete: ${exerciseDao.count()} exercises")
        }
    }

    suspend fun getOrCreateExercise(name: String, muscleGroup: String = ""): ExerciseEntity {
        val existing = exerciseDao.getByName(name.trim())
        if (existing != null) return existing
        val id = exerciseDao.insert(
            ExerciseEntity(name = name.trim(), muscleGroup = muscleGroup)
        )
        Log.d(TAG, "Created exercise: id=$id, name=$name")
        return ExerciseEntity(id = id, name = name.trim(), muscleGroup = muscleGroup)
    }

    suspend fun getAllExercises(): List<ExerciseEntity> {
        return exerciseDao.getAll()
    }

    suspend fun searchExercises(query: String): List<ExerciseEntity> {
        return exerciseDao.searchByName(query.trim())
    }

    suspend fun getExercisesByTemplate(template: WorkoutTemplate): List<ExerciseEntity> {
        return template.exerciseNames.mapNotNull { name ->
            exerciseDao.getByName(name)
        }
    }

    // Session operations

    suspend fun createSession(date: Long = System.currentTimeMillis(), workoutType: String = ""): Long {
        val id = sessionDao.insert(WorkoutSessionEntity(date = date, workoutType = workoutType))
        Log.d(TAG, "Created session: id=$id, date=$date, type=$workoutType")
        return id
    }

    suspend fun getSessionById(id: Long): WorkoutSessionEntity? {
        return sessionDao.getById(id)
    }

    suspend fun updateWorkoutType(id: Long, workoutType: String) {
        sessionDao.updateWorkoutType(id, workoutType)
    }

    suspend fun deleteSession(id: Long) {
        setDao.deleteBySession(id)
        sessionDao.deleteById(id)
        Log.d(TAG, "Deleted session: id=$id")
    }

    suspend fun getAllSessions(): List<WorkoutSessionEntity> {
        return sessionDao.getAllSessionsList()
    }

    fun getAllSessionsFlow(): Flow<List<WorkoutSessionEntity>> {
        return sessionDao.getAllSessions()
    }

    // Set operations

    suspend fun addSet(sessionId: Long, exerciseId: Long, weight: Double, reps: Int) {
        val id = setDao.insert(
            WorkoutSetEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                weight = weight,
                reps = reps
            )
        )
        Log.d(TAG, "Inserted set: id=$id, sessionId=$sessionId, exerciseId=$exerciseId, weight=$weight, reps=$reps")
    }

    suspend fun insertSets(sets: List<WorkoutSetEntity>) {
        setDao.insertAll(sets)
    }

    suspend fun getSetsForSession(sessionId: Long): List<WorkoutSetEntity> {
        return setDao.getSetsForSession(sessionId)
    }

    suspend fun getSetById(id: Long): WorkoutSetEntity? {
        return setDao.getSetById(id)
    }

    suspend fun updateSetWeight(id: Long, weight: Double) {
        setDao.updateWeight(id, weight)
    }

    suspend fun updateSetReps(id: Long, reps: Int) {
        setDao.updateReps(id, reps)
    }

    suspend fun deleteSet(id: Long) {
        setDao.deleteById(id)
    }

    suspend fun deleteSetsByExerciseAndSession(sessionId: Long, exerciseId: Long) {
        setDao.deleteByExerciseAndSession(sessionId, exerciseId)
    }

    // Stats

    fun getTotalWorkoutsFlow(): Flow<Int> {
        return sessionDao.getTotalSessionsFlow()
    }

    fun getTotalSetsFlow(): Flow<Int> {
        return setDao.getTotalSetsFlow()
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
        Log.d(TAG, "Stats: totalWorkouts=${sessions.size}, totalSets=$total")
        return total
    }

    suspend fun getTotalVolume(): Double {
        return setDao.getTotalVolume() ?: 0.0
    }

    suspend fun getMostFrequentExercise(): ExerciseEntity? {
        val id = setDao.getMostFrequentExerciseId() ?: return null
        return exerciseDao.getById(id)
    }

    // Streak

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
        Log.d(TAG, "Current streak: $streak")
        return streak
    }

    suspend fun getLongestStreak(): Int {
        val sessions = sessionDao.getAllSessionsList()
        if (sessions.isEmpty()) return 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = sessions.map { dateFormat.format(Date(it.date)) }
            .distinct()
            .sorted()

        if (dates.isEmpty()) return 0

        var longest = 1
        var current = 1

        for (i in 1 until dates.size) {
            val prev = dateFormat.parse(dates[i - 1])!!
            val curr = dateFormat.parse(dates[i])!!
            val diff = (curr.time - prev.time) / 86_400_000L
            if (diff == 1L) {
                current++
                longest = maxOf(longest, current)
            } else {
                current = 1
            }
        }
        Log.d(TAG, "Longest streak: $longest")
        return longest
    }

    // PR

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

        val result = if (maxWeight > 0.0) Pair(maxExercise, maxWeight) else null
        Log.d(TAG, "Recent PR: ${result?.first} / ${result?.second} kg")
        return result
    }

    // Sessions grouped with details

    suspend fun getRecentSessions(limit: Int = 5): List<SessionSummary> {
        val sessions = sessionDao.getAllSessionsList()
            .take(limit)
        return sessions.map { session ->
            val sets = setDao.getSetsForSession(session.id)
            SessionSummary(
                sessionId = session.id,
                date = session.date,
                workoutType = session.workoutType,
                setCount = sets.size,
                exerciseNames = sets.mapNotNull { set ->
                    exerciseDao.getById(set.exerciseId)?.name
                }.distinct(),
                totalVolume = sets.sumOf { it.weight * it.reps }
            )
        }
    }

    suspend fun getFullSessionDetail(sessionId: Long): SessionDetail? {
        val session = sessionDao.getById(sessionId) ?: return null
        val sets = setDao.getSetsForSession(sessionId)
        val exerciseSetMap = sets.groupBy { it.exerciseId }
        val exercises = exerciseSetMap.map { (exerciseId, exerciseSets) ->
            val exercise = exerciseDao.getById(exerciseId)
            ExerciseDetail(
                exerciseId = exerciseId,
                exerciseName = exercise?.name ?: "Unknown",
                muscleGroup = exercise?.muscleGroup ?: "",
                sets = exerciseSets.map { it.toSetEntry() },
                volume = exerciseSets.sumOf { it.weight * it.reps }
            )
        }
        return SessionDetail(
            sessionId = session.id,
            date = session.date,
            workoutType = session.workoutType,
            exercises = exercises,
            totalSets = sets.size,
            totalVolume = sets.sumOf { it.weight * it.reps },
            totalExercises = exercises.size
        )
    }

    suspend fun getSessionsGroupedByDate(): List<DateGroup> {
        val sessions = sessionDao.getAllSessionsList()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val grouped = sessions.groupBy {
            dateFormat.format(Date(it.date))
        }
        return grouped.map { (dateString, sessionList) ->
            val date = dateFormat.parse(dateString)!!.time
            val sessionDetails = sessionList.map { session ->
                val sets = setDao.getSetsForSession(session.id)
                val exerciseSetMap = sets.groupBy { it.exerciseId }
                val exercises = exerciseSetMap.map { (exerciseId, exerciseSets) ->
                    val exercise = exerciseDao.getById(exerciseId)
                    ExerciseDetail(
                        exerciseId = exerciseId,
                        exerciseName = exercise?.name ?: "Unknown",
                        muscleGroup = exercise?.muscleGroup ?: "",
                        sets = exerciseSets.map { it.toSetEntry() },
                        volume = exerciseSets.sumOf { it.weight * it.reps }
                    )
                }
                SessionDetail(
                    sessionId = session.id,
                    date = session.date,
                    workoutType = session.workoutType,
                    exercises = exercises,
                    totalSets = sets.size,
                    totalVolume = sets.sumOf { it.weight * it.reps },
                    totalExercises = exercises.size
                )
            }
            DateGroup(date = date, sessions = sessionDetails)
        }.sortedByDescending { it.date }
    }

    // Previous workout for an exercise

    suspend fun getPreviousWorkoutForExercise(exerciseId: Long): PreviousExerciseWorkout? {
        val recentSets = setDao.getRecentSetsForExercise(exerciseId, limit = 1)
        if (recentSets.isEmpty()) return null

        val first = recentSets.first()
        val session = sessionDao.getById(first.sessionId) ?: return null
        val allSetsForExercise = setDao.getSetsForExerciseInSession(first.sessionId, exerciseId)
        return PreviousExerciseWorkout(
            sessionDate = session.date,
            sets = allSetsForExercise.map { SetEntry(it.weight, it.reps) }
        )
    }

    // Exercise stats

    suspend fun getExerciseStats(exerciseId: Long): ExerciseStats {
        val bestWeight = setDao.getMaxWeightForExercise(exerciseId) ?: 0.0
        val totalSets = setDao.getTotalSetsForExercise(exerciseId)
        val totalSessions = setDao.getTotalSessionsForExercise(exerciseId)
        val totalVolume = setDao.getTotalVolumeForExercise(exerciseId) ?: 0.0
        val avgReps = setDao.getAvgRepsForExercise(exerciseId) ?: 0.0
        val lastTrained = setDao.getLastTrainedDateForExercise(exerciseId)
        return ExerciseStats(
            bestWeight = bestWeight,
            totalSessions = totalSessions,
            totalSets = totalSets,
            totalVolume = totalVolume,
            avgReps = avgReps,
            lastTrainedDate = lastTrained ?: 0L
        )
    }

    // Data for heatmap

    suspend fun getWorkoutDatesForMonth(year: Int, month: Int): List<Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        val startDate = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endDate = cal.timeInMillis
        val sessions = sessionDao.getSessionsBetweenDates(startDate, endDate)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sessions.map { dateFormat.parse(dateFormat.format(Date(it.date)))!!.time }
            .distinct()
    }

    // All workout dates

    suspend fun getAllWorkoutDates(): List<Long> {
        val sessions = sessionDao.getAllSessionsList()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sessions.map { dateFormat.parse(dateFormat.format(Date(it.date)))!!.time }
            .distinct()
    }

    // Export/Import

    suspend fun exportAllData(): BackupData {
        val sessions = sessionDao.getAllSessionsList()
        val allExercises = exerciseDao.getAll()
        val allSets = mutableListOf<WorkoutSetEntity>()
        for (session in sessions) {
            allSets.addAll(setDao.getSetsForSession(session.id))
        }
        return BackupData(
            exercises = allExercises,
            sessions = sessions,
            sets = allSets
        )
    }

    suspend fun importData(data: BackupData) {
        for (exercise in data.exercises) {
            exerciseDao.insert(exercise)
        }
        for (session in data.sessions) {
            sessionDao.insert(session)
        }
        for (set in data.sets) {
            setDao.insert(set)
        }
        Log.d(TAG, "Import complete: ${data.exercises.size} exercises, ${data.sessions.size} sessions, ${data.sets.size} sets")
    }

    // Data classes

    data class SessionSummary(
        val sessionId: Long,
        val date: Long,
        val workoutType: String = "",
        val setCount: Int,
        val exerciseNames: List<String>,
        val totalVolume: Double = 0.0
    )

    data class SetEntry(
        val weight: Double,
        val reps: Int,
        val id: Long = 0
    )

    data class ExerciseDetail(
        val exerciseId: Long,
        val exerciseName: String,
        val muscleGroup: String,
        val sets: List<SetEntry>,
        val volume: Double
    )

    data class SessionDetail(
        val sessionId: Long,
        val date: Long,
        val workoutType: String,
        val exercises: List<ExerciseDetail>,
        val totalSets: Int,
        val totalVolume: Double,
        val totalExercises: Int
    )

    data class DateGroup(
        val date: Long,
        val sessions: List<SessionDetail>
    )

    data class PreviousExerciseWorkout(
        val sessionDate: Long,
        val sets: List<SetEntry>
    )

    data class ExerciseStats(
        val bestWeight: Double,
        val totalSessions: Int,
        val totalSets: Int,
        val totalVolume: Double,
        val avgReps: Double,
        val lastTrainedDate: Long
    )

    data class BackupData(
        val exercises: List<ExerciseEntity>,
        val sessions: List<WorkoutSessionEntity>,
        val sets: List<WorkoutSetEntity>
    )
}

private fun WorkoutSetEntity.toSetEntry() = WorkoutRepository.SetEntry(weight, reps, id)
