package com.securevault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    onBiometricRequest: () -> Unit,
    vm: LockViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var pwd by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPwd by remember { mutableStateOf(false) }
    val isSetup = vm.isSetupDone

    LaunchedEffect(state) { if (state is LockState.Success) onUnlocked() }

    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Security, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
            Text("Secure Vault", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(if (isSetup) "Введите мастер-пароль" else "Создайте мастер-пароль",
                fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = pwd, onValueChange = { pwd = it; vm.clearError() },
                label = { Text("Мастер-пароль") },
                visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton({ showPwd = !showPwd }) {
                        Icon(if (showPwd) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            if (!isSetup) {
                OutlinedTextField(
                    value = confirm, onValueChange = { confirm = it; vm.clearError() },
                    label = { Text("Подтвердите пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }

            if (state is LockState.Error)
                Text((state as LockState.Error).msg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)

            Button(
                onClick = { if (isSetup) vm.unlock(pwd) else vm.setup(pwd, confirm) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = state !is LockState.Loading
            ) {
                if (state is LockState.Loading)
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else
                    Text(if (isSetup) "Войти" else "Создать хранилище")
            }

            if (isSetup && vm.isBiometricEnabled) {
                OutlinedButton(onBiometricRequest, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Default.Fingerprint, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Войти по биометрии")
                }
            }

            if (!isSetup)
                Text("AES-256-GCM · Android Keystore · PBKDF2-SHA256",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
