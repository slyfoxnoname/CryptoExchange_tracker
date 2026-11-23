package com.example.cryptoexchange.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cryptoexchange.data.local.dao.CryptoDao
import com.example.cryptoexchange.data.local.entity.CryptoCurrencyEntity

@Database(
    entities = [CryptoCurrencyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CryptoDatabase : RoomDatabase() {
    abstract fun cryptoDao(): CryptoDao

    companion object {
        const val DATABASE_NAME = "crypto_exchange.db"

        fun create(context: Context): CryptoDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                CryptoDatabase::class.java,
                DATABASE_NAME
            ).build()
    }
}
