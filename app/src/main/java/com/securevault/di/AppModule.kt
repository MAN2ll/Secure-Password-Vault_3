package com.securevault.di

import android.content.Context
import androidx.room.Room
import com.securevault.data.db.VaultDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun db(@ApplicationContext ctx: Context): VaultDatabase =
        Room.databaseBuilder(ctx, VaultDatabase::class.java, "vault.db")
            .fallbackToDestructiveMigration().build()

    @Provides @Singleton
    fun dao(db: VaultDatabase) = db.dao()
}
