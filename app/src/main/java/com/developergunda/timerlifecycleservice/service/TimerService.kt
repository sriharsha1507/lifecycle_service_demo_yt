package com.developergunda.timerlifecycleservice.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.developergunda.timerlifecycleservice.*
import com.developergunda.timerlifecycleservice.util.TimerUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class TimerService : LifecycleService() {
    companion object {
        val isTimerRunning = MutableLiveData<Boolean>()
        val timerInMillis = MutableLiveData<Long>()
    }

    private val timerInSeconds = MutableLiveData<Long>()
    private var isServiceStopped = false

    private lateinit var notificationBuilder: NotificationCompat.Builder

    //Timer properties
    private var lapTime = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    override fun onCreate() {
        super.onCreate()
        initValues()

        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.twotone_alarm_black_48)
            .setContentTitle("Timer Lifecycle Service Demo")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_SERVICE -> {
                    Timber.d("start service")
                    startForegroundService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("stop service")
                    stopService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initValues() {
        isTimerRunning.postValue(false)
        timerInMillis.postValue(0L)
        timerInSeconds.postValue(0L)
    }

    private fun startForegroundService() {
        isTimerRunning.postValue(true)
        startTimer()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        timerInMillis.observe(this, Observer {
            if (!isServiceStopped) {
                notificationBuilder.setContentText(
                    TimerUtil.getFormattedTime(it, false)
                )
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        })
    }

    private fun stopService() {
        isServiceStopped = true
        initValues()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
            NOTIFICATION_ID
        )
        stopForeground(true)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
            )

        notificationManager.createNotificationChannel(channel)
    }

    private fun getMainActivityPendingIntent() =
        PendingIntent.getActivity(
            this,
            420,
            Intent(this, MainActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            FLAG_UPDATE_CURRENT
        )

    private fun startTimer() {
        timeStarted = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while (isTimerRunning.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                timerInMillis.postValue(lapTime)
                if (timerInMillis.value!! > lastSecondTimeStamp + 1000L) {
                    timerInSeconds.postValue(timerInMillis.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(50L)
            }
        }
    }
}