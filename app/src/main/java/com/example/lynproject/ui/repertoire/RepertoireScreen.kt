package com.example.lynproject.ui.repertoire

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lynproject.data.Song
import com.example.lynproject.data.SongStatus
import com.example.lynproject.ui.theme.LynProjectTheme

/**
 * "Stateful" screen: gets the ViewModel and passes plain data + callbacks down
 * to [RepertoireContent] (state hoisting).
 */
@Composable
fun RepertoireScreen(
    onAddSong: () -> Unit,
    onEditSong: (Long) -> Unit,
    viewModel: RepertoireViewModel = viewModel(factory = RepertoireViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RepertoireContent(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onAddSong = onAddSong,
        onEditSong = onEditSong,
        onDeleteSong = viewModel::deleteSong
    )
}

/** "Stateless" UI: only draws what it is given. Easy to preview and test. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepertoireContent(
    uiState: RepertoireUiState,
    onFilterSelected: (SongStatus?) -> Unit,
    onAddSong: () -> Unit,
    onEditSong: (Long) -> Unit,
    onDeleteSong: (Song) -> Unit
) {
    var songToDelete by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Repertoire") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSong) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            StatusFilterRow(
                selected = uiState.selectedFilter,
                onFilterSelected = onFilterSelected
            )

            when {
                uiState.isLoading -> CenteredBox { CircularProgressIndicator() }

                uiState.songs.isEmpty() -> CenteredBox {
                    Text(
                        text = if (uiState.selectedFilter == null)
                            "No songs yet.\nTap + to add your first song."
                        else
                            "No songs with status \"${uiState.selectedFilter.label}\".",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.songs, key = { it.id }) { song ->
                        SongCard(
                            song = song,
                            onClick = { onEditSong(song.id) },
                            onDelete = { songToDelete = song }
                        )
                    }
                }
            }
        }
    }

    // Confirmation before deleting
    songToDelete?.let { song ->
        AlertDialog(
            onDismissRequest = { songToDelete = null },
            title = { Text("Delete song?") },
            text = { Text("\"${song.title}\" will be removed from your repertoire.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteSong(song)
                    songToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { songToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatusFilterRow(
    selected: SongStatus?,
    onFilterSelected: (SongStatus?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onFilterSelected(null) },
                label = { Text("All") }
            )
        }
        items(SongStatus.entries) { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onFilterSelected(status) },
                label = { Text(status.label) }
            )
        }
    }
}

@Composable
private fun SongCard(song: Song, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buildString {
                        if (song.musicalKey.isNotBlank()) append("Key: ${song.musicalKey}  •  ")
                        append("Difficulty: ${"★".repeat(song.difficulty)}${"☆".repeat(5 - song.difficulty)}")
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = song.status.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Preview(showBackground = true)
@Composable
private fun RepertoirePreview() {
    LynProjectTheme {
        RepertoireContent(
            uiState = RepertoireUiState(
                songs = listOf(
                    Song(1, "Autumn Leaves", "G minor", 3, SongStatus.READY),
                    Song(2, "Blackbird", "G major", 4, SongStatus.PRACTICING)
                ),
                isLoading = false
            ),
            onFilterSelected = {}, onAddSong = {}, onEditSong = {}, onDeleteSong = {}
        )
    }
}
