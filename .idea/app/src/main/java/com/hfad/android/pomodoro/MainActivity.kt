package com.hfad.android.pomodoro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfad.android.pomodoro.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), StopwatchListener {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val stopwatches = mutableListOf<Stopwatch>()
    private val stopwatchAdapter = StopwatchAdapter(this)
    private var nextId = 0

    private var playingTimerId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = stopwatchAdapter

        binding.addNewStopwatchButton.setOnClickListener {
            val time = if (binding.editText.text.isNotEmpty())
                binding.editText.text.toString().toLong() * 1000 * 60
                else STANDARD_TIME

            stopwatches.add(Stopwatch(nextId++, time, false))
            stopwatchAdapter.submitList(stopwatches.toList())
        }


    }

    //TODO(): переписать это, так как не очень эффективно создавать всё время новый список (не сработало, так как для sumbitList должная меняться ссылка, а в таком случае она не меняется)
    //Todo но стоит попробовать сделать через notifyItemChanged()
    override fun start(id: Int) {
        val timerToStop = stopwatches.firstOrNull { it.isStarted }
        timerToStop?.let { stop(it.id,it.currentMs) } //останавливаем работающий таймер
        changeStopwatch(id, null, true) // запускаем новый таймер
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

    override fun restart(id: Int, time: Long) {
        changeStopwatch(id, time, false)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { id == it.id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

        private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(id, currentMs ?: it.currentMs, isStarted, it.fullTime))
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }
//        private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean){
//            val timerToChange = stopwatches.first { it.id == id }.id
//            stopwatches[timerToChange].isStarted = isStarted
//            stopwatches[timerToChange].currentMs = currentMs?:0
////            stopwatches[id].fullTime = timerToChange.fullTime
//            stopwatchAdapter.notifyItemChanged(id)
//        }

    companion object{
        private const val STANDARD_TIME = 100L * 60 //5000L * 60
    }

}
