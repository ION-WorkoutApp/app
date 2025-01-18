package com.ion606.workoutapp.helpers

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow


class Listeners {
    companion object {
        @Composable
        fun LazyColumnWithBottomDetection(
            onBottomReached: () -> Unit,
            content: @Composable (LazyListState) -> Unit
        ) {
            val listState = rememberLazyListState()
            var isBottomReached by remember { mutableStateOf(false) }

            // Monitor the scroll state
            LaunchedEffect(listState) {
                snapshotFlow { listState.layoutInfo }
                    .collect { layoutInfo ->
                        val totalItems = layoutInfo.totalItemsCount
                        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index

                        // Check if the last item is visible and avoid multiple triggers
                        if (lastVisibleItemIndex == totalItems - 1 && !isBottomReached) {
                            isBottomReached = true
                            onBottomReached()
                        } else if (lastVisibleItemIndex != totalItems - 1) {
                            // Reset when not at the bottom (e.g., after loading more items)
                            isBottomReached = false
                        }
                    }
            }

            content(listState)
        }

    }
}