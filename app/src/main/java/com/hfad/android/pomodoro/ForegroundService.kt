package com.hfad.android.pomodoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.lang.StringBuilder

class ForegroundService : Service() {
    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private var job: Job? = null

    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro")
            .setGroup("Timer")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_baseline_access_alarms_24)
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            application.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {
                val startTime = intent?.extras?.getLong(STARTED_TIMER_TIME_MS) ?: return
                commandStart(startTime)
            }
            COMMAND_STOP -> commandStop()
            INVALID -> return
        }
    }

    private fun commandStart(startTime: Long) {
        if (isServiceStarted) return
        Log.i("TAG", "commandStart()")
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer(startTime)
        } finally {
            isServiceStarted = true
        }
    }

//    private fun continueTimer(startTime: Long) {
//        job = GlobalScope.launch(Dispatchers.Main) {
//            var multiple = 0
//            while (true) {
//                Log.d("Coroutine", "coroutine is running")
//                notificationManager?.notify(
//                    NOTIFICATION_ID,
//                    getNotification(
//                        (startTime - INTERVAL * multiple++).displayTime().dropLast(3)
//                    )
//                )
//                delay(INTERVAL)
//                if(startTime - INTERVAL * multiple <= 0){
//                    job?.cancel()
//                    notificationManager?.notify(NOTIFICATION_ID,getNotification("Timer ended!"))
//                    break
//                }
//            }
//        }
//    }
private fun continueTimer(startTime: Long) {
    job = GlobalScope.launch(Dispatchers.Main) {
        val time = System.currentTimeMillis() + startTime
        while (true) {
            Log.d("Coroutine", "coroutine is running")
            notificationManager?.notify(
                NOTIFICATION_ID,
                getNotification(
                    (time - System.currentTimeMillis()).displayTime().dropLast(3)
                )
            )
            delay(INTERVAL)
            if(time - System.currentTimeMillis() <= 0){
                job?.cancel()
                notificationManager?.notify(NOTIFICATION_ID,getNotification("Timer ended!"))
                break
            }
        }
    }
}


    private fun commandStop() {
        if (!isServiceStarted) return
        Log.i("TAG", "commandStop()")
        try {
            job?.cancel()
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("TAG", "moveToStartedState(): Running on android O or higher")
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            Log.d("TAG", "moveToStartedState(): Running on android N or lower")
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("content")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(CHANNEL_ID, channelName, importance)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    private companion object {
        private const val CHANNEL_ID = "Channel id"
        private const val NOTIFICATION_ID = 777
        private const val INTERVAL = 1000L

    }
}