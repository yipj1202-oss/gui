package com.gui.tomato.data.image

import android.content.Context
import android.net.Uri
import java.io.File

class ImageStore(private val ctx: Context) {
    private val dir: File get() = File(ctx.filesDir, "images").apply { mkdirs() }

    fun saveUri(taskId: Long, uri: Uri, idx: Int): String {
        val name = "task${taskId}_i${idx}_${System.currentTimeMillis()}.jpg"
        val out = File(dir, name)
        ctx.contentResolver.openInputStream(uri)?.use { input ->
            out.outputStream().use { outw -> input.copyTo(outw) }
        }
        return out.absolutePath
    }

    fun file(path: String): File = File(path)
    fun delete(path: String) { File(path).delete() }
    fun deleteForTask(taskId: Long) {
        dir.listFiles()?.forEach { if (it.name.startsWith("task${taskId}_i")) it.delete() }
    }
}
