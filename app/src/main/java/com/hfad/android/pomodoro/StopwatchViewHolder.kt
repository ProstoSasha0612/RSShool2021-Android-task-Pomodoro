package com.hfad.android.pomodoro

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.hfad.android.pomodoro.databinding.StopwatchItemBinding
import kotlinx.coroutines.*


class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null


    //в stopwatchtimer добавить current, которыыйы буду использовать в цикле while
    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.progressView.setPeriod(stopwatch.time)
        binding.progressView.isVisible = false

        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }
        initButtonsListeners(stopwatch)


    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.restartButton.setOnClickListener {
            listener.restart(stopwatch.id, stopwatch.time)
            setBackgroundColors(resources.getColor(R.color.design_default_color_background))
        }

        binding.deleteButton.setOnClickListener {
            listener.delete(stopwatch.id)
            //mey be make geColor that changing with app theme
            setBackgroundColors(resources.getColor(R.color.design_default_color_background))
            binding.startPauseButton.setBackgroundColor(Color.WHITE)
            stopwatch.currentMs = 0
            stopwatch.isStarted =false
        }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.setImageResource(R.drawable.ic_baseline_pause_24)

        timer?.cancel() //cancel() из-за того что rw переиспользует vh (чтобы таймеры не налаживались друг на друга)
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        binding.progressView.isVisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()

    //m b not so and delete
//        binding.progressView.setPeriod(stopwatch.time)        commented 19 07 23:23
//        GlobalScope.launch {
//            while (current < PER) {
//                if(stopwatch.isStarted) {
//                    current += INT
//                    binding.progressView.setCurrent(current)
//                    delay(INT)
//                }
//            }
//        }
//        GlobalScope.launch {
//            var current = stopwatch.currentMs
//            while (stopwatch.currentMs >= 0 && stopwatch.isStarted) {
//                current -= INT
//                binding.progressView.setCurrent(current)
//                delay(INT)
//            }
//        }
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        stopwatch.isStarted = false

    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(
            stopwatch.time, UNIT_HUNDRED_MS
        ) {
            val interval = UNIT_HUNDRED_MS

            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs -= interval
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                binding.progressView.setCurrent(stopwatch.currentMs)
            }

            override fun onFinish() {
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                stopTimer(stopwatch)
                //TODO Fix it (color saving fon new holders)
                setBackgroundColors(resources.getColor(R.color.red_circle))
            }
        }
    }

    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) "$count" else "0$count"
    }

    fun setBackgroundColors(color: Int){
        binding.root.setBackgroundColor(color)
        binding.startPauseButton.setBackgroundColor(color)
        binding.restartButton.setBackgroundColor(color)
        binding.deleteButton.setBackgroundColor(color)
    }

    private companion object {

        private const val START_TIME = "00:00:00"
        private const val UNIT_TEN_MS = 10L
        private const val UNIT_HUNDRED_MS = 100L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day

        private const val INT = 100L
        private const val PER = 1000L * 30
        private const val REP = 10
    }

}