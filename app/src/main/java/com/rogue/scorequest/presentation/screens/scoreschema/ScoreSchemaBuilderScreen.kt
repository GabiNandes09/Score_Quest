package com.rogue.scorequest.presentation.screens.scoreschema

import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.ComparisonRule
import com.rogue.scorequest.domain.model.DuplicableSchema
import com.rogue.scorequest.domain.model.ScoreFieldType
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.domain.model.WinnerMode
import com.rogue.scorequest.presentation.components.CompositeFieldInputForm
import com.rogue.scorequest.presentation.viewmodel.ScoreSchemaBuilderViewModel
import com.rogue.scorequest.presentation.viewmodel.states.BuilderStep
import com.rogue.scorequest.presentation.viewmodel.states.ScoreSchemaBuilderState
import com.rogue.scorequest.ui.theme.Gold
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreSchemaBuilderScreen(
    gameId: String,
    onBackClick: () -> Unit,
    viewModel: ScoreSchemaBuilderViewModel = koinViewModel(parameters = { parametersOf(gameId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBackClick()
    }

    val onBackPressed: () -> Unit = {
        when (state.step) {
            BuilderStep.TYPE_CHOICE -> onBackClick()
            BuilderStep.FIELD_LIST -> viewModel.goToStep(BuilderStep.TYPE_CHOICE)
            BuilderStep.WINNER_ASSEMBLY -> viewModel.goToStep(BuilderStep.FIELD_LIST)
            BuilderStep.TEST_MODE -> viewModel.goToStep(BuilderStep.WINNER_ASSEMBLY)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.gameName.isNotBlank()) "Pontuação: ${state.gameName}" else "Pontuação personalizada") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (state.step) {
                    BuilderStep.TYPE_CHOICE -> TypeChoiceStep(state, viewModel)
                    BuilderStep.FIELD_LIST -> FieldListStep(state, viewModel)
                    BuilderStep.WINNER_ASSEMBLY -> WinnerAssemblyStep(state, viewModel)
                    BuilderStep.TEST_MODE -> TestModeStep(state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun TypeChoiceStep(state: ScoreSchemaBuilderState, viewModel: ScoreSchemaBuilderViewModel) {
    var showDuplicatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Como a pontuação desse jogo deve ser lançada?",
            style = MaterialTheme.typography.titleMedium
        )

        SchemaTypeCard(
            title = "Pontuação Simples",
            description = "Uma tela só, todos os jogadores juntos. Vencedor = maior pontuação.",
            selected = state.type == ScoreSchemaType.SIMPLE,
            onClick = viewModel::onSimpleChosen
        )
        SchemaTypeCard(
            title = "Pontuação Composta",
            description = "Uma tela por jogador, com campos configuráveis e vencedor manual, automático ou sem vencedor.",
            selected = state.type == ScoreSchemaType.COMPOSITE,
            onClick = viewModel::onCompositeChosen
        )
        SchemaTypeCard(
            title = "Ranking",
            description = "Todos os jogadores na mesma tela — arraste pra definir a posição de 1º a Nº. Vencedor = quem fica em 1º.",
            selected = state.type == ScoreSchemaType.RANKING,
            onClick = viewModel::onRankingChosen
        )

        if (state.type == ScoreSchemaType.RANKING) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ativar pontos por jogador",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.fields.isNotEmpty(),
                    onCheckedChange = { viewModel.onToggleRankingPoints() }
                )
            }
        }

        if (state.duplicableSchemas.isNotEmpty()) {
            OutlinedButton(onClick = { showDuplicatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Duplicar de outro jogo")
            }
        }

        if (state.type == ScoreSchemaType.SIMPLE || state.type == ScoreSchemaType.RANKING) {
            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }
        }
    }

    if (showDuplicatePicker) {
        DuplicatePickerDialog(
            options = state.duplicableSchemas,
            onSelected = { selected ->
                viewModel.onDuplicateSelected(selected)
                showDuplicatePicker = false
            },
            onDismiss = { showDuplicatePicker = false }
        )
    }
}

@Composable
private fun SchemaTypeCard(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (selected) {
                    Modifier.border(width = 1.dp, brush = Brush.linearGradient(listOf(Gold, Color.White)), shape = shape)
                } else {
                    Modifier
                }
            ),
        shape = shape
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Gold else MaterialTheme.colorScheme.onSurface
            )
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DuplicatePickerDialog(
    options: List<DuplicableSchema>,
    onSelected: (DuplicableSchema) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Duplicar de outro jogo") },
        text = {
            Column {
                options.forEach { option ->
                    Text(
                        text = option.gameName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSelected(option) }
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
private fun FieldListStep(state: ScoreSchemaBuilderState, viewModel: ScoreSchemaBuilderViewModel) {
    var showTypePicker by remember { mutableStateOf(false) }
    var configuringKind by remember { mutableStateOf<FieldKind?>(null) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Esta configuração será preenchida uma vez para cada jogador ao lançar o placar da partida.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.fields.size) { index ->
                val field = state.fields[index]
                FieldCard(
                    field = field,
                    onClick = {
                        editingIndex = index
                        configuringKind = field.kind()
                    },
                    onMoveUp = { viewModel.moveFieldUp(index) },
                    onMoveDown = { viewModel.moveFieldDown(index) },
                    onRemove = { viewModel.removeField(index) },
                    canMoveUp = index > 0,
                    canMoveDown = index < state.fields.lastIndex
                )
            }
            item {
                val addFieldShape = RoundedCornerShape(12.dp)
                Card(
                    onClick = {
                        editingIndex = null
                        showTypePicker = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(width = 1.dp, brush = Brush.linearGradient(listOf(Gold, Color.White)), shape = addFieldShape),
                    shape = addFieldShape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Gold)
                        Text(text = "Adicionar campo", color = Gold, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.goToStep(BuilderStep.WINNER_ASSEMBLY) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Continuar")
        }
    }

    if (showTypePicker) {
        FieldTypePickerDialog(
            onTypeSelected = { kind ->
                configuringKind = kind
                showTypePicker = false
            },
            onDismiss = { showTypePicker = false }
        )
    }

    configuringKind?.let { kind ->
        FieldConfigDialog(
            kind = kind,
            existing = editingIndex?.let { state.fields.getOrNull(it) },
            onConfirm = { field ->
                val index = editingIndex
                if (index != null) viewModel.updateField(index, field) else viewModel.addField(field)
                configuringKind = null
                editingIndex = null
            },
            onDismiss = {
                configuringKind = null
                editingIndex = null
            }
        )
    }
}

@Composable
private fun FieldCard(
    field: ScoreFieldType,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean
) {
    val shape = RoundedCornerShape(12.dp)
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, brush = Brush.linearGradient(listOf(Gold, Color.White)), shape = shape),
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = field.label, style = MaterialTheme.typography.bodyLarge, color = Gold, fontWeight = FontWeight.SemiBold)
                Text(
                    text = field.kind().label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                Icon(Icons.Filled.ArrowUpward, contentDescription = null)
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                Icon(Icons.Filled.ArrowDownward, contentDescription = null)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = null)
            }
        }
    }
}

@Composable
private fun WinnerAssemblyStep(state: ScoreSchemaBuilderState, viewModel: ScoreSchemaBuilderViewModel) {
    var showTermPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Montagem do vencedor", style = MaterialTheme.typography.titleMedium, color = Gold)

        WinnerModeOption("Manual — eu escolho o vencedor ao final da partida", state.winnerMode == WinnerMode.MANUAL) {
            viewModel.onWinnerModeChosen(WinnerMode.MANUAL)
        }
        WinnerModeOption("Automático — fórmula calcula o vencedor", state.winnerMode == WinnerMode.AUTOMATIC) {
            viewModel.onWinnerModeChosen(WinnerMode.AUTOMATIC)
        }
        WinnerModeOption("Sem vencedor — jogo cooperativo", state.winnerMode == WinnerMode.NONE) {
            viewModel.onWinnerModeChosen(WinnerMode.NONE)
        }

        if (state.winnerMode == WinnerMode.AUTOMATIC) {
            HorizontalDivider()
            Text(text = "Termos da fórmula", style = MaterialTheme.typography.titleSmall, color = Gold)

            state.formulaTerms.forEach { term ->
                val field = state.fields.find { it.key == term.fieldKey }
                if (field != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = field.label, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.updateFormulaTermWeight(term.fieldKey, term.weight - 1) }) {
                            Text("−")
                        }
                        Text(text = "×${if (term.weight == term.weight.toLong().toDouble()) term.weight.toLong().toString() else term.weight.toString()}")
                        IconButton(onClick = { viewModel.updateFormulaTermWeight(term.fieldKey, term.weight + 1) }) {
                            Text("+")
                        }
                        IconButton(onClick = { viewModel.removeFormulaTerm(term.fieldKey) }) {
                            Icon(Icons.Filled.Close, contentDescription = null)
                        }
                    }
                }
            }

            val addableFields = state.eligibleFieldsForFormula.filter { field ->
                state.formulaTerms.none { it.fieldKey == field.key }
            }
            if (addableFields.isNotEmpty()) {
                TextButton(onClick = { showTermPicker = true }) {
                    Text("+ Adicionar termo")
                }
            }

            Text(
                text = state.formulaPreviewText,
                style = MaterialTheme.typography.bodyMedium,
                color = Gold
            )

            HorizontalDivider()
            Text(text = "Regra de vitória", style = MaterialTheme.typography.titleSmall, color = Gold)
            WinnerModeOption("Maior total vence", state.comparisonRule == ComparisonRule.HIGHEST_WINS) {
                viewModel.onComparisonRuleChosen(ComparisonRule.HIGHEST_WINS)
            }
            WinnerModeOption("Menor total vence", state.comparisonRule == ComparisonRule.LOWEST_WINS) {
                viewModel.onComparisonRuleChosen(ComparisonRule.LOWEST_WINS)
            }

            OutlinedButton(
                onClick = { viewModel.goToStep(BuilderStep.TEST_MODE) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Testar com dados fictícios")
            }
        }

        Button(
            onClick = viewModel::save,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar")
        }
    }

    if (showTermPicker) {
        val addableFields = state.eligibleFieldsForFormula.filter { field ->
            state.formulaTerms.none { it.fieldKey == field.key }
        }
        AlertDialog(
            onDismissRequest = { showTermPicker = false },
            title = { Text("Adicionar termo") },
            text = {
                Column {
                    addableFields.forEach { field ->
                        Text(
                            text = field.label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.addFormulaTerm(field.key)
                                    showTermPicker = false
                                }
                                .padding(vertical = 12.dp)
                        )
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showTermPicker = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun WinnerModeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            color = if (selected) Gold else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun TestModeStep(state: ScoreSchemaBuilderState, viewModel: ScoreSchemaBuilderViewModel) {
    var valuesA by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var valuesB by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Preencha valores de teste — nada aqui é salvo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(text = "Jogador A", style = MaterialTheme.typography.titleMedium, color = Gold)
        CompositeFieldInputForm(
            fields = state.fields,
            values = valuesA,
            onValueChange = { key, value -> valuesA = valuesA + (key to value) }
        )
        if (state.winnerMode == WinnerMode.AUTOMATIC) {
            Text(text = "Total: ${viewModel.calculateTestTotal(valuesA) ?: "-"}", fontWeight = FontWeight.Bold)
        }

        HorizontalDivider()

        Text(text = "Jogador B", style = MaterialTheme.typography.titleMedium, color = Gold)
        CompositeFieldInputForm(
            fields = state.fields,
            values = valuesB,
            onValueChange = { key, value -> valuesB = valuesB + (key to value) }
        )
        if (state.winnerMode == WinnerMode.AUTOMATIC) {
            val totalA = viewModel.calculateTestTotal(valuesA)
            val totalB = viewModel.calculateTestTotal(valuesB)
            Text(text = "Total: ${totalB ?: "-"}", fontWeight = FontWeight.Bold)

            if (totalA != null && totalB != null) {
                val winnerText = when {
                    totalA == totalB -> "Empate"
                    state.comparisonRule == ComparisonRule.HIGHEST_WINS && totalA > totalB -> "Jogador A venceria"
                    state.comparisonRule == ComparisonRule.HIGHEST_WINS -> "Jogador B venceria"
                    state.comparisonRule == ComparisonRule.LOWEST_WINS && totalA < totalB -> "Jogador A venceria"
                    else -> "Jogador B venceria"
                }
                Text(text = winnerText, style = MaterialTheme.typography.titleMedium, color = Gold)
            }
        }

        Button(
            onClick = { viewModel.goToStep(BuilderStep.WINNER_ASSEMBLY) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar pra montagem do vencedor")
        }
    }
}
