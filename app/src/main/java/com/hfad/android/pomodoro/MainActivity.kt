package com.hfad.android.pomodoro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfad.android.pomodoro.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val stopwatches = mutableListOf<Stopwatch>()
    private val stopwatchAdapter = StopwatchAdapter(this)
    private var nextId = 0

    private var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this) //new code
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(savedInstanceState != null){
            nextId = savedInstanceState.getInt(NEXT_ID)
            val size = savedInstanceState.getInt(STOPWATCHES_SIZE)
            for(i in 0 until size){
                val id = savedInstanceState.getInt("$ID$i")
                val fullTime = savedInstanceState.getLong("$FULL_TIME$i")
                val currentMs = savedInstanceState.getLong("$CURRENT_MS$i")
                val isStarted = savedInstanceState.getBoolean("$IS_STARTED$i")
                stopwatches.add(Stopwatch(id,currentMs,isStarted,fullTime))
            }
            stopwatchAdapter.submitList(stopwatches)
        }
        //new code
//        startTime = System.currentTimeMillis()
//
//        lifecycleScope.launch (Dispatchers.Main){
//            while (true){
//                binding.stopwatchTimer.text = (System.currentTimeMillis()- startTime).displayTime()
//                delay(INTERVAL)
//            }
//        }


        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = stopwatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {
            val time = if (binding.editText.text.isNotEmpty())
                binding.editText.text.toString().toLong() * 1000 * 60
            else STANDARD_TIME

            if(time != 0L) {
                stopwatches.add(Stopwatch(nextId++, time, false))
                stopwatchAdapter.submitList(stopwatches.toList())
            }
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

    private fun getPlayingStopwatch(): Stopwatch {
        return stopwatches.first { it.isStarted }
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(NEXT_ID,nextId)
        outState.putInt(STOPWATCHES_SIZE,stopwatches.size)
        for(i in stopwatches.indices){
            outState.putLong("$CURRENT_MS$i",stopwatches[i].currentMs)
            outState.putLong("$FULL_TIME$i",stopwatches[i].fullTime)
            outState.putBoolean("$IS_STARTED$i",stopwatches[i].isStarted)
            outState.putInt("$ID$i",stopwatches[i].id)
        }
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground() {
        val startIntent = Intent(this,ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS,getPlayingStopwatch().currentMs) //TODO здесь передавать время включенного секундомера, и внутри его обрабатывать
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground(){
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopwatches.forEach { it.isStarted = false }
        stopwatches.clear()
    }

    companion object{
        private const val STANDARD_TIME = 100L * 60 //5000L * 60
        private const val INTERVAL = 10L
        private const val NEXT_ID = "NEXT ID"
        private const val STOPWATCHES_SIZE = "Stopwatche size"

        private const val CURRENT_MS = "Current ms"
        private const val IS_STARTED = "Is started"
        private const val FULL_TIME = "Full time"
        private const val ID = "Id"
    }

}
