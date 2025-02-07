package com.ion606.workoutapp.helpers

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

fun generateRandomVibrantColor(): Color {
    // generate a random hue between 0 and 360
    val hue = Random.nextFloat() * 360f
    // choose high saturation and brightness (from 0.7 to 1.0) to avoid dull colors
    val saturation = 0.7f + Random.nextFloat() * 0.3f
    val brightness = 0.7f + Random.nextFloat() * 0.3f

    // convert hsv to rgb using android's hsv-to-color conversion
    val hsv = floatArrayOf(hue, saturation, brightness)
    return Color(android.graphics.Color.HSVToColor(hsv))
}