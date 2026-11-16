package com.example.myapp.domain.model

/**
 * Скачанный на устройство документ (резюме).
 * [filePath] — путь к файлу во внутреннем хранилище приложения.
 */
data class DownloadedDocument(
    val filePath: String,
    val fileName: String,
    val mimeType: String,
)
