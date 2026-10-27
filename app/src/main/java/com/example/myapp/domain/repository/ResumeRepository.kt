package com.example.myapp.domain.repository

import com.example.myapp.domain.model.DownloadedDocument

/**
 * Загрузка документов (резюме) из сети в память телефона.
 */
interface ResumeRepository {

    /** Скачать файл по ссылке. Возвращает [Result] — сеть может быть недоступна. */
    suspend fun download(url: String): Result<DownloadedDocument>
}
