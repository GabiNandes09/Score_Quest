package com.rogue.scorequest.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Campo "+ Adicionar" + lista de itens de texto livre com botão de excluir por linha —
 * extraído do sorteio de papéis pra ser reaproveitado em qualquer ferramenta com lista
 * customizada (papéis, opções de roleta, etc.).
 */
@Composable
fun EditableTextList(
    items: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Novo item"
) {
    var newItem by remember { mutableStateOf("") }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newItem,
                onValueChange = { newItem = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(placeholder) },
                singleLine = true
            )
            OutlinedButton(onClick = {
                onAdd(newItem)
                newItem = ""
            }) {
                Text("+ Adicionar")
            }
        }

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item, style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = { onRemove(index) }) {
                    Icon(Icons.Filled.Close, contentDescription = null)
                }
            }
        }
    }
}
