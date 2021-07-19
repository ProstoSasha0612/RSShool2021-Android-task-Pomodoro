package com.hfad.android.pomodoro

interface StopwatchListener {
    fun start(id: Int)

    fun stop(id: Int, currentMs: Long)

    fun restart(id: Int, time: Long)

    fun delete(id: Int)
}