package com.example.workouttracker.domain.model

enum class WorkoutTemplate(
    val displayName: String,
    val exerciseNames: List<String>
) {
    PUSH("Push", listOf(
        "Bench Press",
        "Incline Dumbbell Press",
        "Overhead Press",
        "Lateral Raise",
        "Tricep Pushdown"
    )),
    PULL("Pull", listOf(
        "Pull Up",
        "Barbell Row",
        "Lat Pulldown",
        "Face Pull",
        "Barbell Curl"
    )),
    LEGS("Legs", listOf(
        "Squat",
        "Romanian Deadlift",
        "Leg Press",
        "Leg Extension",
        "Leg Curl",
        "Standing Calf Raise"
    )),
    UPPER("Upper", listOf(
        "Bench Press",
        "Pull Up",
        "Overhead Press",
        "Barbell Row",
        "Lateral Raise",
        "Barbell Curl"
    )),
    LOWER("Lower", listOf(
        "Squat",
        "Romanian Deadlift",
        "Leg Press",
        "Leg Curl",
        "Standing Calf Raise",
        "Hip Thrust"
    )),
    CUSTOM("Custom", emptyList());

    companion object {
        val templateList = entries.toList()
    }
}
