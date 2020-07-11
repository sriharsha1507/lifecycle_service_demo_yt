package com.developergunda.timerlifecycleservice.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.developergunda.timerlifecycleservice.MainActivity
import com.developergunda.timerlifecycleservice.NOTIFICATION_CHANNEL_ID
import com.developergunda.timerlifecycleservice.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext context: Context
    ) =
        PendingIntent.getActivity(
            context,
            420,
            Intent(context, MainActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )


    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.twotone_alarm_black_48)
        .setContentTitle("Timer Lifecycle Service Demo")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)

    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


}