package com.hfad.android.pomodoro

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean
) {
     val time:Long = currentMs
}