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
                if (onChange != null) onChange(it)
                if (enabled) inpVal.value = it
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable(enabled) // Enable focus only if enabled
                .onFocusChanged { focusState ->
                    if (enabled) {
                        isFocused = focusState.isFocused
                        if (isFocused && inpVal.value == value) {
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
                    } else Color(0xFF1A1A1A), // Dim background if disabled
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            textStyle = TextStyle(
                color = if (enabled) Color.White else Color.Gray, // Dim text color if disabled
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = kbt),
            enabled = enabled // Disables input if not enabled
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
                if (enabled) {
                    inpVal.value = it
                    onChange?.invoke(it)
                }
            }, // Allow text change only if enabled
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable(enabled) // Enable focus only if enabled
                .onFocusChanged { focusState ->
                    if (enabled) {
                        isFocused = focusState.isFocused
                        if (isFocused && inpVal.value == value) {
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
                    } else Color(0xFF1A1A1A), // Dim background if disabled
                    shape = MaterialTheme.shapes.small // Smaller corner radius
                )
                .padding(horizontal = 8.dp, vertical = 8.dp), // Reduce padding
            textStyle = TextStyle(
                color = if (enabled) Color.White else Color.Gray, // Dim text color if disabled
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = kbt),
            enabled = enabled // Disables input if not enabled
        )
    }
}

