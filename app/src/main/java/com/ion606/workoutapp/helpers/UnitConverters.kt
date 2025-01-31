package com.ion606.workoutapp.helpers

import com.ion606.workoutapp.dataObjects.ActiveExercise
import com.ion606.workoutapp.dataObjects.ExerciseMeasureType
import com.ion606.workoutapp.managers.UserManager



class UnitConverters {
    companion object {
        private const val KG_TO_LBS = 2.20462
        private const val LBS_TO_KG = 0.453592
        private const val KM_TO_MILES = 0.621371
        private const val MILES_TO_KM = 1.60934

        fun convert(exercise: ActiveExercise, userManager: UserManager): ActiveExercise {
            return exercise
                .convertDistanceToUserUnit(userManager)
                .convertWeightToUserUnit(userManager)
        }

        fun convertBack(exercise: ActiveExercise, userManager: UserManager): ActiveExercise {
            return exercise
                .convertDistanceBackToOriginalUnit(userManager)
                .convertWeightBackToOriginalUnit(userManager)
        }

        // Extension function to convert distance to user preference
        private fun ActiveExercise.convertDistanceToUserUnit(userManager: UserManager): ActiveExercise {
            val userData = userManager.getUserData()
            if (userData?.distanceUnit == "km" || this.exercise.measureType != ExerciseMeasureType.DISTANCE_BASED) {
                return this
            }

            this.inset?.forEachIndexed { index, distance ->
                val convertedValue = (distance.value * KM_TO_MILES).toInt()
                this.inset!![index] = distance.copy(value = convertedValue)
            }
            return this
        }

        // Extension function to convert weight to user preference
        private fun ActiveExercise.convertWeightToUserUnit(userManager: UserManager): ActiveExercise {
            val userData = userManager.getUserData()
            if (userData?.weightUnit == "kg" || this.weight.isNullOrEmpty()) {
                return this
            }

            this.weight?.forEachIndexed { index, weight ->
                val convertedValue = (weight.value * KG_TO_LBS).toInt()
                this.weight!![index] = weight.copy(value = convertedValue)
            }
            return this
        }

        // Extension function to convert distance back to original unit
        private fun ActiveExercise.convertDistanceBackToOriginalUnit(userManager: UserManager): ActiveExercise {
            val userData = userManager.getUserData()
            if (userData?.distanceUnit != "km" || this.exercise.measureType != ExerciseMeasureType.DISTANCE_BASED) {
                return this
            }

            this.inset?.forEachIndexed { index, distance ->
                val convertedValue = (distance.value * MILES_TO_KM).toInt()
                this.inset!![index] = distance.copy(value = convertedValue)
            }
            return this
        }

        // Extension function to convert weight back to original unit
        private fun ActiveExercise.convertWeightBackToOriginalUnit(userManager: UserManager): ActiveExercise {
            val userData = userManager.getUserData()
            if (userData?.weightUnit != "kg" || this.weight.isNullOrEmpty()) {
                return this
            }

            this.weight?.forEachIndexed { index, weight ->
                val convertedValue = (weight.value * LBS_TO_KG).toInt()
                this.weight!![index] = weight.copy(value = convertedValue)
            }
            return this
        }
    }
}
