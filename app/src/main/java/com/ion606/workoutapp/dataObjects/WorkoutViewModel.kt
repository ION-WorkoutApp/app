package com.ion606.workoutapp.dataObjects

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ion606.workoutapp.screens.activeExercise.SuperSet
import com.ion606.workoutapp.screens.activeExercise.SuperSetDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val TAG = "WorkoutViewModel"

class WorkoutViewModel(private val dao: SuperSetDao) : ViewModel() {
    val supersets = mutableStateListOf<SuperSet>()
    private val draggedExercise = mutableStateOf<ActiveExercise?>(null)
    private val draggedFromSuperset = mutableStateOf<SuperSet?>(null)
    val exerciseBounds = mutableMapOf<String, Rect>()

    // New state to track the current target ExerciseBox ID
    private val _currentSwapTargetId = mutableStateOf<String?>(null)
    val currentSwapTargetId: State<String?> = _currentSwapTargetId
    private var swapTargetJob: Job? = null

    private val _currentExercise = mutableStateOf<ActiveExercise?>(null)
    val currentExercise: State<ActiveExercise?> = _currentExercise

    private val _currentSuperset = mutableStateOf<SuperSet?>(null)
    val currentSuperset: State<SuperSet?> = _currentSuperset

    fun setCurrentExercise(exercise: ActiveExercise, superset: SuperSet) {
        _currentExercise.value = exercise
        _currentSuperset.value = superset
    }

    // Function to clear the current exercise and superset
    fun clearCurrentExercise() {
        _currentExercise.value = null
        _currentSuperset.value = null
    }

    fun setCurrentSwapTargetId(exerciseId: String?) {
        // Cancel any existing job
        swapTargetJob?.cancel()

        // Launch a new job with debounce
        swapTargetJob = viewModelScope.launch {
            delay(100) // 100ms debounce delay
            _currentSwapTargetId.value = exerciseId
        }
    }

    // Initialize supersets from the database
    fun initializeSupersets(fetchedSupersets: List<SuperSet>) {
        viewModelScope.launch(Dispatchers.Main) {
            supersets.clear()
            supersets.addAll(fetchedSupersets)
        }
    }

    fun createNewSuperset(exercise: ActiveExercise) {
        viewModelScope.launch(Dispatchers.IO) {
            // Create a new Superset object
            val newSuperset = SuperSet(
                exercises = SnapshotStateList<ActiveExercise>().apply {
                    add(exercise)
                },
                isDone = false
            )
            dao.insert(newSuperset)

            // Update the local supersets list on the main thread
            withContext(Dispatchers.Main) {
                supersets.add(newSuperset)
            }
        }
    }

    fun removeExerciseFromSuperset(superSet: SuperSet, exercise: ActiveExercise) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!superSet.removeExercise(exercise)) Log.d(
                TAG,
                "Failed to remove exercise ${exercise.id} from superset ${superSet.id}"
            )
            dao.update(superSet)

            if (superSet.exercises.isEmpty()) {
                dao.delete(superSet)
                withContext(Dispatchers.Main) {
                    supersets.remove(superSet)
                }
            } else {
                withContext(Dispatchers.Main) {
                    val index = supersets.indexOfFirst { it.id == superSet.id }
                    if (index != -1) supersets[index] = superSet
                }
            }
        }
    }


    // Start dragging an exercise
    fun startDragging(exercise: ActiveExercise, fromSuperset: SuperSet) {
        if (draggedExercise.value != null) {
            Log.d(
                TAG,
                "Cannot start dragging ${exercise.id}; another exercise is already being dragged."
            )
        } else {
            Log.d(TAG, "Dragging ${exercise.id}")
            draggedExercise.value = exercise
            draggedFromSuperset.value = fromSuperset
        }
    }

    // Move the dragged exercise to another superset
    fun moveExercise(targetSuperset: SuperSet) {
        val exercise = draggedExercise.value ?: return
        val fromSuperset = draggedFromSuperset.value ?: return

        if (fromSuperset.id == targetSuperset.id) {
            stopDragging()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            // Remove from original Superset
            fromSuperset.removeExercise(exercise)
            dao.update(fromSuperset)

            // Add to target Superset
            targetSuperset.addExercise(exercise)
            dao.update(targetSuperset)

            // If original Superset is empty, delete it
            if (fromSuperset.exercises.isEmpty()) {
                dao.delete(fromSuperset)
                viewModelScope.launch(Dispatchers.Main) {
                    supersets.remove(fromSuperset)
                }
            }

            // Update the ViewModel's supersets list on the main thread
            viewModelScope.launch(Dispatchers.Main) {
                val fromIndex = supersets.indexOfFirst { it.id == fromSuperset.id }
                if (fromIndex != -1) {
                    supersets[fromIndex] = fromSuperset
                }

                val targetIndex = supersets.indexOfFirst { it.id == targetSuperset.id }
                if (targetIndex != -1) {
                    supersets[targetIndex] = targetSuperset
                }

                stopDragging()
            }
        }
    }

    fun reorderExercise(superSet: SuperSet, fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val exercise = superSet.exercises.removeAt(fromIndex);
        superSet.addExercise(exercise, toIndex)

        viewModelScope.launch(Dispatchers.IO) {
            dao.update(superSet) // Persist to the database
            viewModelScope.launch(Dispatchers.Main) {
                val index = supersets.indexOfFirst { it.id == superSet.id }
                if (index != -1) {
                    supersets[index] = superSet // Update local state
                }
            }
        }
    }

    // Stop dragging
    fun stopDragging() {
        Log.d(TAG, "Stopped dragging ${draggedExercise.value?.id}")
        exerciseBounds.clear()
        draggedExercise.value = null
        draggedFromSuperset.value = null
    }
}