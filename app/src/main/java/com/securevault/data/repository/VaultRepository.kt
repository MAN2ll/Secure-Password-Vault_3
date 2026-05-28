package com.securevault.data.repository

import com.securevault.data.db.VaultDao
import com.securevault.data.model.VaultEntry
import com.securevault.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class Entry(
    val id: Long, val title: String, val category: String,
    val username: String, val password: String,
    val url: String, val notes: String,
    val createdAt: Long, val updatedAt: Long, val isFavorite: Boolean
)

@Singleton
class VaultRepository @Inject constructor(
    private val dao: VaultDao,
    private val crypto: CryptoManager
) {
    fun getAll(): Flow<List<Entry>> = dao.getAll().map { it.map(::dec) }
    fun getFavorites(): Flow<List<Entry>> = dao.getFavorites().map { it.map(::dec) }
    fun search(q: String): Flow<List<Entry>> = dao.search(q).map { it.map(::dec) }
    fun getCategories(): Flow<List<String>> = dao.getCategories()
    fun getCount(): Flow<Int> = dao.getCount()

    suspend fun getAllSync(): List<Entry> = dao.getAll().first().map(::dec)
    suspend fun getById(id: Long): Entry? = dao.getById(id)?.let(::dec)

    suspend fun save(
        id: Long = 0, title: String, category: String,
        username: String, password: String,
        url: String = "", notes: String = "", isFavorite: Boolean = false
    ) = dao.insert(VaultEntry(
        id = id, title = title, category = category,
        encryptedUsername = enc(username), encryptedPassword = enc(password),
        encryptedUrl = enc(url), encryptedNotes = enc(notes),
        updatedAt = System.currentTimeMillis(), isFavorite = isFavorite
    ))

    suspend fun toggleFavorite(e: Entry) {
        dao.getById(e.id)?.let { dao.update(it.copy(isFavorite = !it.isFavorite)) }
    }
    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun deleteAll() = dao.deleteAll()

    private fun enc(s: String) = if (s.isNotEmpty()) crypto.encryptString(s) else ""
    private fun dec(s: String) = if (s.isNotEmpty()) try { crypto.decryptString(s) } catch (_: Exception) { "" } else ""
    private fun dec(e: VaultEntry) = Entry(e.id, e.title, e.category,
        dec(e.encryptedUsername), dec(e.encryptedPassword),
        dec(e.encryptedUrl), dec(e.encryptedNotes),
        e.createdAt, e.updatedAt, e.isFavorite)
}
