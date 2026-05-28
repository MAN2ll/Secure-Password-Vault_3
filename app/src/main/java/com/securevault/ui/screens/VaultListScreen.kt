package com.securevault.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.securevault.data.repository.Entry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultListScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onLock: () -> Unit,
    favOnly: Boolean = false,
    vm: VaultViewModel = hiltViewModel()
) {
    val allEntries by (if (favOnly) vm.favorites else vm.entries).collectAsState()
    val query by vm.query.collectAsState()
    val categories by vm.categories.collectAsState()
    val selectedCat by vm.category.collectAsState()
    val count by vm.count.collectAsState()
    var toDelete by remember { mutableStateOf<Entry?>(null) }

    val shown = if (selectedCat != null) allEntries.filter { it.category == selectedCat } else allEntries

    toDelete?.let { e ->
        AlertDialog(onDismissRequest = { toDelete = null },
            title = { Text("Удалить?") },
            text = { Text("«${e.title}» будет удалена безвозвратно.") },
            confirmButton = { TextButton({ vm.delete(e.id); toDelete = null }) {
                Text("Удалить", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton({ toDelete = null }) { Text("Отмена") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (favOnly) "Избранное" else "Пароли") },
                actions = {
                    Text("$count", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp).align(Alignment.CenterVertically))
                    IconButton({ vm.lock(); onLock() }) { Icon(Icons.Default.Lock, "Заблокировать") }
                }
            )
        },
        floatingActionButton = { if (!favOnly) FloatingActionButton(onAdd) { Icon(Icons.Default.Add, null) } }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            if (!favOnly) {
                OutlinedTextField(
                    value = query, onValueChange = vm::setQuery,
                    placeholder = { Text("Поиск...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = { if (query.isNotEmpty()) IconButton({ vm.setQuery("") }) { Icon(Icons.Default.Clear, null) } },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )
                if (categories.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selectedCat == null, { vm.setCategory(null) }, label = { Text("Все") })
                        categories.forEach { c ->
                            FilterChip(selectedCat == c, { vm.setCategory(if (selectedCat == c) null else c) }, label = { Text(c) })
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
            if (shown.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(if (favOnly) Icons.Default.StarBorder else Icons.Default.VpnKey,
                            null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(when {
                            favOnly -> "Нет избранных записей"
                            query.isNotEmpty() -> "Ничего не найдено"
                            else -> "Нет паролей.\nНажмите + чтобы добавить"
                        }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(shown, key = { it.id }) { e ->
                        EntryCard(e, { onEdit(e.id) }, { vm.toggleFavorite(e) }, { toDelete = e })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntryCard(e: Entry, onClick: () -> Unit, onFav: () -> Unit, onDelete: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = { menu = true }),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(44.dp)) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(e.title.take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(e.title, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (e.username.isNotEmpty())
                    Text(e.username, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(e.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onFav, Modifier.size(32.dp)) {
                    Icon(if (e.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, null,
                        tint = if (e.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp))
                }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem(text = { Text("Удалить") }, onClick = { menu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) })
                }
            }
        }
    }
}
