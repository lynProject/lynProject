package com.example.lynproject.ui.repertoire

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lynproject.LynApplication
import com.example.lynproject.data.Song
import com.example.lynproject.data.SongRepository
import com.example.lynproject.data.SongStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The values currently typed into the add/edit form. */
data class SongFormState(
    val title: String = "",
    val musicalKey: String = "",
    val difficulty: Int = 3,
    val status: SongStatus = SongStatus.LEARNING,
    val isEditing: Boolean = false,
    val titleError: Boolean = false,
    val isSaved: Boolean = false // becomes true after saving -> screen navigates back
)

/**
 * Handles the Add Song / Edit Song form.
 * The song id comes from the navigation route ("song_edit?songId=5").
 * If there is no id (-1) we are adding a new song.
 */
class SongEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: SongRepository
) : ViewModel() {

    private val songId: Long = savedStateHandle.get<Long>(SONG_ID_ARG) ?: NEW_SONG_ID

    private val _formState = MutableStateFlow(SongFormState())
    val formState: StateFlow<SongFormState> = _formState.asStateFlow()

    init {
        // Editing an existing song: load it from Room and fill the form.
        if (songId != NEW_SONG_ID) {
            viewModelScope.launch {
                repository.getSong(songId)?.let { song ->
                    _formState.value = SongFormState(
                        title = song.title,
                        musicalKey = song.musicalKey,
                        difficulty = song.difficulty,
                        status = song.status,
                        isEditing = true
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) =
        _formState.update { it.copy(title = value, titleError = false) }

    fun onKeyChange(value: String) =
        _formState.update { it.copy(musicalKey = value) }

    fun onDifficultyChange(value: Int) =
        _formState.update { it.copy(difficulty = value) }

    fun onStatusChange(value: SongStatus) =
        _formState.update { it.copy(status = value) }

    fun save() {
        val form = _formState.value
        if (form.title.isBlank()) {
            _formState.update { it.copy(titleError = true) }
            return
        }
        val song = Song(
            id = if (songId == NEW_SONG_ID) 0 else songId,
            title = form.title.trim(),
            musicalKey = form.musicalKey.trim(),
            difficulty = form.difficulty,
            status = form.status
        )
        viewModelScope.launch {
            if (songId == NEW_SONG_ID) repository.addSong(song) else repository.updateSong(song)
            _formState.update { it.copy(isSaved = true) }
        }
    }

    companion object {
        const val SONG_ID_ARG = "songId"
        const val NEW_SONG_ID = -1L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LynApplication
                SongEditViewModel(createSavedStateHandle(), app.songRepository)
            }
        }
    }
}
