package com.developergunda.timerlifecycleservice

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.developergunda.timerlifecycleservice.model.TimerEvent
import com.developergunda.timerlifecycleservice.service.TimerService
import com.developergunda.timerlifecycleservice.util.TimerUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fab.setOnClickListener {
            toggleTimer()
        }
        setObservers()
    }

    private fun setObservers() {
        TimerService.timerEvent.observe(this, Observer {
            updateUi(it)
        })

        TimerService.timerInMillis.observe(this, Observer {
            tvTimer.text = TimerUtil.getFormattedTime(it, true)
        })
    }

    private fun updateUi(event: TimerEvent) {
        when (event) {
            is TimerEvent.START -> {
                isTimerRunning = true
                fab.setImageResource(R.drawable.twotone_stop_black_24)
            }
            is TimerEvent.END -> {
                isTimerRunning = false
                fab.setImageResource(R.drawable.twotone_alarm_black_24)
            }
        }
    }

    private fun toggleTimer() {
        if (!isTimerRunning) {
            sendCommandToService(ACTION_START_SERVICE)
        } else {
            sendCommandToService(ACTION_STOP_SERVICE)
        }
    }

    private fun sendCommandToService(action: String) {
        startService(Intent(this, TimerService::class.java).apply {
            this.action = action
        })
    }
}