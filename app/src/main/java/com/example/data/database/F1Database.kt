package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.entities.*
import com.example.data.dao.*

@Database(
    entities = [
        DriverEntity::class,
        TeamEntity::class,
        RaceEntity::class,
        NewsEntity::class,
        FavoriteEntity::class,
        NotificationSettingEntity::class,
        LiveTimingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class F1Database : RoomDatabase() {
    abstract fun driverDao(): DriverDao
    abstract fun teamDao(): TeamDao
    abstract fun raceDao(): RaceDao
    abstract fun newsDao(): NewsDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun notificationDao(): NotificationDao
    abstract fun liveTimingDao(): LiveTimingDao

    companion object {
        @Volatile
        private var INSTANCE: F1Database? = null

        fun getDatabase(context: Context): F1Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    F1Database::class.java,
                    "f1_ultimate_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
