package com.ion606.workoutapp.dataObjects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

// there were way too many loose variables in the original code, so I made this
data class ExercisePickerState(
    val categories: SnapshotStateList<String>,
    val exercises: SnapshotStateList<Exercise>,
    val searchQuery: MutableState<String>,
    val checkedExercises: SnapshotStateList<Exercise>,
    val showPopup: MutableState<Boolean>,
    val selectedExercise: MutableState<Exercise?>,
    var currentPage: MutableState<Int>,
    var totalPages: MutableState<Int>,
    val isLoading: MutableState<Boolean>,
    val isSearching: MutableState<Boolean>,
    val triggerSearch: MutableState<Boolean>,
    val shouldLoadMore: MutableState<Boolean>
)

@Composable
fun rememberExercisePickerState(): ExercisePickerState {
    val categories = remember { mutableStateListOf<String>() }
    val exercises = remember { mutableStateListOf<Exercise>() }
    val searchQuery = remember { mutableStateOf("") }
    val checkedExercises = remember { mutableStateListOf<Exercise>() }

    val showPopup = remember { mutableStateOf(false) }
    val selectedExercise = remember { mutableStateOf<Exercise?>(null) }

    val currentPage = remember { mutableStateOf(0) }
    val totalPages = remember { mutableStateOf(1) }
    val isLoading = remember { mutableStateOf(false) }
    val isSearching = remember { mutableStateOf(false) }
    val triggerSearch = remember { mutableStateOf(false) }
    val shouldLoadMore = remember { mutableStateOf(true) }


    return ExercisePickerState(
        categories = categories,
        exercises = exercises,
        searchQuery = searchQuery,
        checkedExercises = checkedExercises,
        showPopup = showPopup,
        selectedExercise = selectedExercise,
        currentPage = currentPage,
        totalPages = totalPages,
        isLoading = isLoading,
        isSearching = isSearching,
        triggerSearch = triggerSearch,
        shouldLoadMore = shouldLoadMore
    )
}
