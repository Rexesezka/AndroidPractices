package com.example.myapp.data.repository

import android.content.Context
import com.example.myapp.domain.model.DownloadedDocument
import com.example.myapp.domain.repository.ResumeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/**
 * Скачивание резюме по ссылке через OkHttp в `filesDir/documents`.
 * Готовый файл открывается системным просмотрщиком через FileProvider.
 */
class ResumeRepositoryImpl(
    private val context: Context,
    private val client: OkHttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ResumeRepository {

    override suspend fun download(url: String): Result<DownloadedDocument> = withContext(ioDispatcher) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                // Некоторые сайты отдают файл только «браузерным» клиентам.
                .header("User-Agent", "Mozilla/5.0 (Linux; Android) MyApp/1.0")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("Сервер вернул код ${response.code}")
                val body = response.body ?: error("Пустой ответ сервера")

                val fileName = resolveFileName(url, response.header("Content-Type"))
                val directory = File(context.filesDir, DIR_NAME).apply { mkdirs() }
                val file = File(directory, fileName)

                body.byteStream().use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }

                DownloadedDocument(
                    filePath = file.absolutePath,
                    fileName = fileName,
                    mimeType = resolveMimeType(fileName, response.header("Content-Type")),
                )
            }
        }
    }

    private fun resolveFileName(url: String, contentType: String?): String {
        val fromUrl = url.substringBefore('?').substringAfterLast('/').takeIf { it.contains('.') }
        if (fromUrl != null) return fromUrl

        val extension = when {
            contentType?.contains("pdf", true) == true -> "pdf"
            contentType?.contains("html", true) == true -> "html"
            else -> "pdf"
        }
        return "resume.$extension"
    }

    private fun resolveMimeType(fileName: String, contentType: String?): String {
        val fromHeader = contentType?.substringBefore(';')?.trim()
        if (!fromHeader.isNullOrBlank() && fromHeader != "application/octet-stream") return fromHeader
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "html", "htm" -> "text/html"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "*/*"
        }
    }

    companion object {
        private const val DIR_NAME = "documents"
    }
}
