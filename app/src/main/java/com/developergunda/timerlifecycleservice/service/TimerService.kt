package com.developergunda.timerlifecycleservice.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.developergunda.timerlifecycleservice.*
import com.developergunda.timerlifecycleservice.model.TimerEvent
import com.developergunda.timerlifecycleservice.util.TimerUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : LifecycleService() {
    companion object {
        val timerEvent = MutableLiveData<TimerEvent>()
        val timerInMillis = MutableLiveData<Long>()
    }

    private var isServiceStopped = false

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    //Timer properties
    private var lapTime = 0L
    private var timeStarted = 0L

    override fun onCreate() {
        super.onCreate()
        initValues()
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
        timerEvent.postValue(TimerEvent.END)
        timerInMillis.postValue(0L)
    }

    private fun startForegroundService() {
        timerEvent.postValue(TimerEvent.START)
        startTimer()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
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
    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
            )

        notificationManager.createNotificationChannel(channel)
    }

    private fun startTimer() {
        timeStarted = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while (timerEvent.value!! == TimerEvent.START && !isServiceStopped) {
                lapTime = System.currentTimeMillis() - timeStarted
                timerInMillis.postValue(lapTime)
                delay(50L)
            }
        }
    }
}