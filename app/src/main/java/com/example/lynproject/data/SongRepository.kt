package com.example.lynproject.data

import kotlinx.coroutines.flow.Flow

/**
 * The single place the rest of the app goes to for song data.
 * ViewModels talk to the repository, never to the DAO directly,
 * so the data source could change later (e.g. add an API) without touching the UI.
 */
class SongRepository(private val songDao: SongDao) {

    val allSongs: Flow<List<Song>> = songDao.getAllSongs()

    suspend fun getSong(id: Long): Song? = songDao.getSongById(id)

    suspend fun addSong(song: Song): Long = songDao.insert(song)

    suspend fun updateSong(song: Song) = songDao.update(song)

    suspend fun deleteSong(song: Song) = songDao.delete(song)
}
