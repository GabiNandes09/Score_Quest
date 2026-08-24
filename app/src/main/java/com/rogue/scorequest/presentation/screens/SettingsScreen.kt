package com.rogue.scorequest.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.ImportResult
import com.rogue.scorequest.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val content = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
        }.getOrNull()

        if (content != null) {
            viewModel.onJsonSelected(content)
        } else {
            viewModel.onJsonReadError("Não foi possível ler o arquivo selecionado.")
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val pending = state.pendingExport
        if (uri != null && pending != null) {
            val success = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(pending.json.toByteArray()) }
            }.isSuccess

            if (success) {
                viewModel.onExportWritten(pending)
            } else {
                viewModel.onExportError("Não foi possível salvar o arquivo selecionado.")
            }
        }
        viewModel.onExportLaunched()
    }

    LaunchedEffect(state.pendingExport) {
        if (state.pendingExport != null) {
            exportLauncher.launch("scorequest_jogos.json")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tema escuro")
                Switch(checked = state.isDarkTheme, onCheckedChange = viewModel::onThemeToggled)
            }

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                enabled = !state.isImporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.isImporting) "Importando..." else "Importar JSON")
            }

            OutlinedButton(
                onClick = viewModel::onExportRequested,
                enabled = !state.isExporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.FileUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.isExporting) "Exportando..." else "Exportar JSON")
            }

            Text(
                text = "ScoreQuest — v1.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    state.importResult?.let { result ->
        ImportResultDialog(result = result, onDismiss = viewModel::dismissImportResult)
    }

    state.importReadError?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissImportReadError,
            title = { Text("Erro ao importar") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissImportReadError) { Text("OK") }
            }
        )
    }

    state.exportSuccessMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissExportSuccess,
            title = { Text("Exportação concluída") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissExportSuccess) { Text("OK") }
            }
        )
    }

    state.exportError?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissExportError,
            title = { Text("Erro ao exportar") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissExportError) { Text("OK") }
            }
        )
    }
}

@Composable
private fun ImportResultDialog(result: ImportResult, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importação concluída") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${result.gamesAdded} jogo(s) adicionado(s)")
                Text("${result.gamesUpdated} jogo(s) atualizado(s)")
                Text("${result.schemasImported} pontuação(ões) personalizada(s) importada(s)")
                if (result.errors.isNotEmpty()) {
                    Text(
                        text = "Erros:",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    result.errors.forEach { error ->
                        Text(text = "• $error", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}
