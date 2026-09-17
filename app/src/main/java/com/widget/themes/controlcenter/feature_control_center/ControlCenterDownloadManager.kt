package com.widget.themes.controlcenter.feature_control_center

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

object ControlCenterDownloadManager {

    private const val TAG = "CCDownloadManager"

    fun getLocalThemeDir(context: Context, themeId: String): File {
        val cleanId = themeId.replace("/", "_").replace("\\", "_")
        return File(context.filesDir, "control_center/$cleanId")
    }

    fun resolveThemeDir(targetDir: File): File {
        if (File(targetDir, "home_bg.jpg").exists() ||
            File(targetDir, "home_bg.png").exists() ||
            File(targetDir, "home_control_bg.png").exists() ||
            File(targetDir, "control_spec.json").exists()
        ) {
            return targetDir
        }
        val subDirs = targetDir.listFiles()?.filter { it.isDirectory && !it.name.startsWith("__") } ?: emptyList()
        for (sub in subDirs) {
            if (File(sub, "home_bg.jpg").exists() ||
                File(sub, "home_bg.png").exists() ||
                File(sub, "home_control_bg.png").exists() ||
                File(sub, "control_spec.json").exists()
            ) {
                return sub
            }
        }
        return targetDir
    }

    fun isThemeDownloaded(context: Context, themeId: String): Boolean {
        val dir = getLocalThemeDir(context, themeId)
        return dir.exists() && dir.isDirectory && (dir.list()?.isNotEmpty() == true)
    }

    suspend fun downloadAndExtractZip(
        context: Context,
        themeId: String,
        zipUrl: String,
        onProgress: ((Int) -> Unit)? = null
    ): File? = withContext(Dispatchers.IO) {
        val targetDir = getLocalThemeDir(context, themeId)
        if (isThemeDownloaded(context, themeId)) {
            return@withContext resolveThemeDir(targetDir)
        }

        targetDir.mkdirs()
        try {
            val url = URL(zipUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                requestMethod = "GET"
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Download ZIP failed with code: ${connection.responseCode}")
                return@withContext null
            }

            val totalLength = connection.contentLength
            val inputStream = connection.inputStream

            ZipInputStream(inputStream).use { zipIn ->
                var entry = zipIn.nextEntry
                val buffer = ByteArray(8192)
                while (entry != null) {
                    val entryFile = File(targetDir, entry.name)
                    // Security check against Zip Slip vulnerability
                    if (!entryFile.canonicalPath.startsWith(targetDir.canonicalPath)) {
                        throw SecurityException("Zip traversal attempt in ${entry.name}")
                    }

                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        entryFile.parentFile?.mkdirs()
                        FileOutputStream(entryFile).use { out ->
                            var bytesRead: Int
                            while (zipIn.read(buffer).also { bytesRead = it } != -1) {
                                out.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            Log.d(TAG, "Extracted CC package to ${targetDir.absolutePath}")
            resolveThemeDir(targetDir)
        } catch (e: Exception) {
            Log.e(TAG, "Failed downloading/extracting CC ZIP: ${e.message}")
            targetDir.deleteRecursively()
            null
        }
    }
}
