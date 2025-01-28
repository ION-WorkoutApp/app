package com.ion606.workoutapp.dataObjects

import com.google.gson.annotations.SerializedName
import com.ion606.workoutapp.screens.activeExercise.SuperSet

data class ParsedActiveExercise(
    @SerializedName("_id") val id: String,
    @SerializedName("exercises") val exercises: List<ParsedExercise>,
    @SerializedName("sets") val sets: Int,                // Matches "sets" in JSON
    @SerializedName("setsDone") val setsDone: Int,        // Matches "setsDone" in JSON
    @SerializedName("isDone") val isDone: Boolean,        // Matches "isDone" in JSON
    @SerializedName("reps") val reps: List<ParsedSetData>?,
    @SerializedName("times") val times: List<ParsedSetData>?,
    @SerializedName("weight") val weight: List<ParsedSetData>?,
    @SerializedName("createdAt") val createdAt: String
)

data class ParsedExercise(
    @SerializedName("exerciseId") val exerciseId: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("type") val type: String,
    @SerializedName("bodyPart") val bodyPart: String,
    @SerializedName("equipment") val equipment: String,
    @SerializedName("level") val level: String,
    @SerializedName("rating") val rating: Float,
    @SerializedName("ratingDescription") val ratingDescription: String,
    @SerializedName("videoPath") val videoPath: String,
    @SerializedName("timeBased") val timeBased: Boolean,
    @SerializedName("reps") val reps: List<ParsedSetData>?,
    @SerializedName("times") val times: List<ParsedSetData>?,
    @SerializedName("weight") val weight: List<ParsedSetData>?,
    @SerializedName("sets") val sets: Int?
)

data class ParsedSetData(
    @SerializedName("id") val id: String,
    @SerializedName("isDone") val isDone: Boolean,
    @SerializedName("value") val value: Int,
    @SerializedName("restTime") val restTime: Int = 0
)

data class SavedWorkoutResponse(
    val success: Boolean, val workouts: List<Workout>, val message: String?
)

data class Workout(
    @SerializedName("_id") val id: String,
    @SerializedName("exercises") val exercises: List<Exercise>,
    @SerializedName("supersets") val supersets: List<SuperSet>,
    @SerializedName("totalTime") val totalTime: Int,
    @SerializedName("isSaved") val isSaved: Boolean,
    @SerializedName("workoutName") val workoutName: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("__v") val version: Int
)


fun convertActiveExerciseToParsed(activeExercise: ActiveExercise): ParsedActiveExercise {
    return ParsedActiveExercise(
        id = activeExercise.id,
        exercises = listOf(
            ParsedExercise(
                exerciseId = activeExercise.exercise.exerciseId,
                title = activeExercise.exercise.title,
                description = activeExercise.exercise.description,
                type = activeExercise.exercise.type,
                bodyPart = activeExercise.exercise.bodyPart,
                equipment = activeExercise.exercise.equipment,
                level = activeExercise.exercise.level,
                rating = activeExercise.exercise.rating,
                ratingDescription = activeExercise.exercise.ratingDescription,
                videoPath = activeExercise.exercise.videoPath,
                timeBased = activeExercise.exercise.timeBased,
                reps = activeExercise.reps?.map {
                    ParsedSetData(
                        id = it.id,
                        isDone = it.isDone,
                        value = it.value,
                        restTime = it.restTime
                    )
                },
                times = activeExercise.times?.map {
                    ParsedSetData(
                        id = it.id,
                        isDone = it.isDone,
                        value = it.value,
                        restTime = it.restTime
                    )
                },
                weight = activeExercise.weight?.map {
                    ParsedSetData(
                        id = it.id,
                        isDone = it.isDone,
                        value = it.value,
                        restTime = it.restTime
                    )
                },
                sets = activeExercise.sets
            )
        ),
        sets = activeExercise.sets,
        setsDone = activeExercise.setsDone,
        isDone = activeExercise.isDone,
        reps = activeExercise.reps?.map {
            ParsedSetData(
                id = it.id,
                isDone = it.isDone,
                value = it.value,
                restTime = it.restTime
            )
        },
        times = activeExercise.times?.map {
            ParsedSetData(
                id = it.id,
                isDone = it.isDone,
                value = it.value,
                restTime = it.restTime
            )
        },
        weight = activeExercise.weight?.map {
            ParsedSetData(
                id = it.id,
                isDone = it.isDone,
                value = it.value,
                restTime = it.restTime
            )
        },
        createdAt = "" // Placeholder, as `ActiveExercise` does not have a `createdAt` field
    )
}

fun convertSupersetsToParsed(supersets: List<SuperSet>): List<ParsedActiveExercise> {
    return supersets.flatMap { superset ->
        superset.exercises.map { convertActiveExerciseToParsed(it) }
    }
}
