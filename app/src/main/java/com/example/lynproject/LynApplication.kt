package com.example.lynproject

import android.app.Application
import com.example.lynproject.data.AppDatabase
import com.example.lynproject.data.SetlistRepository
import com.example.lynproject.data.SongRepository

/**
 * Runs once when the app starts, before any screen.
 * It holds the objects the whole app shares (database + repositories),
 * so every ViewModel gets the same instances.
 *
 * Registered in AndroidManifest.xml with android:name=".LynApplication".
 */
class LynApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val songRepository: SongRepository by lazy { SongRepository(database.songDao()) }

    val setlistRepository: SetlistRepository by lazy { SetlistRepository(database.setlistDao()) }
}
