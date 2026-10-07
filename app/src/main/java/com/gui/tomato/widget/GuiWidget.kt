package com.gui.tomato.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.gui.tomato.GuiApp
import com.gui.tomato.R
import com.gui.tomato.engine.State

class GuiWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) updateOne(context, mgr, id)
    }

    fun updateOne(context: Context, mgr: AppWidgetManager, id: Int) {
        val app = context.applicationContext as GuiApp
        val snap = app.repo.current()
        val stateText = when (snap.state) {
            State.IDLE -> "空闲"
            State.FOCUS -> "专注中"
            State.SHORT_BREAK -> "短休息"
            State.LONG_BREAK -> "长休息"
        }
        val mmss = String.format("%02d:%02d", snap.remainMs / 60000, (snap.remainMs % 60000) / 1000)
        val taskName = snap.taskId?.let { app.db.taskDao().let { d ->
            kotlinx.coroutines.runBlocking { d.getById(it)?.title ?: "" }
        } } ?: ""
        val todayCount = app.todayFocusCountSafe()
        val todayMins = app.todayFocusMinutesSafe()

        val views = RemoteViews(context.packageName, R.layout.widget_gui)
        views.setTextViewText(R.id.w_today, "今日 $todayCount 番茄 · $todayMins 分")
        val status = if (snap.state == State.IDLE) stateText else "$stateText $mmss" + if (taskName.isNotEmpty()) " · $taskName" else ""
        views.setTextViewText(R.id.w_status, status)

        val openIntent = Intent(context, com.gui.tomato.MainActivity::class.java)
        val pi = android.app.PendingIntent.getActivity(context, 0, openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.w_root, pi)

        mgr.updateAppWidget(id, views)
    }

    companion object {
        fun refreshAll(ctx: Context) {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, GuiWidget::class.java))
            val w = GuiWidget()
            for (id in ids) w.updateOne(ctx, mgr, id)
        }
    }
}
