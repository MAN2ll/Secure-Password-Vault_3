package com.securevault.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Модель записи пароля
 */
@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val service: String,          // Название сервиса (например, "Google")
    val username: String,         // Логин/почта
    val password: String,         // Зашифрованный пароль
    val category: String,         // Категория: "work", "personal", "bank" и т.д.
    val notes: String = "",       // Дополнительные заметки
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val profile: Profile = Profile.PERSONAL,  // Рабочий/Личный профиль
    val lastChanged: Long = System.currentTimeMillis(),  // Для напоминаний о смене
    val changeIntervalDays: Int = 90  // Через сколько дней напомнить сменить
)

/**
 * Профили для разделения: рабочий / личный
 */
enum class Profile {
    PERSONAL,
    WORK
}

/**
 * Простая функция для проверки "силы" пароля (базовая)
 */
fun Entry.isPasswordStrong(): Boolean {
    return password.length >= 12 &&
           password.any { it.isDigit() } &&
           password.any { it.isUpperCase() } &&
           password.any { !it.isLetterOrDigit() }
}
