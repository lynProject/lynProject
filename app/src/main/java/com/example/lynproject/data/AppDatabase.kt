package com.example.lynproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The app's single Room database (file name: "music_companion.db").
 *
 * Tables: songs (Lyn), setlists + setlist_songs (Rym).
 * fallbackToDestructiveMigration() wipes old test data when the version
 * changes, which is fine while we are still developing.
 */
@Database(
    entities = [Song::class, Setlist::class, SetlistSong::class],
    version = 2, // v2: Rym added setlists + setlist_songs
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    abstract fun setlistDao(): SetlistDao

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
