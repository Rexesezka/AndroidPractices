package com.example.myapp.data.local.profile

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Копирует выбранное изображение во внутреннее хранилище приложения.
 *
 * Так URI аватарки остаётся валидным после перезапуска приложения:
 * content:// URI из галереи может потерять доступ, а собственный файл — нет.
 */
class AvatarStorage(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val directory: File
        get() = File(context.filesDir, DIR_NAME).apply { mkdirs() }

    /** Сохраняет изображение и возвращает URI файла (`file://...`). */
    suspend fun saveFromUri(sourceUri: String): String = withContext(ioDispatcher) {
        val uri = Uri.parse(sourceUri)
        val extension = resolveExtension(uri)
        val target = File(directory, "avatar_${System.currentTimeMillis()}.$extension")

        val input = context.contentResolver.openInputStream(uri)
            ?: error("Не удалось открыть выбранное изображение")
        input.use { source ->
            target.outputStream().use { output -> source.copyTo(output) }
        }

        // Старые аватарки и временные снимки больше не нужны.
        directory.listFiles()
            ?.filter {
                it.isFile && it != target &&
                    (it.name.startsWith("avatar_") || it.name.startsWith("capture_"))
            }
            ?.forEach { it.delete() }

        Uri.fromFile(target).toString()
    }

    private fun resolveExtension(uri: Uri): String {
        val mime = context.contentResolver.getType(uri)
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(uri.lastPathSegment?.substringAfterLast('.') ?: "")
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "jpg"
    }

    companion object {
        private const val DIR_NAME = "avatars"
    }
}
