package com.ion606.workoutapp.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


// Reusable input field with dark styling
@Composable
fun InputField(
    value: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    kbt: KeyboardType = KeyboardType.Number,
    enabled: Boolean = true,
    onChange: ((String) -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val inpVal = remember { mutableStateOf(value) }
    val initialValue by remember { mutableStateOf(value) }
    var debouncedValue by remember { mutableStateOf(value) }

    // Debounce logic
    LaunchedEffect(inpVal.value) {
        delay(1000) // 1-second delay
        debouncedValue = inpVal.value
        onChange?.invoke(debouncedValue)
    }

    Column(
        modifier = modifier
    ) {
        if (!label.isNullOrEmpty()) {
            Text(
                text = label,
                style = TextStyle(
                    color = if (isFocused && enabled) Color.White else Color.Gray,
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        BasicTextField(
            value = inpVal.value,
            onValueChange = {
                if (enabled) inpVal.value = it // Update the input value immediately
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable(enabled)
                .onFocusChanged { focusState ->
                    if (enabled) {
                        isFocused = focusState.isFocused
                        if (isFocused && inpVal.value == initialValue) {
                            inpVal.value = ""
                        } else if (!isFocused && inpVal.value == "") {
                            inpVal.value = value
                        }
                    }
                }
                .pointerInput(enabled) {
                    if (enabled) {
                        detectTapGestures(onTap = { focusRequester.requestFocus() })
                    }
                }
                .fillMaxWidth()
                .background(
                    color = if (enabled) {
                        if (isFocused) Color(0xFF444444) else Color(0xFF2A2A2A)
                    } else Color(0xFF1A1A1A),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            maxLines = 1,
            textStyle = TextStyle(
                color = if (enabled) Color.White else Color.Gray,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = kbt),
            enabled = enabled
        )
    }
}


@Composable
fun InputFieldCompact(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    kbt: KeyboardType = KeyboardType.Number,
    enabled: Boolean,
    onChange: ((String) -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val inpVal = remember { mutableStateOf(value) }
    val initialValue by remember { mutableStateOf(value) }
    var debouncedValue by remember { mutableStateOf(value) }

    // Debounce logic
    LaunchedEffect(inpVal.value) {
        delay(1000) // 1-second delay
        debouncedValue = inpVal.value
        onChange?.invoke(debouncedValue)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = if (isFocused && enabled) Color.White else Color.Gray,
                fontSize = 14.sp
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        BasicTextField(
            value = inpVal.value,
            onValueChange = {
                if (enabled) inpVal.value = it // Update the input value immediately
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable(enabled)
                .onFocusChanged { focusState ->
                    if (enabled) {
                        isFocused = focusState.isFocused
                        if (isFocused && inpVal.value == initialValue) {
                            inpVal.value = ""
                        } else if (!isFocused && inpVal.value == "") {
                            inpVal.value = value
                        }
                    }
                }
                .pointerInput(enabled) {
                    if (enabled) {
                        detectTapGestures(onTap = { focusRequester.requestFocus() })
                    }
                }
                .fillMaxHeight(0.7f)
                .background(
                    color = if (enabled) {
                        if (isFocused) Color(0xFF555555) else Color(0xFF333333)
                    } else Color(0xFF1A1A1A),
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            textStyle = TextStyle(
                color = if (enabled) Color.White else Color.Gray,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = kbt),
            enabled = enabled
        )
    }
}
