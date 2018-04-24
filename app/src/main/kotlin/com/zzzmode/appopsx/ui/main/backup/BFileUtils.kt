package com.zzzmode.appopsx.ui.main.backup

import android.content.Context
import android.os.SystemClock
import java.io.File
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.IOException
import java.util.*

/**
 * Created by zl on 2017/5/7.
 */

internal object BFileUtils {

    private val DIR_NAME = "backup"
    private val SUFFIX = ".bak"

    private fun getBackupDir(context: Context): File {
        val externalFilesDir = context.getExternalFilesDir(DIR_NAME)
        if (externalFilesDir != null) {
            if (externalFilesDir.exists()) {
                return externalFilesDir
            } else {
                val mkdirs = externalFilesDir.mkdirs()
                if (mkdirs) {
                    return externalFilesDir
                }
            }
        }
        return context.getDir(DIR_NAME, Context.MODE_PRIVATE)
    }

    private fun generateDefaultFile(context: Context): File {
        val file = File(getBackupDir(context),
                System.currentTimeMillis().toString() + "_" + Random().nextInt(1000) + SUFFIX)
        try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }

    fun getBackFiles(context: Context): List<File> {
        val files = ArrayList<File>()

        arrayOf(context.getExternalFilesDir(DIR_NAME), context.getDir(DIR_NAME, Context.MODE_PRIVATE))
                .mapNotNull { dirFinder(it, FilenameFilter { _, name -> name != null && name.endsWith(SUFFIX) }) }
                .forEach { files.addAll(Arrays.asList(*it)) }
        return files
    }

    private fun dirFinder(dir: File?, filenameFilter: FilenameFilter): Array<File>? {
        return if (dir != null && dir.exists()) {
            dir.listFiles(filenameFilter)
        } else null
    }

    @Throws(IOException::class)
    fun saveBackup(context: Context, config: String): File {
        val file = generateDefaultFile(context)
        file.writeText(config)
        return file
    }

    fun read2String(file: File): String? {
        return file.readText()
    }

    fun deleteBackFile(path: String): Boolean {
        return File(path).delete()
    }

}
