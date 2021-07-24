package com.hfad.android.pomodoro

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean,
    val fullTime:Long = currentMs, //this value for saving progressView progress when user click stop/start
    var wasStarted:Boolean = false
)