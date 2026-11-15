package com.example.lynproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * How far along the musician is with a song.
 * Room stores an enum as its name (e.g. "LEARNING") in the database.
 */
enum class SongStatus(val label: String) {
    LEARNING("Learning"),
    PRACTICING("Practicing"),
    READY("Performance ready")
}

/**
 * One song in the musician's personal repertoire.
 * Each Song is one row in the "songs" table.
 *
 * Rym's Setlist feature refers to songs by [id].
 */
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    /** Musical key, e.g. "G major" or "E minor". (Not called "key" because KEY is an SQL keyword.) */
    val musicalKey: String,
    /** 1 = easy … 5 = very hard */
    val difficulty: Int,
    val status: SongStatus
)
