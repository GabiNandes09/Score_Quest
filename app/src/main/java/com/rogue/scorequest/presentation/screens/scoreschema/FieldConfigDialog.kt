package com.rogue.scorequest.presentation.screens.scoreschema

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rogue.scorequest.domain.model.EnumOption
import com.rogue.scorequest.domain.model.ScoreFieldType
import java.util.Locale
import java.util.UUID

enum class FieldKind(val label: String) {
    NUMBER("Número"),
    BOOLEAN("Sim/Não"),
    ENUM("Escolha única"),
    MULTI_SELECT("Múltipla escolha"),
    TEXT("Texto")
}

fun ScoreFieldType.kind(): FieldKind = when (this) {
    is ScoreFieldType.NumberField -> FieldKind.NUMBER
    is ScoreFieldType.BooleanField -> FieldKind.BOOLEAN
    is ScoreFieldType.EnumField -> FieldKind.ENUM
    is ScoreFieldType.MultiSelectField -> FieldKind.MULTI_SELECT
    is ScoreFieldType.TextField -> FieldKind.TEXT
}

private fun generateFieldKey(label: String): String {
    val slug = label.trim().lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_").trim('_')
    val base = slug.ifBlank { "campo" }
    return "${base}_${UUID.randomUUID().toString().take(4)}"
}

@Composable
fun FieldTypePickerDialog(
    onTypeSelected: (FieldKind) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tipo de campo") },
        text = {
            Column {
                FieldKind.entries.forEach { kind ->
                    Text(
                        text = kind.label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTypeSelected(kind) }
                            .padding(vertical = 12.dp)
                    )
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun FieldConfigDialog(
    kind: FieldKind,
    existing: ScoreFieldType?,
    onConfirm: (ScoreFieldType) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(existing?.label.orEmpty()) }

    // NUMBER
    var defaultValue by remember { mutableStateOf((existing as? ScoreFieldType.NumberField)?.default?.toString() ?: "0") }
    var allowNegative by remember { mutableStateOf((existing as? ScoreFieldType.NumberField)?.allowNegative ?: false) }

    // BOOLEAN
    var pointsIfChecked by remember {
        mutableStateOf((existing as? ScoreFieldType.BooleanField)?.pointsIfChecked?.toString().orEmpty())
    }

    // ENUM / MULTI_SELECT
    val initialOptions = when (existing) {
        is ScoreFieldType.EnumField -> existing.options
        is ScoreFieldType.MultiSelectField -> existing.options
        else -> emptyList()
    }
    val options = remember { mutableStateListOf(*initialOptions.toTypedArray()) }
    var newOptionLabel by remember { mutableStateOf("") }
    var newOptionPoints by remember { mutableStateOf("0") }

    val isValid = label.isNotBlank() && (kind != FieldKind.ENUM && kind != FieldKind.MULTI_SELECT || options.isNotEmpty())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Campo: ${kind.label}") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Rótulo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                when (kind) {
                    FieldKind.NUMBER -> {
                        OutlinedTextField(
                            value = defaultValue,
                            onValueChange = { defaultValue = it.filter { c -> c.isDigit() || c == '-' } },
                            label = { Text("Valor padrão") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = allowNegative, onCheckedChange = { allowNegative = it })
                            Text("Permitir valores negativos")
                        }
                    }

                    FieldKind.BOOLEAN -> {
                        OutlinedTextField(
                            value = pointsIfChecked,
                            onValueChange = { pointsIfChecked = it.filter { c -> c.isDigit() || c == '-' } },
                            label = { Text("Pontos se marcado (vazio = só anotação)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    FieldKind.ENUM, FieldKind.MULTI_SELECT -> {
                        Text("Opções", style = MaterialTheme.typography.labelLarge)
                        options.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = option.label, modifier = Modifier.weight(1f))
                                Text(text = "${option.points} pts", style = MaterialTheme.typography.bodySmall)
                                IconButton(onClick = { options.removeAt(index) }) {
                                    Icon(Icons.Filled.Close, contentDescription = null)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newOptionLabel,
                                onValueChange = { newOptionLabel = it },
                                label = { Text("Nova opção") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newOptionPoints,
                                onValueChange = { newOptionPoints = it.filter { c -> c.isDigit() || c == '-' } },
                                label = { Text("Pts") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(80.dp)
                            )
                        }
                        TextButton(
                            onClick = {
                                if (newOptionLabel.isNotBlank()) {
                                    options.add(EnumOption(label = newOptionLabel.trim(), points = newOptionPoints.toIntOrNull() ?: 0))
                                    newOptionLabel = ""
                                    newOptionPoints = "0"
                                }
                            }
                        ) {
                            Text("+ Adicionar opção")
                        }
                    }

                    FieldKind.TEXT -> {
                        Text(
                            text = "Campo de texto livre — nunca entra na pontuação.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = {
                    val key = existing?.key ?: generateFieldKey(label)
                    val field: ScoreFieldType = when (kind) {
                        FieldKind.NUMBER -> ScoreFieldType.NumberField(
                            key = key,
                            label = label.trim(),
                            default = defaultValue.toIntOrNull() ?: 0,
                            allowNegative = allowNegative
                        )
                        FieldKind.BOOLEAN -> ScoreFieldType.BooleanField(
                            key = key,
                            label = label.trim(),
                            pointsIfChecked = pointsIfChecked.toIntOrNull()
                        )
                        FieldKind.ENUM -> ScoreFieldType.EnumField(
                            key = key,
                            label = label.trim(),
                            options = options.toList()
                        )
                        FieldKind.MULTI_SELECT -> ScoreFieldType.MultiSelectField(
                            key = key,
                            label = label.trim(),
                            options = options.toList()
                        )
                        FieldKind.TEXT -> ScoreFieldType.TextField(key = key, label = label.trim())
                    }
                    onConfirm(field)
                }
            ) {
                Text("Salvar campo")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
