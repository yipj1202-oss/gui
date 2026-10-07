package com.gui.tomato.data.backup

import android.content.Context
import com.gui.tomato.data.db.AppDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class BackupStore(private val ctx: Context, private val db: AppDatabase) {

    fun export(dest: File): File {
        val dbFile = File(ctx.filesDir, "databases/gui.db")
        val imgDir = File(ctx.filesDir, "images")
        val meta = """{"app":"gui","version":1,"exportedAt":${System.currentTimeMillis()}}"""
        val zos = ZipOutputStream(FileOutputStream(dest))
        zos.use { out ->
            addZip(out, dbFile.name, dbFile.readBytes())
            val files = imgDir.listFiles()
            if (files != null) {
                for (f in files) addZip(out, "images/" + f.name, f.readBytes())
            }
            addZip(out, "meta.json", meta.toByteArray())
        }
        return dest
    }

    private fun addZip(out: ZipOutputStream, name: String, bytes: ByteArray) {
        val e = ZipEntry(name)
        out.putNextEntry(e)
        out.write(bytes)
        out.closeEntry()
    }

    fun import(src: File, overwrite: Boolean = true): String {
        if (!overwrite) return "aborted"
        val zf = ZipFile(src)
        try {
            val dbEntry = zf.getEntry("gui.db")
            if (dbEntry != null) {
                val dbBytes: ByteArray = zf.getInputStream(dbEntry).use { ins: InputStream -> ins.readBytes() }
                val dbFile = File(ctx.filesDir, "databases/gui.db")
                dbFile.parentFile?.mkdirs()
                dbFile.delete()
                dbFile.outputStream().use { fos: java.io.OutputStream -> fos.write(dbBytes) }
            }
            // image entries are namespaced under images/; iterate known tasks
            val imagesDir = File(ctx.filesDir, "images")
            imagesDir.listFiles()?.forEach { imgFile ->
                val entryName = "images/" + imgFile.name
                val entry = zf.getEntry(entryName)
                if (entry != null) {
                    val data: ByteArray = zf.getInputStream(entry).use { ins: InputStream -> ins.readBytes() }
                    imgFile.outputStream().use { fos: java.io.OutputStream -> fos.write(data) }
                }
            }
        } finally {
            zf.close()
        }
        return "ok"
    }
}