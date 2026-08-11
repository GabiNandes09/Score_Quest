package com.rogue.scorequest.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.presentation.viewmodel.AddEditGameViewModel
import com.rogue.scorequest.utils.ImageStorage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGameScreen(
    gameId: String,
    onBackClick: () -> Unit,
    viewModel: AddEditGameViewModel = koinViewModel(parameters = { parametersOf(gameId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.saved) {
        if (state.saved) onBackClick()
    }

    var pendingCaptureUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCaptureUri?.let { uri -> viewModel.onCoverCaptured(ImageStorage.persistImage(context, uri)) }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onCoverCaptured(ImageStorage.persistImage(context, uri))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Editar Jogo" else "Adicionar Jogo") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.coverImagePath?.let { path ->
                AsyncImage(
                    model = path,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val uri = ImageStorage.createCaptureUri(context)
                    pendingCaptureUri = uri
                    takePictureLauncher.launch(uri)
                }) {
                    Text("Câmera")
                }
                OutlinedButton(onClick = {
                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("Galeria")
                }
            }

            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.minPlayers,
                    onValueChange = viewModel::onMinPlayersChange,
                    label = { Text("Mín. jogadores") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.maxPlayers,
                    onValueChange = viewModel::onMaxPlayersChange,
                    label = { Text("Máx. jogadores") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = state.avgDurationMinutes,
                onValueChange = viewModel::onDurationChange,
                label = { Text("Duração média (min)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.category,
                onValueChange = viewModel::onCategoryChange,
                label = { Text("Categoria (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.weight,
                onValueChange = viewModel::onWeightChange,
                label = { Text("Peso/complexidade (opcional, 1.0 a 5.0)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (!state.isEditMode) {
                Text(text = "Status na estante", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LibraryStatus.entries.forEach { status ->
                        FilterChip(
                            selected = state.status == status,
                            onClick = { viewModel.onStatusSelected(status) },
                            label = { Text(statusLabel(status)) }
                        )
                    }
                }
            }

            Button(
                onClick = viewModel::save,
                enabled = state.isValid && !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }
        }
    }
}

private fun statusLabel(status: LibraryStatus): String = when (status) {
    LibraryStatus.HAVE -> "Tenho"
    LibraryStatus.WANT -> "Quero"
    LibraryStatus.DONT_HAVE -> "Não tenho"
}
