package com.example.lynproject.ui.setlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lynproject.LynApplication
import com.example.lynproject.data.SetlistRepository
import com.example.lynproject.data.Song
import com.example.lynproject.data.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SetlistDetailUiState(
    val setlistName: String = "",
    /** Songs in this setlist, in the saved order. */
    val songs: List<Song> = emptyList(),
    /** Repertoire songs that are NOT in this setlist yet (for the "Add songs" dialog). */
    val availableSongs: List<Song> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * State for one setlist: its ordered songs + the songs that can still be added.
 *
 * It combines THREE live Room flows (setlist name, its songs, the whole repertoire),
 * so the screen updates by itself whenever any of them changes.
 */
class SetlistDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val setlistRepository: SetlistRepository,
    songRepository: SongRepository
) : ViewModel() {

    private val setlistId: Long = checkNotNull(savedStateHandle.get<Long>(SETLIST_ID_ARG))

    val uiState: StateFlow<SetlistDetailUiState> =
        combine(
            setlistRepository.getSetlist(setlistId),
            setlistRepository.getSongsInSetlist(setlistId),
            songRepository.allSongs
        ) { setlist, songsInSetlist, allSongs ->
            val idsInSetlist = songsInSetlist.map { it.id }.toSet()
            SetlistDetailUiState(
                setlistName = setlist?.name ?: "",
                songs = songsInSetlist,
                availableSongs = allSongs.filter { it.id !in idsInSetlist },
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SetlistDetailUiState()
        )

    fun addSong(song: Song) {
        viewModelScope.launch { setlistRepository.addSongToSetlist(setlistId, song.id) }
    }

    fun removeSong(song: Song) {
        viewModelScope.launch { setlistRepository.removeSongFromSetlist(setlistId, song.id) }
    }

    fun moveUp(index: Int) = move(from = index, to = index - 1)

    fun moveDown(index: Int) = move(from = index, to = index + 1)

    /** Swaps two songs and saves the whole new order to Room, so it survives a restart. */
    private fun move(from: Int, to: Int) {
        val current = uiState.value.songs.toMutableList()
        if (from !in current.indices || to !in current.indices) return
        val song = current.removeAt(from)
        current.add(to, song)
        viewModelScope.launch {
            setlistRepository.saveOrder(setlistId, current.map { it.id })
        }
    }

    companion object {
        const val SETLIST_ID_ARG = "setlistId"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LynApplication
                SetlistDetailViewModel(
                    createSavedStateHandle(),
                    app.setlistRepository,
                    app.songRepository
                )
            }
        }
    }
}
