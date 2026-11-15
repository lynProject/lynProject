package com.example.lynproject.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A named setlist, e.g. "Friday gig at Café Blue".
 * One row in the "setlists" table.
 */
@Entity(tableName = "setlists")
data class Setlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

/**
 * Links a song to a setlist and remembers its place in the order.
 *
 * - Many-to-many: one setlist has many songs, one song can be in many setlists.
 * - [position] (0, 1, 2, ...) is what keeps the chosen order after the app restarts.
 * - CASCADE: deleting a setlist or a song automatically removes these links.
 */
@Entity(
    tableName = "setlist_songs",
    primaryKeys = ["setlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = Setlist::class,
            parentColumns = ["id"],
            childColumns = ["setlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("songId")]
)
data class SetlistSong(
    val setlistId: Long,
    val songId: Long,
    val position: Int
)

/** A setlist plus how many songs it contains (for the Setlists list screen). */
data class SetlistWithCount(
    @Embedded val setlist: Setlist,
    val songCount: Int
)
