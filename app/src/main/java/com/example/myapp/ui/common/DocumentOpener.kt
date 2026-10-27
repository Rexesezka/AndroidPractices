package com.example.myapp.ui.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.myapp.domain.model.DownloadedDocument
import java.io.File

/**
 * Открывает скачанный документ системным просмотрщиком/файловым менеджером.
 * Файл отдаётся через FileProvider (content://), как требуют современные Android.
 */
fun openDownloadedDocument(context: Context, document: DownloadedDocument) {
    val file = File(document.filePath)
    if (!file.exists()) {
        Toast.makeText(context, "Файл не найден", Toast.LENGTH_LONG).show()
        return
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, document.mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "На устройстве нет приложения для просмотра ${document.fileName}",
            Toast.LENGTH_LONG,
        ).show()
    }
}
