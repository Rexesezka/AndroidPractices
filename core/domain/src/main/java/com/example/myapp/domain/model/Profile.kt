package com.example.myapp.domain.model

/**
 * Профиль пользователя. Небольшая типизированная структура, поэтому
 * хранится в одном JSON-файле во внутреннем хранилище приложения
 * (см. data/local/profile/ProfileFileDataSource).
 */
data class Profile(
    val fullName: String = "",
    val position: String = "",
    /** URI аватарки на устройстве (файл во внутреннем хранилище приложения). */
    val avatarUri: String? = null,
    /** Ссылка на резюме/портфолио (например, PDF в сети). */
    val resumeUrl: String = "",
    /** Время любимой пары в формате HH:mm — для напоминания. */
    val lessonTime: String = "",
) {
    /** Профиль не заполнен — на экране показываем приглашение заполнить. */
    val isEmpty: Boolean
        get() = fullName.isBlank() && position.isBlank() && avatarUri.isNullOrBlank() &&
            resumeUrl.isBlank() && lessonTime.isBlank()

    companion object {
        /** Время в 24-часовом формате, например 09:30 или 23:05. */
        private val TIME_REGEX = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")

        fun isValidTime(value: String): Boolean = TIME_REGEX.matches(value)
    }
}
