package com.ion606.workoutapp.dataObjects

import com.google.gson.annotations.SerializedName

data class ParsedWorkoutResponse(
    val success: Boolean,
    val workouts: List<ParsedActiveExercise>
)

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

