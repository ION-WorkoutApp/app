package com.ion606.workoutapp.helpers

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.PowerManager;

fun isBatteryLow(context: Context): Boolean {
    // create an intent filter for battery changed events
    val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    val batteryStatus = context.registerReceiver(null, ifilter);
    if (batteryStatus != null) {
        // get current battery level and max battery scale
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (scale > 0) {
            // calculate battery percentage
            val batteryPct = level / scale.toFloat();
            // assume battery is low if percentage is 15% or lower
            return batteryPct <= 0.15;
        }
    }
    return false;
};

fun isPowerSaveMode(context: Context): Boolean {
    // get the power manager from system services
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager;
    return pm.isPowerSaveMode;
};
