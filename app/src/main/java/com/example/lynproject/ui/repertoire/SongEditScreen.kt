package com.example.lynproject.ui.repertoire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lynproject.data.SongStatus
import com.example.lynproject.ui.theme.LynProjectTheme
import kotlin.math.roundToInt

@Composable
fun SongEditScreen(
    onBack: () -> Unit,
    viewModel: SongEditViewModel = viewModel(factory = SongEditViewModel.Factory)
) {
    val form by viewModel.formState.collectAsStateWithLifecycle()

    // When the ViewModel reports the song was saved, go back to the list.
    LaunchedEffect(form.isSaved) {
        if (form.isSaved) onBack()
    }

    SongEditContent(
        form = form,
        onTitleChange = viewModel::onTitleChange,
        onKeyChange = viewModel::onKeyChange,
        onDifficultyChange = viewModel::onDifficultyChange,
        onStatusChange = viewModel::onStatusChange,
        onSave = viewModel::save,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongEditContent(
    form: SongFormState,
    onTitleChange: (String) -> Unit,
    onKeyChange: (String) -> Unit,
    onDifficultyChange: (Int) -> Unit,
    onStatusChange: (SongStatus) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (form.isEditing) "Edit Song" else "Add Song") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Back") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = form.title,
                onValueChange = onTitleChange,
                label = { Text("Song title *") },
                isError = form.titleError,
                supportingText = { if (form.titleError) Text("Title is required") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.musicalKey,
                onValueChange = onKeyChange,
                label = { Text("Key (e.g. G major)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                Text(
                    "Difficulty: ${form.difficulty} / 5",
                    style = MaterialTheme.typography.titleSmall
                )
                Slider(
                    value = form.difficulty.toFloat(),
                    onValueChange = { onDifficultyChange(it.roundToInt()) },
                    valueRange = 1f..5f,
                    steps = 3 // 3 stops between 1 and 5 -> 1,2,3,4,5
                )
            }

            Column {
                Text("Status", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SongStatus.entries.forEach { status ->
                        FilterChip(
                            selected = form.status == status,
                            onClick = { onStatusChange(status) },
                            label = { Text(status.label) }
                        )
                    }
                }
            }

            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text(if (form.isEditing) "Save changes" else "Add to repertoire")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SongEditPreview() {
    LynProjectTheme {
        SongEditContent(
            form = SongFormState(title = "Blackbird", musicalKey = "G major"),
            onTitleChange = {}, onKeyChange = {}, onDifficultyChange = {},
            onStatusChange = {}, onSave = {}, onBack = {}
        )
    }
}
