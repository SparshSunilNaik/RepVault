package com.example.workouttracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.workouttracker.data.db.entities.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {

    @Insert
    suspend fun insert(set: WorkoutSetEntity): Long

    @Insert
    suspend fun insertAll(sets: List<WorkoutSetEntity>)

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun getSetsForSession(sessionId: Long): List<WorkoutSetEntity>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    fun getSetsForSessionFlow(sessionId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE id = :id")
    suspend fun getSetById(id: Long): WorkoutSetEntity?

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId AND exerciseId = :exerciseId")
    suspend fun getSetsForExerciseInSession(sessionId: Long, exerciseId: Long): List<WorkoutSetEntity>

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_sessions wss ON ws.sessionId = wss.id
        WHERE ws.exerciseId = :exerciseId
        ORDER BY wss.date DESC
        LIMIT :limit
    """)
    suspend fun getRecentSetsForExercise(exerciseId: Long, limit: Int = 100): List<WorkoutSetEntity>

    @Query("SELECT COUNT(*) FROM workout_sets WHERE exerciseId = :exerciseId")
    fun getTotalSetsForExerciseFlow(exerciseId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getTotalSetsForExercise(exerciseId: Long): Int

    @Query("SELECT COUNT(DISTINCT sessionId) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getTotalSessionsForExercise(exerciseId: Long): Int

    @Query("SELECT MAX(weight) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getMaxWeightForExercise(exerciseId: Long): Double?

    @Query("SELECT SUM(weight * reps) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getTotalVolumeForExercise(exerciseId: Long): Double?

    @Query("SELECT AVG(reps) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getAvgRepsForExercise(exerciseId: Long): Double?

    @Query("SELECT MAX(wss.date) FROM workout_sets ws INNER JOIN workout_sessions wss ON ws.sessionId = wss.id WHERE ws.exerciseId = :exerciseId")
    suspend fun getLastTrainedDateForExercise(exerciseId: Long): Long?

    @Query("SELECT SUM(weight * reps) FROM workout_sets")
    suspend fun getTotalVolume(): Double?

    @Query("""
        SELECT exerciseId FROM workout_sets 
        GROUP BY exerciseId 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
    """)
    suspend fun getMostFrequentExerciseId(): Long?

    @Query("UPDATE workout_sets SET weight = :weight WHERE id = :id")
    suspend fun updateWeight(id: Long, weight: Double)

    @Query("UPDATE workout_sets SET reps = :reps WHERE id = :id")
    suspend fun updateReps(id: Long, reps: Int)

    @Query("DELETE FROM workout_sets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM workout_sets WHERE sessionId = :sessionId AND exerciseId = :exerciseId")
    suspend fun deleteByExerciseAndSession(sessionId: Long, exerciseId: Long)

    @Query("DELETE FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)

    @Query("SELECT COUNT(*) FROM workout_sets")
    fun getTotalSetsFlow(): Flow<Int>
}
