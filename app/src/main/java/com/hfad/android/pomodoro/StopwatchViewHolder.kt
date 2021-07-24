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


    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.progressView.setPeriod(stopwatch.fullTime)

//            if (stopwatch.isStarted) {
//                startTimer(stopwatch)
//                setIsRecyclable(false)
//            } else {
//                stopTimer(stopwatch)
//                setIsRecyclable(true)
//            }

        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }

        if (stopwatch.wasStarted) {
            binding.progressView.setCurrent(0)
        }
        if(stopwatch.currentMs == 0L && !stopwatch.isStarted){
            binding.startPauseButton.setImageResource(R.drawable.ic_baseline_restore_24)
            setBackgroundColors(resources.getColor(R.color.red_circle))
        } else setBackgroundColors(resources.getColor(R.color.design_default_color_background))

        initButtonsListeners(stopwatch)

    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {

        binding.startPauseButton.setOnClickListener {
            if (stopwatch.currentMs <= 0L) {
                listener.restart(stopwatch.id, stopwatch.fullTime)
            }

            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }


        binding.deleteButton.setOnClickListener {
            listener.delete(stopwatch.id)
            //may be make geColor that changing with app theme
            setBackgroundColors(resources.getColor(R.color.design_default_color_background))
            binding.startPauseButton.setBackgroundColor(Color.WHITE)
            binding.progressView.isInvisible = true
            stopwatch.currentMs = 0
            stopwatch.isStarted = false
        }

    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.setImageResource(R.drawable.ic_baseline_pause_24)

        timer?.cancel() //cancel() из-за того что rw переиспользует vh (чтобы таймеры не налаживались друг на друга)
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.progressView.setPeriod(stopwatch.fullTime)
        binding.blinkingIndicator.isInvisible = false
        binding.progressView.isVisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()


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
            PERIOD_DAY, UNIT_HUNDRED_MS
        ) {
            val interval = UNIT_HUNDRED_MS

            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs -= interval
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                binding.progressView.setCurrent(stopwatch.currentMs)
                if (stopwatch.currentMs <= 0) {
                    timer?.cancel()
                    onFinish()
                }
            }

            override fun onFinish() {
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                stopTimer(stopwatch)

                setBackgroundColors(resources.getColor(R.color.red_circle))
                binding.startPauseButton.setImageResource(R.drawable.ic_baseline_restore_24)
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

    private fun setBackgroundColors(color: Int) {
        binding.root.setBackgroundColor(color)
        binding.startPauseButton.setBackgroundColor(color)
        binding.deleteButton.setBackgroundColor(color)
    }

    private companion object {

        private const val START_TIME = "00:00:00"
        private const val UNIT_TEN_MS = 10L
        private const val UNIT_HUNDRED_MS = 100L
        private const val PERIOD_DAY = 1000L * 60L * 60L * 24L // Day

        private const val INT = 100L
        private const val PER = 1000L * 30
        private const val REP = 10
    }

}
