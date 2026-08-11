package com.rogue.scorequest.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rogue.scorequest.domain.model.MULTI_SELECT_VALUE_SEPARATOR
import com.rogue.scorequest.domain.model.ScoreFieldType
import com.rogue.scorequest.ui.theme.Gold

/**
 * Renders one input per composite-scoring field, shared by the schema
 * builder's test mode and the real per-player scoring step in the wizard
 * (product doc 8.5.1: "reaproveitando exatamente a mesma UI").
 */
@Composable
fun CompositeFieldInputForm(
    fields: List<ScoreFieldType>,
    values: Map<String, String>,
    onValueChange: (fieldKey: String, value: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        fields.forEach { field ->
            when (field) {
                is ScoreFieldType.NumberField -> {
                    OutlinedTextField(
                        value = values[field.key] ?: field.default.toString(),
                        onValueChange = { text ->
                            val filtered = if (field.allowNegative) {
                                text.filterIndexed { index, c -> c.isDigit() || (c == '-' && index == 0) }
                            } else {
                                text.filter { it.isDigit() }
                            }
                            onValueChange(field.key, filtered)
                        },
                        label = { Text(field.label) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = Gold,
                            unfocusedLabelColor = Gold,
                            focusedBorderColor = Gold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is ScoreFieldType.BooleanField -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = values[field.key] == "true",
                            onCheckedChange = { checked -> onValueChange(field.key, checked.toString()) }
                        )
                        Text(field.label, color = Gold, style = MaterialTheme.typography.labelLarge)
                    }
                }

                is ScoreFieldType.EnumField -> {
                    Column {
                        Text(field.label, style = MaterialTheme.typography.labelLarge, color = Gold)
                        field.options.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onValueChange(field.key, option.label) }
                            ) {
                                RadioButton(
                                    selected = values[field.key] == option.label,
                                    onClick = { onValueChange(field.key, option.label) }
                                )
                                Text(option.label)
                            }
                        }
                    }
                }

                is ScoreFieldType.MultiSelectField -> {
                    val selected = values[field.key]?.split(MULTI_SELECT_VALUE_SEPARATOR)?.toSet() ?: emptySet()
                    Column {
                        Text(field.label, style = MaterialTheme.typography.labelLarge, color = Gold)
                        field.options.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val updated = if (option.label in selected) selected - option.label else selected + option.label
                                        onValueChange(field.key, updated.joinToString(MULTI_SELECT_VALUE_SEPARATOR))
                                    }
                            ) {
                                Checkbox(
                                    checked = option.label in selected,
                                    onCheckedChange = { checked ->
                                        val updated = if (checked) selected + option.label else selected - option.label
                                        onValueChange(field.key, updated.joinToString(MULTI_SELECT_VALUE_SEPARATOR))
                                    }
                                )
                                Text(option.label)
                            }
                        }
                    }
                }

                is ScoreFieldType.TextField -> {
                    OutlinedTextField(
                        value = values[field.key] ?: "",
                        onValueChange = { onValueChange(field.key, it) },
                        label = { Text(field.label) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = Gold,
                            unfocusedLabelColor = Gold,
                            focusedBorderColor = Gold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        if (fields.isEmpty()) {
            Text(
                text = "Nenhum campo configurado.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
