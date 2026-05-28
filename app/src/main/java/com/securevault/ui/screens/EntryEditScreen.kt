package com.securevault.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditScreen(entryId: Long?, onBack: () -> Unit, vm: EntryViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val existing by vm.entry.collectAsState()
    val saved by vm.saved.collectAsState()

    var title by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Общее") }
    var user by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var fav by remember { mutableStateOf(false) }
    var showPwd by remember { mutableStateOf(false) }
    var showGen by remember { mutableStateOf(false) }
    val cats = listOf("Общее","Соцсети","Банки","Работа","Почта","Другое")

    LaunchedEffect(entryId) { entryId?.let { vm.load(it) } }
    LaunchedEffect(existing) { existing?.let { title = it.title; cat = it.category; user = it.username; pwd = it.password; url = it.url; notes = it.notes; fav = it.isFavorite } }
    LaunchedEffect(saved) { if (saved) onBack() }

    fun copy(text: String) = (ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        .setPrimaryClip(ClipData.newPlainText("sv", text))

    if (showGen) {
        var len by remember { mutableFloatStateOf(20f) }
        var upper by remember { mutableStateOf(true) }
        var digits by remember { mutableStateOf(true) }
        var syms by remember { mutableStateOf(true) }
        var preview by remember { mutableStateOf(vm.generatePassword()) }
        AlertDialog(onDismissRequest = { showGen = false },
            title = { Text("Генератор паролей") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                        Text(preview, Modifier.padding(12.dp))
                    }
                    Text("Длина: ${len.toInt()}")
                    Slider(len, { len = it; preview = vm.generatePassword(it.toInt(), upper, digits, syms) }, valueRange = 8f..40f)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(upper, { upper = it; preview = vm.generatePassword(len.toInt(), it, digits, syms) })
                        Text("Заглавные буквы")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(digits, { digits = it; preview = vm.generatePassword(len.toInt(), upper, it, syms) })
                        Text("Цифры")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(syms, { syms = it; preview = vm.generatePassword(len.toInt(), upper, digits, it) })
                        Text("Символы")
                    }
                }
            },
            confirmButton = { Button({ pwd = preview; showGen = false }) { Text("Использовать") } },
            dismissButton = { TextButton({ showGen = false }) { Text("Отмена") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entryId == null) "Новая запись" else "Редактировать") },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton({ fav = !fav }) {
                        Icon(if (fav) Icons.Default.Star else Icons.Default.StarBorder, null,
                            tint = if (fav) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton({ vm.save(entryId ?: 0, title, cat, user, pwd, url, notes, fav) }) {
                        Icon(Icons.Default.Check, "Сохранить")
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название *") },
                modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Label, null) }, singleLine = true)

            var catExp by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(catExp, { catExp = it }) {
                OutlinedTextField(value = cat, onValueChange = {}, readOnly = true, label = { Text("Категория") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExp) },
                    modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(catExp, { catExp = false }) {
                    cats.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { cat = c; catExp = false }) }
                }
            }

            OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Логин / Email") },
                modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Person, null) },
                trailingIcon = { if (user.isNotEmpty()) IconButton({ copy(user) }) { Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp)) } },
                singleLine = true)

            OutlinedTextField(value = pwd, onValueChange = { pwd = it }, label = { Text("Пароль *") },
                visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    Row {
                        if (pwd.isNotEmpty()) IconButton({ copy(pwd) }) { Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp)) }
                        IconButton({ showPwd = !showPwd }) { Icon(if (showPwd) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) }
                        IconButton({ showGen = true }) { Icon(Icons.Default.Casino, null) }
                    }
                }, singleLine = true)

            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL (необязательно)") },
                modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Language, null) }, singleLine = true)

            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Заметки") },
                modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 6)
        }
    }
}
