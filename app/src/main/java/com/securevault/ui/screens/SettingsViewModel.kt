package com.securevault.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securevault.data.repository.VaultRepository
import com.securevault.security.CryptoManager
import com.securevault.security.ExportManager
import com.securevault.security.PasswordHasher
import com.securevault.security.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

sealed class SettingsState {
    object Idle : SettingsState()
    object Loading : SettingsState()
    data class Success(val msg: String) : SettingsState()
    data class Error(val msg: String) : SettingsState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val session: SessionManager,
    private val hasher: PasswordHasher,
    private val repo: VaultRepository,
    private val exportMgr: ExportManager,
    private val crypto: CryptoManager
) : ViewModel() {
    private val _state = MutableStateFlow<SettingsState>(SettingsState.Idle)
    val state: StateFlow<SettingsState> = _state

    val isBiometricEnabled get() = session.isBiometricEnabled()

    fun setBiometric(on: Boolean) { session.setBiometricEnabled(on) }

    fun changePassword(cur: String, new: String, confirm: String) {
        if (new.length < 6) { _state.value = SettingsState.Error("Минимум 6 символов"); return }
        if (new != confirm) { _state.value = SettingsState.Error("Пароли не совпадают"); return }
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = SettingsState.Loading
            val hash = session.getMasterHash()
            val ok = hash != null && runCatching { hasher.verify(cur, hash) }.getOrDefault(false)
            if (!ok) { _state.value = SettingsState.Error("Неверный текущий пароль"); return@launch }
            session.saveMasterHash(hasher.hash(new))
            _state.value = SettingsState.Success("Пароль изменён")
        }
    }

    fun export(uri: Uri, pwd: String) {
        if (pwd.length < 4) { _state.value = SettingsState.Error("Минимум 4 символа"); return }
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = SettingsState.Loading
            runCatching {
                val all = repo.getAllSync()
                val arr = JSONArray()
                all.forEach { e ->
                    arr.put(JSONObject().apply {
                        put("t", e.title); put("c", e.category); put("u", e.username)
                        put("p", e.password); put("url", e.url); put("n", e.notes)
                        put("f", e.isFavorite)
                    })
                }
                val encrypted = exportMgr.encrypt(arr.toString(), pwd)
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(encrypted.toByteArray()) }
                _state.value = SettingsState.Success("Экспортировано ${all.size} записей ✓")
            }.onFailure { _state.value = SettingsState.Error("Ошибка: ${it.message}") }
        }
    }

    fun import(uri: Uri, pwd: String) {
        if (pwd.isBlank()) { _state.value = SettingsState.Error("Введите пароль файла"); return }
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = SettingsState.Loading
            runCatching {
                val raw = ctx.contentResolver.openInputStream(uri)
                    ?.use { it.readBytes().toString(Charsets.UTF_8) } ?: error("Не удалось прочитать файл")
                val json = exportMgr.decrypt(raw, pwd) ?: error("Неверный пароль или повреждённый файл")
                val arr = JSONArray(json)
                var count = 0
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    repo.save(0, o.getString("t"), o.getString("c"), o.getString("u"),
                        o.getString("p"), o.optString("url",""), o.optString("n",""),
                        o.optBoolean("f", false))
                    count++
                }
                _state.value = SettingsState.Success("Импортировано $count записей ✓")
            }.onFailure { _state.value = SettingsState.Error("Ошибка: ${it.message}") }
        }
    }

    fun reset() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = SettingsState.Loading
            runCatching { repo.deleteAll(); crypto.deleteKey(); session.reset()
                _state.value = SettingsState.Success("RESET") }
                .onFailure { _state.value = SettingsState.Error("Ошибка сброса") }
        }
    }

    fun clearState() { _state.value = SettingsState.Idle }
}
