package com.ion606.workoutapp.dataObjects.User

data class UserStats(
    val overallActivity: OverallActivity,
    val timeAndDuration: TimeAndDuration,
    val calories: Calories,
    val exerciseDistribution: ExerciseDistribution,
    val performance: Performance,
    val consistency: Consistency,
    val muscleRecovery: MuscleRecovery
)

data class MuscleRecoverIndividual(
    val lastUsed: String,
    val recoveryPercentage: Float,
    val personalizedRecoveryHours: Float
)

data class MuscleRecovery(
    val chest: MuscleRecoverIndividual,
    val back: MuscleRecoverIndividual,
    val legs: MuscleRecoverIndividual,
    val arms: MuscleRecoverIndividual,
    val core: MuscleRecoverIndividual,
    val shoulders: MuscleRecoverIndividual,
    val lastUpdated: String
)

data class OverallActivity(
    val totalWorkouts: Int,
    val totalVolume: Int,
    val longestStreak: Int,
    val currentStreak: Int
)

data class TimeAndDuration(
    val totalWorkoutTime: Int,
    val totalDuration: Int,
    val averageWorkoutTime: Double,
    val dailyStats: List<DailyStat>
)

data class DailyStat(
    val date: String,
    val workoutCount: Int,
    val totalCalories: Int,
    val totalWorkoutTime: Int,
    val totalDuration: Int
)

data class Calories(
    val totalCalories: Int,
    val averageCalories: Double
)

data class ExerciseDistribution(
    val byType: Map<String, Int>,
    val byBodyPart: Map<String, Int>,
    val topExercises: List<TopExercise>
)

data class TopExercise(
    val title: String,
    val count: Int
)

data class Performance(
    val totalVolume: Int,
    val personalBests: List<PersonalBest>
)

data class PersonalBest(
    val title: String,
    val maxWeight: Int? = null,
    val maxReps: Int? = null // Include other PB fields as needed
)

data class Consistency(
    val monthlyWorkouts: List<MonthlyWorkout>
)

data class MonthlyWorkout(
    val month: String,
    val workoutCount: Int
)