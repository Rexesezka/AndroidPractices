package com.example.myapp.core

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Форматирование даты из ISO-строки API в удобочитаемый вид.
 */
object DateTimeFormat {

    private const val INPUT_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX"
    private const val OUTPUT_PATTERN = "dd.MM.yyyy HH:mm"

    fun format(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val input = SimpleDateFormat(INPUT_PATTERN, Locale.US)
            val output = SimpleDateFormat(OUTPUT_PATTERN, Locale.getDefault())
            val parsed = input.parse(raw) ?: return@runCatching raw
            output.format(parsed)
        }.getOrElse { raw }
    }
}
