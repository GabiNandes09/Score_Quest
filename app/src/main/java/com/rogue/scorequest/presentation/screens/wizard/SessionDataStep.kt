package com.rogue.scorequest.presentation.screens.wizard

import android.app.DatePickerDialog
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.rogue.scorequest.presentation.viewmodel.AddSessionViewModel
import com.rogue.scorequest.utils.ImageStorage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDataStep(
    viewModel: AddSessionViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingCaptureUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingCaptureUri?.let { uri -> viewModel.onPhotoCaptured(ImageStorage.persistImage(context, uri)) }
    }
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onPhotoCaptured(ImageStorage.persistImage(context, uri))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dados da sessão") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }
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
            OutlinedButton(onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        viewModel.onDateChange(LocalDate.of(year, month + 1, dayOfMonth))
                    },
                    state.date.year,
                    state.date.monthValue - 1,
                    state.date.dayOfMonth
                ).show()
            }) {
                Text("Data: ${state.date.format(dateFormatter)}")
            }

            OutlinedTextField(
                value = state.durationMinutes,
                onValueChange = viewModel::onDurationChange,
                label = { Text("Duração (min)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.variantOrExpansion,
                onValueChange = viewModel::onVariantChange,
                label = { Text("Variante/expansão (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            state.photoPath?.let { path ->
                AsyncImage(model = path, contentDescription = null, modifier = Modifier.size(120.dp))
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

            Button(
                onClick = onNext,
                enabled = state.canProceedFromSessionDataStep,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Próximo")
            }
        }
    }
}
