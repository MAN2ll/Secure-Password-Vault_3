package com.securevault.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onResetDone: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    var bio by remember { mutableStateOf(vm.isBiometricEnabled) }

    var showChangePwd by remember { mutableStateOf(false) }
    var curPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var cfmPwd by remember { mutableStateOf("") }
    var showPwds by remember { mutableStateOf(false) }

    var exportUri by remember { mutableStateOf<Uri?>(null) }
    var importUri by remember { mutableStateOf<Uri?>(null) }
    var showExportDlg by remember { mutableStateOf(false) }
    var showImportDlg by remember { mutableStateOf(false) }
    var exportPwd by remember { mutableStateOf("") }
    var importPwd by remember { mutableStateOf("") }
    var showExpPwd by remember { mutableStateOf(false) }
    var showImpPwd by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        when (val s = state) {
            is SettingsState.Success -> {
                if (s.msg == "RESET") { onResetDone(); return@LaunchedEffect }
                snackbar.showSnackbar(s.msg)
                vm.clearState()
                showChangePwd = false; showExportDlg = false; showImportDlg = false
                curPwd = ""; newPwd = ""; cfmPwd = ""; exportPwd = ""; importPwd = ""
            }
            is SettingsState.Error -> { snackbar.showSnackbar(s.msg); vm.clearState() }
            else -> {}
        }
    }

    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let { exportUri = it; showExportDlg = true }
    }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importUri = it; showImportDlg = true }
    }

    // Change Password Dialog
    if (showChangePwd) {
        AlertDialog(onDismissRequest = { showChangePwd = false },
            title = { Text("Изменить мастер-пароль") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Triple(curPwd, "Текущий пароль", { v: String -> curPwd = v }),
                        Triple(newPwd, "Новый пароль", { v: String -> newPwd = v }),
                        Triple(cfmPwd, "Подтвердите", { v: String -> cfmPwd = v })
                    ).forEach { (value, label, onChange) ->
                        OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) },
                            visualTransformation = if (showPwds) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = { if (label == "Текущий пароль") IconButton({ showPwds = !showPwds }) {
                                Icon(if (showPwds) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                            modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                }
            },
            confirmButton = {
                Button({ vm.changePassword(curPwd, newPwd, cfmPwd) }, enabled = state !is SettingsState.Loading) {
                    if (state is SettingsState.Loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text("Изменить")
                }
            },
            dismissButton = { TextButton({ showChangePwd = false }) { Text("Отмена") } }
        )
    }

    // Export Dialog
    if (showExportDlg && exportUri != null) {
        AlertDialog(onDismissRequest = { showExportDlg = false; exportPwd = "" },
            title = { Text("Пароль для файла резервной копии") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Запомните этот пароль — без него файл не открыть.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(value = exportPwd, onValueChange = { exportPwd = it }, label = { Text("Пароль экспорта") },
                        visualTransformation = if (showExpPwd) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = { IconButton({ showExpPwd = !showExpPwd }) {
                            Icon(if (showExpPwd) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            },
            confirmButton = {
                Button({ exportUri?.let { vm.export(it, exportPwd) } }, enabled = state !is SettingsState.Loading) {
                    if (state is SettingsState.Loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text("Экспортировать")
                }
            },
            dismissButton = { TextButton({ showExportDlg = false; exportPwd = "" }) { Text("Отмена") } }
        )
    }

    // Import Dialog
    if (showImportDlg && importUri != null) {
        AlertDialog(onDismissRequest = { showImportDlg = false; importPwd = "" },
            title = { Text("Пароль файла резервной копии") },
            text = {
                OutlinedTextField(value = importPwd, onValueChange = { importPwd = it }, label = { Text("Пароль файла") },
                    visualTransformation = if (showImpPwd) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = { IconButton({ showImpPwd = !showImpPwd }) {
                        Icon(if (showImpPwd) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
            },
            confirmButton = {
                Button({ importUri?.let { vm.import(it, importPwd) } }, enabled = state !is SettingsState.Loading) {
                    if (state is SettingsState.Loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text("Импортировать")
                }
            },
            dismissButton = { TextButton({ showImportDlg = false; importPwd = "" }) { Text("Отмена") } }
        )
    }

    // Reset Dialog
    if (showReset) {
        AlertDialog(onDismissRequest = { showReset = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Сбросить хранилище?") },
            text = { Text("Все пароли будут удалены без возможности восстановления. Сделайте резервную копию перед сбросом.", color = MaterialTheme.colorScheme.error) },
            confirmButton = {
                Button({ showReset = false; vm.reset() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Удалить всё")
                }
            },
            dismissButton = { TextButton({ showReset = false }) { Text("Отмена") } }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState())) {
            // Header
            Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Настройки", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            SectionHeader("Безопасность")
            SettingItem(Icons.Default.Fingerprint, "Биометрический вход", "Отпечаток пальца",
                trailing = { Switch(bio, { bio = it; vm.setBiometric(it) }) })
            SettingItem(Icons.Default.Lock, "Изменить мастер-пароль", "Смена пароля для входа",
                onClick = { showChangePwd = true })

            SectionHeader("Резервная копия")
            SettingItem(Icons.Default.Upload, "Экспорт данных", "Сохранить зашифрованный .svault файл",
                onClick = { exportPicker.launch("backup_${System.currentTimeMillis()}.svault") })
            SettingItem(Icons.Default.Download, "Импорт данных", "Восстановить из .svault файла",
                onClick = { importPicker.launch(arrayOf("*/*")) })

            SectionHeader("Данные")
            SettingItem(Icons.Default.DeleteForever, "Сбросить хранилище", "Удалить все пароли",
                titleColor = MaterialTheme.colorScheme.error, onClick = { showReset = true })

            SectionHeader("О приложении")
            SettingItem(Icons.Default.Shield, "Безопасность", "AES-256-GCM · Android Keystore · PBKDF2-SHA256")
            SettingItem(Icons.Default.Info, "Версия", "Secure Vault 1.0")
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun SettingItem(
    icon: ImageVector, title: String, subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(onClick = { onClick?.invoke() }, enabled = onClick != null,
        color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = titleColor, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (trailing != null) trailing()
            else if (onClick != null) Icon(Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}
