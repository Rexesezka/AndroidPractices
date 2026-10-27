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
) {
    /** Профиль не заполнен — на экране показываем приглашение заполнить. */
    val isEmpty: Boolean
        get() = fullName.isBlank() && position.isBlank() && avatarUri.isNullOrBlank() && resumeUrl.isBlank()
}
