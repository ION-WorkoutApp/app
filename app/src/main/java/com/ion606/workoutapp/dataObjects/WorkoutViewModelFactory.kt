package com.ion606.workoutapp.dataObjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ion606.workoutapp.screens.activeExercise.SuperSetDao


class WorkoutViewModelFactory(
    private val dao: SuperSetDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            return WorkoutViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
