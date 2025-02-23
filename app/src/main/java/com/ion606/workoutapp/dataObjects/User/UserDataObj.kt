package com.ion606.workoutapp.dataObjects.User

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

// General Preferences
data class GeneralPreferences(
    val activityLevel: String = "moderate",
    val preferredWorkoutTime: String = "no preference",
    val workoutFrequency: Int = 3,
    val injuriesOrLimitations: List<String> = emptyList(),
    val equipmentAccess: List<String> = emptyList(),
    val preferredWorkoutEnvironment: String = "no preference"
)

// Workout Preferences
data class WorkoutPreferences(
    val preferredWorkoutDuration: Int = 30,
    val exerciseDifficulty: String = "beginner",
    val warmupAndCooldownPreference: Boolean = true,
    val preferredWorkoutMusic: String = "No preference"
)

// Progress Tracking
data class ProgressTracking(
    val stepGoal: Int = 10000,
    val waterIntakeGoal: Int = 2000,
    val sleepTracking: Boolean = false
)

// Notifications & Reminders
data class Notifications(
    val remindersEnabled: Boolean = true,
    val notificationFrequency: String = "daily",
    val preferredReminderTime: String = "08:00 AM"
)

// Social & Gamification Preferences
data class SocialPreferences(
    val socialSharing: Boolean = false,
    val leaderboardParticipation: Boolean = false,
    val badgesAndAchievements: List<String> = emptyList()
)

data class UserDataObj(
    val _id: String = "",
    val email: String = "",
    val name: String = "",
    val password: String = "", // Handle securely
    val age: Int = 0,
    val gender: String = "",
    val height: Float = 0f, // in centimeters
    val weight: Float = 0f, // in kilograms
    val weightUnit: String = "lbs", // Default to 'lbs'
    val distanceUnit: String = "km", // Default to 'km'
    val fitnessGoal: String = "",
    val preferredWorkoutType: String = "",
    val comfortLevel: String = "",
    val lastRequestedData: String? = null,

    // New Preferences
    val generalPreferences: GeneralPreferences = GeneralPreferences(),
    val workoutPreferences: WorkoutPreferences = WorkoutPreferences(),
    val progressTracking: ProgressTracking = ProgressTracking(),
    val notifications: Notifications = Notifications(),
    val socialPreferences: SocialPreferences = SocialPreferences(),

    // Other Fields
    val workouts: List<String> = emptyList(),
    val savedWorkouts: List<String> = emptyList()
) {
    // Copy constructor
    constructor(other: UserDataObj) : this(
        other._id,
        other.email,
        other.name,
        other.password,
        other.age,
        other.gender,
        other.height,
        other.weight,
        other.weightUnit,
        other.distanceUnit,
        other.fitnessGoal,
        other.preferredWorkoutType,
        other.comfortLevel,
        other.lastRequestedData,
        other.generalPreferences,
        other.workoutPreferences,
        other.progressTracking,
        other.notifications,
        other.socialPreferences,
        other.workouts,
        other.savedWorkouts
    )

    fun toMap(): Map<String, Any> {
        return mapOf(
            "_id" to _id,
            "email" to email,
            "name" to name,
            "password" to password,
            "age" to age,
            "gender" to gender,
            "height" to height,
            "weight" to weight,
            "weightUnit" to weightUnit,
            "distanceUnit" to distanceUnit,
            "fitnessGoal" to fitnessGoal,
            "preferredWorkoutType" to preferredWorkoutType,
            "comfortLevel" to comfortLevel,
            "lastRequestedData" to (lastRequestedData ?: ""),
            "generalPreferences" to generalPreferences,
            "workoutPreferences" to workoutPreferences,
            "progressTracking" to progressTracking,
            "notifications" to notifications,
            "socialPreferences" to socialPreferences,
            "workouts" to workouts,
            "savedWorkouts" to savedWorkouts
        )
    }

    companion object {
        fun fromString(s: String): UserDataObj? {
            return try {
                Gson().fromJson(s, UserDataObj::class.java)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                null
            }
        }

        override fun toString(): String {
            return Gson().toJson(this)
        }
    }
}

// SanitizedUserDataObj without password
data class SanitizedUserDataObj(
    val _id: String = "",
    val email: String = "",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val height: Float = 0f,
    val weight: Float = 0f,
    val weightUnit: String = "lbs",
    val distanceUnit: String = "km",
    val fitnessGoal: String = "",
    val preferredWorkoutType: String = "",
    val comfortLevel: String = "",
    val lastRequestedData: String? = null,

    // Include preferences
    val generalPreferences: GeneralPreferences = GeneralPreferences(),
    val workoutPreferences: WorkoutPreferences = WorkoutPreferences(),
    val progressTracking: ProgressTracking = ProgressTracking(),
    val notifications: Notifications = Notifications(),
    val socialPreferences: SocialPreferences = SocialPreferences(),

    // Other Fields
    val workouts: List<String> = emptyList(),
    val savedWorkouts: List<String> = emptyList()
) {
    // "Copy" constructor
    constructor(other: UserDataObj) : this(
        other._id,
        other.email,
        other.name,
        other.age,
        other.gender,
        other.height,
        other.weight,
        other.weightUnit,
        other.distanceUnit,
        other.fitnessGoal,
        other.preferredWorkoutType,
        other.comfortLevel,
        other.lastRequestedData,
        other.generalPreferences,
        other.workoutPreferences,
        other.progressTracking,
        other.notifications,
        other.socialPreferences,
        other.workouts,
        other.savedWorkouts
    )

    fun toUserDataObj(password: String): UserDataObj {
        return UserDataObj(
            _id,
            email,
            name,
            password,
            age,
            gender,
            height,
            weight,
            weightUnit,
            distanceUnit,
            fitnessGoal,
            preferredWorkoutType,
            comfortLevel,
            lastRequestedData,
            generalPreferences,
            workoutPreferences,
            progressTracking,
            notifications,
            socialPreferences,
            workouts,
            savedWorkouts
        )
    }
}
