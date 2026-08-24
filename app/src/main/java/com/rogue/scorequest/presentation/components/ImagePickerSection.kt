package com.rogue.scorequest.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rogue.scorequest.utils.ImageStorage

/**
 * Prévia + 3 formas de definir uma imagem (capa de jogo, avatar de jogador, etc.):
 * Câmera, Galeria (as duas persistem local via ImageStorage) ou colar uma URL direta.
 * `onCaptured` recebe o valor final (caminho local ou URL) — mesmo campo que já aceitava
 * as duas formas antes (ex.: BoardGame.coverImageUrl).
 */
@Composable
fun ImagePickerSection(
    currentPath: String?,
    onCaptured: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val captureFromCamera = rememberCameraCaptureAction(onCaptured = onCaptured)
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onCaptured(ImageStorage.persistImage(context, uri))
    }
    var urlInput by remember { mutableStateOf("") }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        currentPath?.let { path ->
            AsyncImage(
                model = path,
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = captureFromCamera) {
                Text("Câmera")
            }
            OutlinedButton(onClick = {
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Text("Galeria")
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("...ou URL da foto") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedButton(
                onClick = {
                    onCaptured(urlInput.trim())
                    urlInput = ""
                },
                enabled = urlInput.isNotBlank()
            ) {
                Text("Usar")
            }
        }
    }
}
