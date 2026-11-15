package com.example.lynproject.ui.setlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.lynproject.data.Setlist
import com.example.lynproject.data.SetlistWithCount
import com.example.lynproject.ui.theme.LynProjectTheme

@Composable
fun SetlistsScreen(
    onOpenSetlist: (Long) -> Unit,
    viewModel: SetlistsViewModel = viewModel(factory = SetlistsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SetlistsContent(
        uiState = uiState,
        onCreateSetlist = viewModel::createSetlist,
        onOpenSetlist = onOpenSetlist,
        onDeleteSetlist = viewModel::deleteSetlist
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistsContent(
    uiState: SetlistsUiState,
    onCreateSetlist: (String) -> Unit,
    onOpenSetlist: (Long) -> Unit,
    onDeleteSetlist: (Setlist) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var setlistToDelete by remember { mutableStateOf<Setlist?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Setlists") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
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

                uiState.setlists.isEmpty() -> Text(
                    text = "No setlists yet.\nTap + to create one for your next performance.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.setlists, key = { it.setlist.id }) { item ->
                        SetlistCard(
                            item = item,
                            onClick = { onOpenSetlist(item.setlist.id) },
                            onDelete = { setlistToDelete = item.setlist }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateSetlistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                onCreateSetlist(name)
                showCreateDialog = false
            }
        )
    }

    setlistToDelete?.let { setlist ->
        AlertDialog(
            onDismissRequest = { setlistToDelete = null },
            title = { Text("Delete setlist?") },
            text = { Text("\"${setlist.name}\" will be deleted. Your songs stay in the repertoire.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteSetlist(setlist)
                    setlistToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { setlistToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SetlistCard(item: SetlistWithCount, onClick: () -> Unit, onDelete: () -> Unit) {
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
                    text = item.setlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (item.songCount == 1) "1 song" else "${item.songCount} songs",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

@Composable
private fun CreateSetlistDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New setlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name (e.g. Friday gig)") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name) }, enabled = name.isNotBlank()) {
                Text("Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Preview(showBackground = true)
@Composable
private fun SetlistsPreview() {
    LynProjectTheme {
        SetlistsContent(
            uiState = SetlistsUiState(
                setlists = listOf(
                    SetlistWithCount(Setlist(1, "Friday gig"), 5),
                    SetlistWithCount(Setlist(2, "Wedding set"), 1)
                ),
                isLoading = false
            ),
            onCreateSetlist = {}, onOpenSetlist = {}, onDeleteSetlist = {}
        )
    }
}
