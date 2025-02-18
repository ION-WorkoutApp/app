package com.ion606.workoutapp.screens.activeExercise

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ion606.workoutapp.R
import com.ion606.workoutapp.dataObjects.ActiveExercise
import com.ion606.workoutapp.dataObjects.Exercise
import com.ion606.workoutapp.dataObjects.ExerciseFilter
import com.ion606.workoutapp.dataObjects.ExerciseMeasureType
import com.ion606.workoutapp.dataObjects.ExercisePickerState
import com.ion606.workoutapp.dataObjects.ExerciseSetDataObj
import com.ion606.workoutapp.dataObjects.SuperSet
import com.ion606.workoutapp.dataObjects.SuperSetDao
import com.ion606.workoutapp.dataObjects.rememberExercisePickerState
import com.ion606.workoutapp.helpers.Listeners.Companion.LazyColumnWithBottomDetection
import com.ion606.workoutapp.helpers.UnitConverters
import com.ion606.workoutapp.helpers.sortBySimilarityOrDefault
import com.ion606.workoutapp.managers.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


private const val TAG = "ExercisePopup"


// TODO: come up with a better way to give rep/set recommendations
fun exercisesToActiveExercises(
    checkedExercises: List<Exercise>, userManager: UserManager
): List<SuperSet> {
    val setsCount = 5
    val inset = (Array(setsCount) { 0 }).mapIndexed() { _, i ->
        ExerciseSetDataObj(i)
    }
    val weightsList = (Array(setsCount) { 0 }).mapIndexed() { _, i ->
        ExerciseSetDataObj(i)
    }

    return checkedExercises.map { checkedExercise ->
        Log.d("ADDING ACTIVE EXERCISE", checkedExercise.toString());

        SuperSet(exercises = SnapshotStateList<ActiveExercise>().apply {
            add(
                UnitConverters.convert(
                    ActiveExercise(
                        exercise = checkedExercise,
                        sets = setsCount,
                        setsDone = 0,
                        inset = inset.map { subset ->
                            if (checkedExercise.measureType == ExerciseMeasureType.DISTANCE_BASED) {
                                subset.copy(id = UUID.randomUUID().toString(), distance = 0)
                            } else subset.copy(id = UUID.randomUUID().toString())
                        }.toMutableList(),
                        weight = weightsList.map { newSuperset ->
                            newSuperset.copy(
                                id = UUID.randomUUID().toString()
                            )
                        }.toMutableList()
                    ), userManager
                )
            )
        })
    }
}


@SuppressLint("NotConstructor")
class ExercisePickerPopup {
    companion object {
        @Composable
        fun CreateSelectionPopup(
            userManager: UserManager,
            exerciseActivities: SnapshotStateList<SuperSet>,
            showSelector: MutableState<Boolean>,
            currentCat: MutableState<String>,
            state: ExercisePickerState = rememberExercisePickerState(), // use the state class
            dao: SuperSetDao
        ) {
            val coroutineScope = rememberCoroutineScope()

            // Fetch categories initially
            LaunchedEffect(Unit) {
                CoroutineScope(Dispatchers.IO).launch {
                    val categoryData = userManager.fetchCategories()
                    state.categories.clear()
                    state.categories.addAll(categoryData?.categories?.sorted() ?: emptyList())
                }
            }

            BackHandler {
                if (currentCat.value.isNotEmpty()) currentCat.value = ""
                else showSelector.value = false
            }

            if ((currentCat.value.isNotEmpty() || state.triggerSearch.value) && !state.isLoading.value && state.shouldLoadMore.value) {
                LaunchedEffect(currentCat.value, state.currentPage.value) {
                    coroutineScope.launch(Dispatchers.IO) {
                        state.isLoading.value = true
                        Log.d(
                            TAG,
                            "Current Page: ${state.currentPage.value}, Total Pages: ${state.totalPages.value}, has next page: ${state.shouldLoadMore.value}"
                        )

                        val response = userManager.fetchExercises(
                            ExerciseFilter(
                                muscleGroup = currentCat.value, term = state.searchQuery.value
                            ), page = state.currentPage.value, pageSize = 30
                        )

                        if (response.exercises.isEmpty()) {
                            state.exercises.clear()
                            state.isLoading.value = false
                            state.isSearching.value = false
                        } else {
                            state.exercises.addAll(
                                response.exercises.sortBySimilarityOrDefault(
                                    state.searchQuery.value
                                )
                            )
                            state.totalPages.value =
                                (response.total + response.pageSize - 1) / response.pageSize
                        }
                        state.isLoading.value = false
                        state.triggerSearch.value = false
                        state.shouldLoadMore.value = false
                        state.currentPage.value += 1
                    }
                }
            }

            // Show the popup dynamically if `showPopup` is true and an exercise is selected
            if (state.showPopup.value && state.selectedExercise.value != null) {
                ExerciseCardPopup(
                    exercise = state.selectedExercise.value!!, showPopup = state.showPopup
                )
            }

            if (showSelector.value) {
                // existing selector screen content
                if (currentCat.value.isEmpty() || state.searchQuery.value.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(Float.MAX_VALUE)
                            .padding(bottom = 20.dp)
                    ) {
                        Button(onClick = {
                            //  Add checked exercises to the activity list
                            val toAdd = exercisesToActiveExercises(
                                state.checkedExercises.toList(), userManager
                            );

                            coroutineScope.launch {
                                dao.insertAll(toAdd)
                            }
                            exerciseActivities += toAdd

                            // Explicitly close the popup here
                            showSelector.value = false
                        },
                            modifier = Modifier
                                .width(100.dp)
                                .height(50.dp)
                                .align(Alignment.BottomCenter),
                            content = { Text("Done") })
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Bar for Exercises
                    TextField(value = state.searchQuery.value,
                        maxLines = 1,
                        onValueChange = {
                            state.searchQuery.value = it
                            state.isSearching.value = true

                            coroutineScope.launch {
                                state.triggerSearch.value = false
                                delay(1500L)

                                Log.d(
                                    TAG,
                                    "Search Query: $it, equal: ${state.searchQuery.value == it}"
                                )

                                // no change == user stopped typing
                                if (state.searchQuery.value == it && state.searchQuery.value.isNotEmpty()) {
                                    state.triggerSearch.value = true
                                    state.shouldLoadMore.value = true
                                    state.currentPage.value = 0
                                }
                            }
                        },
                        label = { Text("Search Exercises") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // Show Categories if no category is selected
                    if (currentCat.value.isEmpty() && state.searchQuery.value.isEmpty()) {
                        AddCategories(state.categories, currentCat, state)
                    } else {
                        // Filtered Exercise List
                        LazyColumnWithBottomDetection(onBottomReached = {
                            Log.d(TAG, "Bottom Reached")

                            state.shouldLoadMore.value =
                                ((state.currentPage.value + 1) < state.totalPages.value - 1 && !state.isLoading.value)
                        }) { listState ->
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                items(state.exercises.filter { exercise ->
                                    state.searchQuery.value.isEmpty() || exercise.title.contains(
                                        state.searchQuery.value, ignoreCase = true
                                    )
                                }) { exercise ->
                                    ExerciseItem(exercise, state.checkedExercises) {
                                        state.selectedExercise.value = it
                                        state.showPopup.value = true
                                    }
                                }
                                item {
                                    val text = when {
                                        state.isSearching.value -> "Loading..."
                                        state.searchQuery.value.isNotEmpty() -> "No exercises found"
                                        else -> "Select a category to view exercises"
                                    }
                                    Text(text)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (state.searchQuery.value.isEmpty()) {
                            // Back Button to Return to Categories
                            Button(
                                onClick = {
                                    currentCat.value = ""
                                }, modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Back to Categories")
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun AddCategories(
            categories: List<String>, currentCat: MutableState<String>, state: ExercisePickerState
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp), // spacing between columns
                verticalArrangement = Arrangement.spacedBy(16.dp)   // spacing between rows
            ) {
                items(categories) { label ->
                    CategoryCard(category = label, onClick = {
                        state.totalPages.value = 0
                        state.currentPage.value = 0
                        currentCat.value = label
                    } // Set selected category
                    )
                }
            }
        }

        @Composable
        fun Float.screenHeight(): Dp {
            val screenHeightDp = LocalConfiguration.current.screenHeightDp
            return (this * screenHeightDp).dp
        }

        @Composable
        fun CategoryCard(category: String, onClick: () -> Unit) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .aspectRatio(1f) // Makes the card square
                    .fillMaxWidth(fraction = 0.3f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White, containerColor = Color.DarkGray
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background), //PLACEHOLDER
                        contentDescription = "Category Image",
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .aspectRatio(1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        text = category,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        @Composable
        fun ExerciseItem(
            exercise: Exercise, checkedExercises: MutableList<Exercise>, onClick: (Exercise) -> Unit
        ) {
            val checked = remember { mutableStateOf(checkedExercises.contains(exercise)) }

            // wrap the checkbox and button in a Row
            Row(
                verticalAlignment = Alignment.CenterVertically, // align items vertically
                modifier = Modifier.fillMaxWidth() // make row take full width
            ) {
                Checkbox(
                    onCheckedChange = {
                        checked.value = it
                        if (it) checkedExercises.add(exercise)
                        else checkedExercises.remove(exercise)
                    }, checked = checked.value, colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF228B22),
                        uncheckedColor = MaterialTheme.colorScheme.onSurface,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp)) // optional spacing between checkbox and button

                Button(
                    onClick = { onClick(exercise) },
                    modifier = Modifier.weight(1f), // make button take up remaining space
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White, containerColor = Color.DarkGray
                    )
                ) {
                    Text(
                        text = exercise.title, style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
