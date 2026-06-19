package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.KhutbahRecord
import com.example.data.model.Masjid
import com.example.data.model.JadwalPetugas

@Database(
    entities = [Masjid::class, KhutbahRecord::class, JadwalPetugas::class, OfflineSurahEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun masjidDao(): MasjidDao
    abstract fun khutbahDao(): KhutbahDao
    abstract fun jadwalDao(): JadwalDao
    abstract fun offlineSurahDao(): OfflineSurahDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "khutbah_masjid_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
