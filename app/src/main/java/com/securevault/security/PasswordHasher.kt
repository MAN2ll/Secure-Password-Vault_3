package com.securevault.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хеширование паролей через PBKDF2WithHmacSHA256 — стандарт OWASP 2024.
 * 310 000 итераций, 256-битный хеш, 16-байтная соль.
 */
@Singleton
class PasswordHasher @Inject constructor() {
    companion object {
        private const val ITERATIONS = 310_000
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 16
        private const val ALGO = "PBKDF2WithHmacSHA256"
        private const val SEPARATOR = ":"
    }

    private val rng = SecureRandom()

    /** Возвращает строку вида "base64(salt):base64(hash)" */
    fun hash(password: String): String {
        val salt = ByteArray(SALT_LENGTH).also { rng.nextBytes(it) }
        val hash = derive(password.toCharArray(), salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP) + SEPARATOR + Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    /** Проверяет пароль против сохранённой строки */
    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(SEPARATOR)
        if (parts.size != 2) return false
        val salt = Base64.decode(parts[0], Base64.NO_WRAP)
        val expected = Base64.decode(parts[1], Base64.NO_WRAP)
        val actual = derive(password.toCharArray(), salt)
        if (actual.size != expected.size) return false
        var diff = 0
        for (i in actual.indices) diff = diff or (actual[i].toInt() xor expected[i].toInt())
        return diff == 0
    }

    private fun derive(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        return try { SecretKeyFactory.getInstance(ALGO).generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }
}
