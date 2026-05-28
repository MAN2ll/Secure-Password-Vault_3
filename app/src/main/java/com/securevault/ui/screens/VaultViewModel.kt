package com.securevault.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securevault.data.repository.Entry
import com.securevault.data.repository.VaultRepository
import com.securevault.security.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repo: VaultRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _category = MutableStateFlow<String?>(null)
    val category: StateFlow<String?> = _category

    @OptIn(ExperimentalCoroutinesApi::class)
    val entries: StateFlow<List<Entry>> = _query
        .flatMapLatest { q -> if (q.isEmpty()) repo.getAll() else repo.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<Entry>> = repo.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repo.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val count: StateFlow<Int> = repo.getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setQuery(q: String) { _query.value = q }
    fun setCategory(c: String?) { _category.value = c }
    fun toggleFavorite(e: Entry) { viewModelScope.launch { repo.toggleFavorite(e) } }
    fun delete(id: Long) { viewModelScope.launch { repo.delete(id) } }
    fun lock() { session.lock() }
}
