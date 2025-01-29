package com.ion606.workoutapp.dataObjects

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException


data class UserDataObj(
    val _id: String = "",
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val age: Int = 0,
    val gender: String = "",
    val height: Int = 0,
    val weight: Int = 0,
    val fitnessGoal: String = "",
    val preferredWorkoutType: String = "",
    val comfortLevel: String = "",
    val lastRequestedData: String? = null
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
        other.fitnessGoal,
        other.preferredWorkoutType,
        other.comfortLevel,
        other.lastRequestedData
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
            "fitnessGoal" to fitnessGoal,
            "preferredWorkoutType" to preferredWorkoutType,
            "comfortLevel" to comfortLevel,
            "lastRequestedData" to (lastRequestedData ?: "")
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

        @Override
        override fun toString(): String {
            return Gson().toJson(this)
        }
    }
}


// no sensitive info
data class SanitizedUserDataObj(
    val _id: String = "",
    val email: String = "",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val height: Int = 0,
    val weight: Int = 0,
    val fitnessGoal: String = "",
    val preferredWorkoutType: String = "",
    val comfortLevel: String = "",
    val lastRequestedData: String? = null
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
        other.fitnessGoal,
        other.preferredWorkoutType,
        other.comfortLevel,
        other.lastRequestedData
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
            fitnessGoal,
            preferredWorkoutType,
            comfortLevel,
            lastRequestedData
        )
    }
}