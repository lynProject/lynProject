package com.example.lynproject.data

import kotlinx.coroutines.flow.Flow

/**
 * The single entry point for setlist data.
 * Setlist ViewModels use this, never the DAO directly.
 */
class SetlistRepository(private val setlistDao: SetlistDao) {

    val setlists: Flow<List<SetlistWithCount>> = setlistDao.getSetlistsWithCount()

    fun getSetlist(id: Long): Flow<Setlist?> = setlistDao.getSetlist(id)

    fun getSongsInSetlist(setlistId: Long): Flow<List<Song>> =
        setlistDao.getSongsInSetlist(setlistId)

    suspend fun createSetlist(name: String): Long =
        setlistDao.insertSetlist(Setlist(name = name))

    suspend fun deleteSetlist(setlist: Setlist) = setlistDao.deleteSetlist(setlist)

    /** Adds a song at the end of the setlist. */
    suspend fun addSongToSetlist(setlistId: Long, songId: Long) {
        val nextPosition = (setlistDao.getMaxPosition(setlistId) ?: -1) + 1
        setlistDao.insertSetlistSong(SetlistSong(setlistId, songId, nextPosition))
    }

    suspend fun removeSongFromSetlist(setlistId: Long, songId: Long) =
        setlistDao.removeSong(setlistId, songId)

    suspend fun saveOrder(setlistId: Long, orderedSongIds: List<Long>) =
        setlistDao.saveOrder(setlistId, orderedSongIds)
}
