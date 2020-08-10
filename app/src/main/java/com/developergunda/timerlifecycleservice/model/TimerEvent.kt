package com.developergunda.timerlifecycleservice.model

sealed class TimerEvent{
    object START : TimerEvent()
    object END : TimerEvent()
}