package com.gui.tomato.data

import com.gui.tomato.data.dao.SessionDao
import com.gui.tomato.data.dao.TaskDao

data class TaskStatRow(val taskId: Long?, val taskName: String, val focusMin: Int, val pomodoros: Int)
data class HourStatRow(val hour: Int, val focusMin: Int)

class StatsRepo(private val sessionDao: SessionDao, private val taskDao: TaskDao) {
    suspend fun byTask(from: Long, to: Long): List<TaskStatRow> {
        val focus = sessionDao.focusInRange(from, to)
        val byTask = focus.groupBy { it.taskId }
        val out = mutableListOf<TaskStatRow>()
        for ((tid, list) in byTask) {
            val name = if (tid == null) "未指定" else (taskDao.getById(tid)?.title ?: "已删除")
            val min = list.sumOf { it.completedMin.toLong() }.toInt()
            out += TaskStatRow(tid, name, min, list.size)
        }
        return out.sortedByDescending { it.focusMin }
    }

    suspend fun byHour(from: Long, to: Long): List<HourStatRow> {
        val focus = sessionDao.focusInRange(from, to)
        val buckets = IntArray(24)
        val count = IntArray(24)
        for (s in focus) {
            val h = java.util.Calendar.getInstance().apply { timeInMillis = s.startedAt }.get(java.util.Calendar.HOUR_OF_DAY)
            buckets[h] += s.completedMin
            count[h]++
        }
        return (0 until 24).map { HourStatRow(it, buckets[it]) }
    }
}
