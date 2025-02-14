package com.ion606.workoutapp.helpers

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner

object AppLifecycleObserver : LifecycleEventObserver {
    var isAppInForeground = false
        private set // no external modification

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: androidx.lifecycle.LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> isAppInForeground = true
            Lifecycle.Event.ON_STOP -> isAppInForeground = false
            else -> {}
        }
    }
}
