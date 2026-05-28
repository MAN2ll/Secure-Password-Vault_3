package com.securevault.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.securevault.data.model.VaultEntry

@Database(entities = [VaultEntry::class], version = 1, exportSchema = false)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun dao(): VaultDao
}
