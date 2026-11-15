package com.example.lynproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The app's single Room database (file name: "music_companion.db").
 *
 * For Rym's Setlist feature: add the new entities to `entities = [...]`,
 * add an abstract DAO function, and increase `version` to 2.
 * fallbackToDestructiveMigration() wipes old test data when the version
 * changes, which is fine while we are still developing.
 */
@Database(
    entities = [Song::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Returns the one shared database instance (creates it the first time). */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_companion.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
