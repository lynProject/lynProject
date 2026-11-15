package com.example.lynproject.ui.setlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lynproject.LynApplication
import com.example.lynproject.data.Setlist
import com.example.lynproject.data.SetlistRepository
import com.example.lynproject.data.SetlistWithCount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SetlistsUiState(
    val setlists: List<SetlistWithCount> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * State for the Setlists list screen.
 * Data flow: Room -> SetlistRepository -> this ViewModel (StateFlow) -> SetlistsScreen
 */
class SetlistsViewModel(private val repository: SetlistRepository) : ViewModel() {

    val uiState: StateFlow<SetlistsUiState> =
        repository.setlists
            .map { SetlistsUiState(setlists = it, isLoading = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SetlistsUiState()
            )

    fun createSetlist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.createSetlist(name.trim()) }
    }

    fun deleteSetlist(setlist: Setlist) {
        viewModelScope.launch { repository.deleteSetlist(setlist) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LynApplication
                SetlistsViewModel(app.setlistRepository)
            }
        }
    }
}
