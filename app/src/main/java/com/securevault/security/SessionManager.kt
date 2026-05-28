package com.securevault.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext ctx: Context) {
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        ctx, "sv_prefs",
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    @Volatile private var unlocked = false

    fun isSetupDone() = prefs.getBoolean("setup", false)
    fun getMasterHash(): String? = prefs.getString("hash", null)
    fun saveMasterHash(h: String) { prefs.edit().putString("hash", h).putBoolean("setup", true).apply() }
    fun unlock() { unlocked = true }
    fun lock() { unlocked = false }
    fun isUnlocked() = unlocked
    fun isBiometricEnabled() = prefs.getBoolean("bio", false)
    fun setBiometricEnabled(v: Boolean) { prefs.edit().putBoolean("bio", v).apply() }
    fun reset() { prefs.edit().clear().apply(); unlocked = false }
}
