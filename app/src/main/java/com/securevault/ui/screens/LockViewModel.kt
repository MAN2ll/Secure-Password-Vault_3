package com.securevault.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securevault.security.PasswordHasher
import com.securevault.security.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LockState {
    object Idle : LockState()
    object Loading : LockState()
    object Success : LockState()
    data class Error(val msg: String) : LockState()
}

@HiltViewModel
class LockViewModel @Inject constructor(
    private val session: SessionManager,
    private val hasher: PasswordHasher
) : ViewModel() {
    private val _state = MutableStateFlow<LockState>(LockState.Idle)
    val state: StateFlow<LockState> = _state

    val isSetupDone get() = session.isSetupDone()
    val isBiometricEnabled get() = session.isBiometricEnabled()

    fun setup(pwd: String, confirm: String) {
        if (pwd.length < 6) { _state.value = LockState.Error("Минимум 6 символов"); return }
        if (pwd != confirm) { _state.value = LockState.Error("Пароли не совпадают"); return }
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LockState.Loading
            runCatching { hasher.hash(pwd) }
                .onSuccess { h -> session.saveMasterHash(h); session.unlock(); _state.value = LockState.Success }
                .onFailure { _state.value = LockState.Error("Ошибка") }
        }
    }

    fun unlock(pwd: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LockState.Loading
            val hash = session.getMasterHash()
            if (hash != null && runCatching { hasher.verify(pwd, hash) }.getOrDefault(false)) {
                session.unlock(); _state.value = LockState.Success
            } else {
                _state.value = LockState.Error("Неверный пароль")
            }
        }
    }

    fun unlockBio() { session.unlock(); _state.value = LockState.Success }
    fun clearError() { if (_state.value is LockState.Error) _state.value = LockState.Idle }
}
