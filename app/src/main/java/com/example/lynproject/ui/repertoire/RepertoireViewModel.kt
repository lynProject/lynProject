package com.example.lynproject.ui.repertoire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lynproject.LynApplication
import com.example.lynproject.data.Song
import com.example.lynproject.data.SongRepository
import com.example.lynproject.data.SongStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Everything the Repertoire screen needs to draw itself. */
data class RepertoireUiState(
    val songs: List<Song> = emptyList(),
    val selectedFilter: SongStatus? = null, // null = show all
    val isLoading: Boolean = true
)

/**
 * Holds the state of the Repertoire list screen and survives rotation.
 *
 * Data flow:  Room (Flow) -> SongRepository -> this ViewModel (StateFlow) -> RepertoireScreen
 * Events flow the other way: the screen calls onFilterSelected / deleteSong.
 */
class RepertoireViewModel(private val repository: SongRepository) : ViewModel() {

    private val selectedFilter = MutableStateFlow<SongStatus?>(null)

    val uiState: StateFlow<RepertoireUiState> =
        combine(repository.allSongs, selectedFilter) { songs, filter ->
            RepertoireUiState(
                songs = if (filter == null) songs else songs.filter { it.status == filter },
                selectedFilter = filter,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RepertoireUiState()
        )

    fun onFilterSelected(status: SongStatus?) {
        selectedFilter.value = status
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch { repository.deleteSong(song) }
    }

    companion object {
        /** Tells Compose how to build this ViewModel with the shared repository. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LynApplication
                RepertoireViewModel(app.songRepository)
            }
        }
    }
}
