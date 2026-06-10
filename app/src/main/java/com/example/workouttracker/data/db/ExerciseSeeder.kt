package com.example.workouttracker.data.db

import com.example.workouttracker.data.db.entities.ExerciseEntity

object ExerciseSeeder {

    data class ExerciseData(val name: String, val muscleGroup: String)

    val exercises: List<ExerciseData> = listOf(
        ExerciseData("Bench Press", "Chest"),
        ExerciseData("Incline Bench Press", "Chest"),
        ExerciseData("Decline Bench Press", "Chest"),
        ExerciseData("Dumbbell Bench Press", "Chest"),
        ExerciseData("Incline Dumbbell Press", "Chest"),
        ExerciseData("Cable Fly", "Chest"),
        ExerciseData("Pec Deck Fly", "Chest"),
        ExerciseData("Push Up", "Chest"),
        ExerciseData("Dips", "Chest"),

        ExerciseData("Overhead Press", "Shoulders"),
        ExerciseData("Arnold Press", "Shoulders"),
        ExerciseData("Lateral Raise", "Shoulders"),
        ExerciseData("Front Raise", "Shoulders"),
        ExerciseData("Face Pull", "Shoulders"),
        ExerciseData("Reverse Fly", "Shoulders"),
        ExerciseData("Shrug", "Shoulders"),

        ExerciseData("Pull Up", "Back"),
        ExerciseData("Lat Pulldown", "Back"),
        ExerciseData("Barbell Row", "Back"),
        ExerciseData("Dumbbell Row", "Back"),
        ExerciseData("T-Bar Row", "Back"),
        ExerciseData("Seated Cable Row", "Back"),
        ExerciseData("Deadlift", "Back"),
        ExerciseData("Hyperextension", "Back"),
        ExerciseData("Good Morning", "Back"),

        ExerciseData("Squat", "Quads"),
        ExerciseData("Front Squat", "Quads"),
        ExerciseData("Leg Press", "Quads"),
        ExerciseData("Bulgarian Split Squat", "Quads"),
        ExerciseData("Leg Extension", "Quads"),
        ExerciseData("Hack Squat", "Quads"),
        ExerciseData("Lunge", "Quads"),

        ExerciseData("Romanian Deadlift", "Hamstrings"),
        ExerciseData("Leg Curl", "Hamstrings"),
        ExerciseData("Nordic Curl", "Hamstrings"),
        ExerciseData("Glute Ham Raise", "Hamstrings"),

        ExerciseData("Hip Thrust", "Glutes"),
        ExerciseData("Glute Bridge", "Glutes"),
        ExerciseData("Cable Kickback", "Glutes"),

        ExerciseData("Standing Calf Raise", "Calves"),
        ExerciseData("Seated Calf Raise", "Calves"),
        ExerciseData("Donkey Calf Raise", "Calves"),

        ExerciseData("Barbell Curl", "Biceps"),
        ExerciseData("Dumbbell Curl", "Biceps"),
        ExerciseData("Hammer Curl", "Biceps"),
        ExerciseData("Preacher Curl", "Biceps"),
        ExerciseData("Cable Curl", "Biceps"),
        ExerciseData("Concentration Curl", "Biceps"),

        ExerciseData("Tricep Pushdown", "Triceps"),
        ExerciseData("Overhead Tricep Extension", "Triceps"),
        ExerciseData("Skull Crusher", "Triceps"),
        ExerciseData("Close Grip Bench Press", "Triceps"),
        ExerciseData("Tricep Kickback", "Triceps"),

        ExerciseData("Crunch", "Abs"),
        ExerciseData("Leg Raise", "Abs"),
        ExerciseData("Plank", "Abs"),
        ExerciseData("Cable Crunch", "Abs"),
        ExerciseData("Russian Twist", "Abs"),
        ExerciseData("Hanging Knee Raise", "Abs"),
        ExerciseData("Ab Wheel Rollout", "Abs"),

        ExerciseData("Wrist Curl", "Forearms"),
        ExerciseData("Reverse Wrist Curl", "Forearms"),
        ExerciseData("Farmer Walk", "Forearms")
    )

    fun toEntities(): List<ExerciseEntity> {
        return exercises.map { it.toEntity() }
    }

    private fun ExerciseData.toEntity(): ExerciseEntity {
        return ExerciseEntity(name = name, muscleGroup = muscleGroup)
    }
}
