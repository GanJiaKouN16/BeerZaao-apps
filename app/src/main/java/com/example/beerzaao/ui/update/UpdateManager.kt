package com.example.beerzaao.ui.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.beerzaao.data.remote.dto.GitHubRelease
import com.example.beerzaao.data.remote.dto.UpdateInfo
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    data object UpToDate : UpdateState()
    data class Downloading(val progress: Float) : UpdateState()
    data class ReadyToInstall(val apkFile: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class UpdateManager(private val context: Context) {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val updateDir: File
        get() = File(context.cacheDir, "updates").also { it.mkdirs() }

    private val baseApkFile: File
        get() = File(updateDir, "base.apk")

    private val patchFile: File
        get() = File(updateDir, "patch.bin")

    private val outputApkFile: File
        get() = File(updateDir, "update.apk")

    private val repoOwner = "GanJiaKouN16"
    private val repoName = "BeerZaao-apps"

    private val githubApiUrls = listOf(
        "https://api.github.com/repos/$repoOwner/$repoName/releases/latest",
        "https://ghproxy.com/https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
    )

    suspend fun checkForUpdate(): UpdateState = withContext(Dispatchers.IO) {
        try {
            var lastError: Exception? = null
            for (baseUrl in githubApiUrls) {
                try {
                    val request = Request.Builder()
                        .url(baseUrl)
                        .addHeader("Accept", "application/vnd.github.v3+json")
                        .build()
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        lastError = Exception("检查更新失败: ${response.code}")
                        continue
                    }
                    val body = response.body?.string() ?: run {
                        lastError = Exception("响应为空")
                        continue
                    }
                    val release = gson.fromJson(body, GitHubRelease::class.java)

                    val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
                    val latestVersion = release.tagName.removePrefix("v")

                    if (compareVersions(latestVersion, currentVersion) <= 0) {
                        return@withContext UpdateState.UpToDate
                    }

                    val apkAsset = release.assets?.firstOrNull { it.name.endsWith(".apk") }
                    if (apkAsset == null) {
                        lastError = Exception("未找到 APK 文件")
                        continue
                    }

                    val updateInfo = UpdateInfo(
                        version = latestVersion,
                        downloadUrl = apkAsset.downloadUrl,
                        releaseDate = release.publishedAt,
                        notes = release.body,
                        releaseUrl = release.htmlUrl
                    )

                    return@withContext UpdateState.Available(updateInfo)
                } catch (e: Exception) {
                    lastError = e
                }
            }
            UpdateState.Error("检查更新失败: ${lastError?.message}")
        } catch (e: Exception) {
            UpdateState.Error("检查更新失败: ${e.message}")
        }
    }

    suspend fun downloadFullApk(apkUrl: String, onProgress: (Float) -> Unit): Result<File> = withContext(Dispatchers.IO) {
        try {
            downloadFile(apkUrl, outputApkFile, onProgress)
            Result.success(outputApkFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveAsBase(apkFile: File) {
        apkFile.copyTo(baseApkFile, overwrite = true)
    }

    fun getCachedBaseApk(): File? {
        return if (baseApkFile.exists()) baseApkFile else null
    }

    fun installApk(apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
        }

        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun downloadFile(url: String, targetFile: File, onProgress: (Float) -> Unit) {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("下载失败: ${response.code}")

        val body = response.body ?: throw Exception("响应体为空")
        val totalBytes = body.contentLength()
        body.byteStream().use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalRead = 0L
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    if (totalBytes > 0) {
                        onProgress(totalRead.toFloat() / totalBytes)
                    }
                }
            }
        }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val a = parts1.getOrElse(i) { 0 }
            val b = parts2.getOrElse(i) { 0 }
            if (a != b) return a - b
        }
        return 0
    }
}
