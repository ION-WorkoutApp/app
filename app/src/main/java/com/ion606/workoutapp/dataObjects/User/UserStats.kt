package com.ion606.workoutapp.dataObjects.User

import com.google.gson.annotations.SerializedName

data class UserStats(
    @SerializedName("overallActivity") val overallActivity: OverallActivity,
    @SerializedName("timeAndDuration") val timeAndDuration: TimeAndDuration,
    @SerializedName("calories") val calories: Calories,
    @SerializedName("exerciseDistribution") val exerciseDistribution: ExerciseDistribution,
    @SerializedName("performance") val performance: Performance,
    @SerializedName("consistency") val consistency: Consistency,
    @SerializedName("muscleRecovery") val muscleRecovery: MuscleRecovery
)

data class MuscleRecovery(
    @SerializedName("Abdominals") val abdominals: MuscleRecoverIndividual,
    @SerializedName("Adductors") val adductors: MuscleRecoverIndividual,
    @SerializedName("Abductors") val abductors: MuscleRecoverIndividual,
    @SerializedName("Biceps") val biceps: MuscleRecoverIndividual,
    @SerializedName("Triceps") val triceps: MuscleRecoverIndividual,
    @SerializedName("Calves") val calves: MuscleRecoverIndividual,
    @SerializedName("Chest") val chest: MuscleRecoverIndividual,
    @SerializedName("Forearms") val forearms: MuscleRecoverIndividual,
    @SerializedName("Glutes") val glutes: MuscleRecoverIndividual,
    @SerializedName("Hamstrings") val hamstrings: MuscleRecoverIndividual,
    @SerializedName("Lats") val lats: MuscleRecoverIndividual,
    @SerializedName("Lower Back") val lowerBack: MuscleRecoverIndividual,
    @SerializedName("Middle Back") val middleBack: MuscleRecoverIndividual,
    @SerializedName("Traps") val traps: MuscleRecoverIndividual,
    @SerializedName("Neck") val neck: MuscleRecoverIndividual,
    @SerializedName("Quadriceps") val quadriceps: MuscleRecoverIndividual,
    @SerializedName("Shoulders") val shoulders: MuscleRecoverIndividual,
    @SerializedName("lastUpdated") val lastUpdated: String? = null
)

data class MuscleRecoverIndividual(
    @SerializedName("lastUsed") val lastUsed: String,
    @SerializedName("recoveryPercentage") val recoveryPercentage: Float,
    @SerializedName("personalizedRecoveryHours") val personalizedRecoveryHours: Float
)

data class OverallActivity(
    @SerializedName("totalWorkouts") val totalWorkouts: Int,
    // totalVolume in the JSON is null so mark it nullable
    @SerializedName("totalVolume") val totalVolume: Int?,
    @SerializedName("longestStreak") val longestStreak: Int,
    @SerializedName("currentStreak") val currentStreak: Int
)

data class TimeAndDuration(
    @SerializedName("totalWorkoutTime") val totalWorkoutTime: Int,
    @SerializedName("totalDuration") val totalDuration: Int,
    @SerializedName("averageWorkoutTime") val averageWorkoutTime: Double,
    @SerializedName("dailyStats") val dailyStats: List<DailyStat>
)

data class DailyStat(
    @SerializedName("date") val date: String,
    @SerializedName("workoutCount") val workoutCount: Int,
    @SerializedName("totalCalories") val totalCalories: Int,
    @SerializedName("totalWorkoutTime") val totalWorkoutTime: Int,
    @SerializedName("totalDuration") val totalDuration: Int
)

data class Calories(
    @SerializedName("totalCalories") val totalCalories: Int,
    @SerializedName("averageCalories") val averageCalories: Double
)

data class ExerciseDistribution(
    @SerializedName("byType") val byType: Map<String, Int>,
    @SerializedName("byBodyPart") val byBodyPart: Map<String, Int>,
    @SerializedName("topExercises") val topExercises: List<TopExercise>
)

data class TopExercise(
    @SerializedName("title") val title: String, @SerializedName("count") val count: Int
)

data class Performance(
    @SerializedName("totalVolume") val totalVolume: Int,
    @SerializedName("personalBests") val personalBests: List<PersonalBest>
)

data class PersonalBest(
    @SerializedName("title") val title: String,
    @SerializedName("maxWeight") val maxWeight: Int? = null,
    @SerializedName("maxReps") val maxReps: Int? = null
)

data class Consistency(
    @SerializedName("monthlyWorkouts") val monthlyWorkouts: List<MonthlyWorkout>
)

data class MonthlyWorkout(
    @SerializedName("month") val month: String,
    @SerializedName("workoutCount") val workoutCount: Int
)