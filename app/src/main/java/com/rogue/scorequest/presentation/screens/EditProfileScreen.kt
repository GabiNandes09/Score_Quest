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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.rogue.scorequest.presentation.viewmodel.EditProfileViewModel
import com.rogue.scorequest.utils.ImageStorage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    viewModel: EditProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.saved) {
        if (state.saved) onBackClick()
    }

    var pendingCaptureUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCaptureUri?.let { uri -> viewModel.onAvatarCaptured(ImageStorage.persistImage(context, uri)) }
    }
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onAvatarCaptured(ImageStorage.persistImage(context, uri))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.avatarUri?.let { uri ->
                AsyncImage(model = uri, contentDescription = null, modifier = Modifier.size(96.dp))
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
                value = state.displayName,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.bio,
                onValueChange = viewModel::onBioChange,
                label = { Text("Bio (até 150 caracteres)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
                Text("Salvar")
            }
        }
    }
}
