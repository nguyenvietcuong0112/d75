package com.widget.themes.controlcenter.feature_widget

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.widget.themes.controlcenter.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadDialogFragment : DialogFragment() {

    interface DownloadCallback {
        fun onDownloadComplete(bitmap: Bitmap)
        fun onDownloadFailed()
    }

    private var downloadUrl: String = ""
    private var callback: DownloadCallback? = null

    fun setParams(url: String, callback: DownloadCallback) {
        this.downloadUrl = url
        this.callback = callback
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.fragment_download_dialog, null)
        val progressBar = view.findViewById<ProgressBar>(R.id.pbLoading)
        val tvProgress = view.findViewById<TextView>(R.id.tvProgress)

        progressBar?.isIndeterminate = false
        progressBar?.max = 100

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.i("DownloadDialog", "Starting download from: $downloadUrl")
                val isRemote = downloadUrl.startsWith("http://", ignoreCase = true) || downloadUrl.startsWith("https://", ignoreCase = true)
                val isAsset = downloadUrl.startsWith("file:///android_asset/", ignoreCase = true) || downloadUrl.startsWith("assets_theme/") || downloadUrl.startsWith("assets_collection/") || downloadUrl.startsWith("category/")
                
                val (input, totalSize) = if (isRemote) {
                    val url = URL(downloadUrl)
                    val connection = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15000
                        readTimeout = 15000
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    }
                    connection.connect()
                    if (connection.responseCode !in 200..299) {
                        throw Exception("Server HTTP ${connection.responseCode}")
                    }
                    Pair(connection.inputStream, connection.contentLength.toLong())
                } else if (isAsset) {
                    val assetPath = downloadUrl.removePrefix("file:///android_asset/").removePrefix("/")
                    val stream = requireContext().assets.open(assetPath)
                    Pair(stream, stream.available().toLong())
                } else if (downloadUrl.startsWith("file://", ignoreCase = true)) {
                    val file = java.io.File(downloadUrl.removePrefix("file://"))
                    Pair(java.io.FileInputStream(file), file.length())
                } else {
                    // Fallback try asset first, then URL
                    try {
                        val stream = requireContext().assets.open(downloadUrl.removePrefix("/"))
                        Pair(stream, stream.available().toLong())
                    } catch (e: Exception) {
                        val url = URL(downloadUrl)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.connect()
                        Pair(conn.inputStream, conn.contentLength.toLong())
                    }
                }

                val output = ByteArrayOutputStream()
                val data = ByteArray(4096)
                var downloaded: Long = 0
                var count: Int

                while (input.read(data).also { count = it } != -1) {
                    downloaded += count
                    if (totalSize > 0) {
                        val progress = (downloaded * 100 / totalSize).coerceIn(0, 100).toInt()
                        withContext(Dispatchers.Main) {
                            progressBar?.progress = progress
                            tvProgress?.text = "$progress%"
                        }
                    }
                    output.write(data, 0, count)
                }
                input.close()

                val bytes = output.toByteArray()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                android.util.Log.i("DownloadDialog", "Download finished. Bitmap decoded: ${bitmap != null} (Size: ${bytes.size} bytes)")

                withContext(Dispatchers.Main) {
                    dismissAllowingStateLoss()
                    if (bitmap != null) {
                        callback?.onDownloadComplete(bitmap)
                    } else {
                        callback?.onDownloadFailed()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DownloadDialog", "Download failed for $downloadUrl: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    dismissAllowingStateLoss()
                    callback?.onDownloadFailed()
                }
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()
    }
}
