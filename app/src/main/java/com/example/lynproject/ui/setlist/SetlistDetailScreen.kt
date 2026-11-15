package com.example.lynproject.ui.setlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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

@Composable
fun SetlistDetailScreen(
    onBack: () -> Unit,
    viewModel: SetlistDetailViewModel = viewModel(factory = SetlistDetailViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SetlistDetailContent(
        uiState = uiState,
        onBack = onBack,
        onAddSong = viewModel::addSong,
        onRemoveSong = viewModel::removeSong,
        onMoveUp = viewModel::moveUp,
        onMoveDown = viewModel::moveDown
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistDetailContent(
    uiState: SetlistDetailUiState,
    onBack: () -> Unit,
    onAddSong: (Song) -> Unit,
    onRemoveSong: (Song) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.setlistName) },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Back") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+ Add songs")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                uiState.songs.isEmpty() -> Text(
                    text = "This setlist is empty.\nTap \"+ Add songs\" to pick songs from your repertoire.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.songs, key = { _, song -> song.id }) { index, song ->
                        SetlistSongRow(
                            number = index + 1,
                            song = song,
                            canMoveUp = index > 0,
                            canMoveDown = index < uiState.songs.lastIndex,
                            onMoveUp = { onMoveUp(index) },
                            onMoveDown = { onMoveDown(index) },
                            onRemove = { onRemoveSong(song) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSongsDialog(
            availableSongs = uiState.availableSongs,
            onAddSong = onAddSong,
            onDismiss = { showAddDialog = false }
        )
    }
}

/** One numbered song in the setlist with ↑ ↓ ✕ controls. */
@Composable
private fun SetlistSongRow(
    number: Int,
    song: Song,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$number.",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (song.musicalKey.isNotBlank()) {
                    Text("Key: ${song.musicalKey}", style = MaterialTheme.typography.bodySmall)
                }
            }
            TextButton(onClick = onMoveUp, enabled = canMoveUp) { Text("↑") }
            TextButton(onClick = onMoveDown, enabled = canMoveDown) { Text("↓") }
            TextButton(onClick = onRemove) { Text("✕") }
        }
    }
}

/** Lets the user tap songs from the repertoire to add them to the end of the setlist. */
@Composable
private fun AddSongsDialog(
    availableSongs: List<Song>,
    onAddSong: (Song) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add songs") },
        text = {
            if (availableSongs.isEmpty()) {
                Text("No more songs to add. Add new songs in the Repertoire tab first.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(availableSongs, key = { it.id }) { song ->
                        Text(
                            text = "+  ${song.title}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddSong(song) } // stays open so you can add several
                                .padding(vertical = 12.dp)
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Preview(showBackground = true)
@Composable
private fun SetlistDetailPreview() {
    LynProjectTheme {
        SetlistDetailContent(
            uiState = SetlistDetailUiState(
                setlistName = "Friday gig",
                songs = listOf(
                    Song(1, "Autumn Leaves", "G minor", 3, SongStatus.READY),
                    Song(2, "Blackbird", "G major", 4, SongStatus.READY)
                ),
                isLoading = false
            ),
            onBack = {}, onAddSong = {}, onRemoveSong = {}, onMoveUp = {}, onMoveDown = {}
        )
    }
}
