package com.example.lynproject.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * SQL queries for setlists and their ordered songs.
 * (An abstract class instead of an interface so [saveOrder] can have a body.)
 */
@Dao
abstract class SetlistDao {

    // ---------- Setlists ----------

    @Query(
        """
        SELECT setlists.*, COUNT(setlist_songs.songId) AS songCount
        FROM setlists
        LEFT JOIN setlist_songs ON setlists.id = setlist_songs.setlistId
        GROUP BY setlists.id
        ORDER BY setlists.name COLLATE NOCASE ASC
        """
    )
    abstract fun getSetlistsWithCount(): Flow<List<SetlistWithCount>>

    @Query("SELECT * FROM setlists WHERE id = :id")
    abstract fun getSetlist(id: Long): Flow<Setlist?>

    @Insert
    abstract suspend fun insertSetlist(setlist: Setlist): Long

    @Delete
    abstract suspend fun deleteSetlist(setlist: Setlist)

    // ---------- Songs inside a setlist ----------

    /** The songs of one setlist, in the saved order. */
    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN setlist_songs ON songs.id = setlist_songs.songId
        WHERE setlist_songs.setlistId = :setlistId
        ORDER BY setlist_songs.position ASC
        """
    )
    abstract fun getSongsInSetlist(setlistId: Long): Flow<List<Song>>

    @Query("SELECT MAX(position) FROM setlist_songs WHERE setlistId = :setlistId")
    abstract suspend fun getMaxPosition(setlistId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertSetlistSong(item: SetlistSong)

    @Query("DELETE FROM setlist_songs WHERE setlistId = :setlistId AND songId = :songId")
    abstract suspend fun removeSong(setlistId: Long, songId: Long)

    @Query("UPDATE setlist_songs SET position = :position WHERE setlistId = :setlistId AND songId = :songId")
    abstract suspend fun updatePosition(setlistId: Long, songId: Long, position: Int)

    /**
     * Saves a new order: the song at index 0 gets position 0, and so on.
     * @Transaction = all updates succeed together or none do.
     */
    @Transaction
    open suspend fun saveOrder(setlistId: Long, orderedSongIds: List<Long>) {
        orderedSongIds.forEachIndexed { index, songId ->
            updatePosition(setlistId, songId, index)
        }
    }
}
