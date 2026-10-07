package com.gui.tomato.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.Context
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gui.tomato.MainActivity
import com.gui.tomato.R
import com.gui.tomato.engine.State

class TimerForegroundService : Service() {
    companion object {
        const val TAG = "GuiTimerSvc"
        const val NOTIF_ID = 42
        const val CHANNEL = "gui_timer"
        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, TimerForegroundService::class.java))
        }
        fun stop(ctx: Context) { ctx.stopService(Intent(ctx, TimerForegroundService::class.java)) }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(CHANNEL, "晷 · 番茄计时", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        val snap = (application as com.gui.tomato.GuiApp).repo.current()
        val mmss = String.format("%02d:%02d", snap.remainMs / 60000, (snap.remainMs % 60000) / 1000)
        val stateText = when (snap.state) {
            State.IDLE -> "空闲"
            State.FOCUS -> "专注中"
            State.SHORT_BREAK -> "短休息"
            State.LONG_BREAK -> "长休息"
        }
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle("晷 · $stateText")
            .setContentText("$mmss" + (if (snap.taskId != null) " · 进行中" else ""))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }
}
