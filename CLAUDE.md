# ScoreQuest — Contexto do Projeto

> Este arquivo é lido automaticamente pelo Claude Code ao abrir este diretório. Ele documenta o estado do app até o momento, para que qualquer nova conversa (neste ou em outro chat) tenha contexto completo sem precisar re-explorar tudo do zero.

## O que é o app

App Android pessoal para histórico de jogatinas de jogos de tabuleiro: catálogo de jogos, estante pessoal (Tenho/Quero/Não tenho + empréstimo), registro de partidas com pontuação, jogadores locais, perfil com favoritos e feed de atividades, home com estatísticas (streak, top jogos, tempo de jogo).

- Projeto scaffolded a partir do skill `android-compose-scaffold` (mesma stack/arquitetura do projeto de referência `ShopControl`), depois implementado como V1 completo numa sessão longa.
- Documento de produto completo (modelo de dados, telas, roadmap V1/V2/V3): [ScoreQuest_Documento_de_Produto.md](ScoreQuest_Documento_de_Produto.md) — **fonte da verdade para requisitos**; este CLAUDE.md documenta o que foi **implementado e decidido durante a implementação**, não repete o doc de produto.
- V1 é **100% local (Room)**, sem contas/nuvem. V2 (contas, sync, social, API Ludopedia) e V3 (gamificação, BGG) ainda não implementados — **exceção: o Criador de Pontuação Personalizado (seção 8 do doc, nominalmente V2) foi implementado por pedido explícito do usuário, cherry-picked pra dentro do app V1**, já que não depende de conta/nuvem — só do modelo de permissão "fase atual" descrito em 8.1 (qualquer usuário edita, sem restrição). Ver seção própria abaixo.

## Stack técnica

- Kotlin **2.3.20** (⚠️ ver seção "Bug do compilador" abaixo — não é a versão original do template, foi ajustada durante a implementação).
- Jetpack Compose (Material3), Compose BOM `2026.06.01`.
- **Koin** 4.2.2 para DI (não Hilt/Dagger).
- **Room** 2.8.4 + `room-paging` para persistência local, banco `scorequest.db`, versão 1 (schema ainda não exportado — `exportSchema` não configurado, ver warning de build).
- **Paging 3** (`androidx.paging` 3.5.0) para a lista de atividades (Perfil).
- **DataStore Preferences** para a preferência de tema claro/escuro.
- **Coil 3** para carregar imagens locais (capas de jogo, avatar, foto de partida).
- **Navigation Compose** 2.9.8, com nested nav graph para o wizard de partida.
- `material-icons-core` **e** `material-icons-extended` (extended foi adicionado depois, para ícones como `Groups`, `Timer`, `FitnessCenter`, `AccessTime`, `PlayArrow` que não existem no core).
- `lifecycle-runtime-compose` 2.9.6 (⚠️ não use 2.11.x — exige AGP 9.1+, este projeto está em AGP 8.13.1).
- `minSdk 26`, `compileSdk 37` / `targetSdk 36`, JVM 17, Gradle wrapper 9.4.1.
- Retrofit/OkHttp/CameraX/ML Kit/Jsoup/WorkManager continuam no template por paridade com o ShopControl, mas **não são usados** no V1 (sem rede, sem QR/NFC-e — isso é específico do ShopControl).

## ⚠️ Bug do compilador Kotlin (já resolvido, não reintroduzir)

O template original vinha com `kotlin = "2.1.20"`. Ao implementar o V1 completo, o compilador começou a falhar com **"Internal compiler error"** (`FirIncompatibleClassExpressionChecker`, `source must not be null`) ao compilar código gerado pelo Room (`*_Impl.kt`), especificamente em DAOs com `EntityInsertAdapter`.

**Causa raiz**: `koin` 4.2.2 e `kotlinx-serialization` 1.11.0 (dependências do próprio template, não algo que eu adicionei) são publicados com metadata Kotlin 2.3.0, que o compilador 2.1.20 não consegue ler de forma confiável — o erro só se manifestava com um grafo de compilação grande o suficiente (schema Room complexo), por isso o ShopControl (mais simples) nunca bateu nesse bug com as mesmas dependências.

**Fix aplicado**: subir `kotlin` para `2.3.20`, com `kotlin-compose` (Compose Compiler) e `ksp` acompanhando a mesma linha (`ksp = "2.3.11"` — a partir do KSP 2.3.0 o versionamento **não é mais** `<kotlinVersion>-<kspVersion>`, é uma versão própria do KSP). Ver `gradle/libs.versions.toml`.

Se voltar a mexer nas versões do Kotlin/KSP/Compose-compiler neste projeto, sempre validar com `./gradlew clean compileDebugKotlin` (build limpo, sem cache) antes de assumir que "compilou" — o erro só aparece de forma confiável em build limpo; com cache de configuração/daemon quente ele pode mascarar o problema por algumas execuções.

## Arquitetura

```
data/
  local/
    dao/         — interfaces Room (@Dao)
    database/    — AppDatabase (7 entidades, version = 1)
    entity/      — @Entity + projeções @Relation (GameWithLibraryEntryEntity, SessionWithScoresEntity) + Converters (enum <-> String)
    ThemePreferences.kt — DataStore, flag de tema escuro/claro
  repository/    — um repository por agregado: BoardGameRepository, PlayerRepository, GameSessionRepository, ProfileRepository (`BoardGameRepository.deleteGame()` existe mas **não é usada em lugar nenhum** — não há tela/usecase de "excluir jogo" no V1, só edição; ver seção 8 do doc de produto e "O que NÃO existe ainda" abaixo)
domain/
  model/         — modelos de domínio (implementam Auditable: createdAt/updatedAt/deletedAt) + agregados de leitura (GameWithLibraryInfo, SessionWithDetails, HomeStats, GameStats, GameLastPlayed, StreakInfo)
  usecase/       — uma classe por operação (mesmo padrão do ShopControl — não agrupar em usecases genéricos), ~30 usecases
presentation/
  components/    — GameCoverImage (capa com placeholder de inicial, reutilizado nos cards e no detalhe), PlayerIdentityRow (padrão fixo de exibição de jogador: ícone de pessoa à esquerda do nome, troféu à direita marcando o vencedor — `onWinnerToggle` opcional torna o troféu clicável para seleção de vencedor; usado em `HomeScreen`, `SessionDetailScreen`, `ConfirmStep` e `ScoringStep` do wizard — **sempre usar este componente, não duplicar o padrão inline**)
  screens/       — uma tela por arquivo + presentation/screens/wizard/ (5 etapas do registro de partida)
  viewmodel/     — um ViewModel por tela + states/ com os data class de estado
  navigation/    — Routes.kt (rotas seladas, com sentinelas "new"/"none" para modo criação vs edição) + AppNavigation.kt (NavHost + bottom bar + FAB + nested graph do wizard)
di/              — AppModule (repos + usecases), DatabaseModule (Room), PreferencesModule (DataStore), ViewModelModule, NetworkModule (não usado no V1, mantido por paridade)
ui/theme/        — Color.kt, Theme.kt (paleta dourado/preto), Type.kt
utils/           — DateTimeExt (epoch<->LocalDateTime, toRelativeDayString), Formatters (formatDuration), ImageStorage (captura/persistência de foto)
```

**Convenções estabelecidas nesta sessão:**
- IDs de entidade são **UUID string** (não `Long autoIncrement` como no ShopControl), soft-delete via `deletedAt: LocalDateTime?` em quase todas as entidades (interface `Auditable`), colunas Room em `snake_case` via `@ColumnInfo` explícito — tudo isso decidido porque o **documento de produto** especifica esse modelo, mesmo o ShopControl não usando nada disso.
- Joins comuns feitos via projeção Room `@Relation` (`GameWithLibraryEntryEntity`, `SessionWithScoresEntity`) em vez de combinar Flows na mão no repository — mais simples e reativo.
- `GameSession.participantIds` **não é coluna** — é derivado das linhas de `ScoreEntry` daquela sessão (evita duplicar dado).
- Um Koin `viewModel { }` por tela; telas com parâmetro de rota (gameId, sessionId) usam `koinViewModel(parameters = { parametersOf(...) })`. O wizard de partida usa uma técnica diferente: ViewModel **escopado ao nested nav graph** (`koinViewModel(viewModelStoreOwner = navController.getBackStackEntry(graphRoute))`), compartilhado pelas 5 etapas — ver `wizardViewModel()` em `AppNavigation.kt`.
- Sentinelas de rota (`"new"` para sessão/jogo novo, `"none"` para "sem jogo pré-selecionado") em vez de argumentos nullable do Navigation Compose — mais simples de tipar e de tratar no `NavType.StringType`.
- Auto-avanço de etapa de wizard (usado para pular a escolha de jogo quando já vem pré-selecionado) é implementado como um flag **consumível uma única vez** no ViewModel (`consumeAutoAdvance()`), não um efeito colateral direto na navegação — isso evita que o usuário fique "preso" ao voltar (back) para aquela etapa.
- **Limpeza de imagens locais órfãs** (seção 8 do doc de produto): `ImageStorage.deleteImage(path)` já existia mas não era chamada em lugar nenhum — capa de jogo, avatar e foto de partida vazavam arquivo toda vez que o usuário trocava a imagem antes de salvar. Cada um dos 3 ViewModels que capturam imagem (`AddEditGameViewModel`, `EditProfileViewModel`, `AddSessionViewModel`) agora guarda o path original carregado do banco (`original*Path`) e aplica duas limpezas: (1) no callback `onXCaptured`, se já havia um path pendente **diferente do original** (ou seja, uma captura anterior desta mesma sessão de edição, nunca persistida), apaga na hora — seguro porque nada no banco aponta pra ele; (2) em `save()`, só depois do update ter sucesso, se o path original for diferente do final, apaga o original — **nunca apaga o original antes de confirmar o save**, senão cancelar a edição deixaria o banco apontando pra um arquivo já excluído.

## ⚠️ Bug de crash de câmera já corrigido — cuidado ao reintroduzir

O app declara `<uses-permission android:name="android.permission.CAMERA"/>` no manifest (necessário por causa de `<uses-feature android:name="android.hardware.camera" android:required="false"/>`). Isso tem um efeito colateral conhecido do Android: uma vez que o app **declara** essa permissão, disparar o intent implícito `ACTION_IMAGE_CAPTURE` (via `ActivityResultContracts.TakePicture()`) **exige que a permissão também esteja concedida em runtime**, mesmo o app nunca chamando a câmera diretamente — sem isso, o sistema derruba o app com `SecurityException` ao tocar em "Câmera". Os três pontos que capturam foto (`AddEditGameScreen`, `EditProfileScreen`, `SessionDataStep`) faziam `takePictureLauncher.launch(uri)` direto, sem checar a permissão antes — corrigido centralizando a lógica em `presentation/components/CameraCapture.kt` (`rememberCameraCaptureAction(onCaptured)`): checa `ContextCompat.checkSelfPermission`, se não concedida dispara `ActivityResultContracts.RequestPermission()` primeiro, só then lança a captura. **Os três call sites agora usam esse helper — não voltar a chamar `takePictureLauncher.launch()` direto sem passar por ele.**

## ⚠️ Bug de layout já corrigido (Compose) — cuidado ao reintroduzir

Em telas com `Column { LazyColumn(Modifier.fillMaxSize()) {...}; Button(...) }`, o `fillMaxSize()` da `LazyColumn` consome todo o espaço da Column e **empurra o Button pra fora da tela** (inacessível). Isso já aconteceu em `PlayersStep.kt` e `ScoringStep.kt` e foi corrigido trocando para `Modifier.weight(1f).fillMaxWidth()`. Regra geral: `fillMaxSize()` numa lista só é seguro quando ela é o **último/único filho** da Column; se vier algo depois (botão, etc.), usar `weight(1f)`.

## Banco de dados

7 entidades: `BoardGameEntity`, `UserLibraryEntryEntity` (PK = `gameId`, um usuário local só), `PlayerEntity`, `GameSessionEntity`, `ScoreEntryEntity` (PK composta `session_id`+`player_id`), `UserProfileEntity` (linha única, id fixo `"local"`), `FavoriteGameEntity` (máx. 3, ver `SetFavoriteGameUseCase`).

**Lacunas do documento de produto preenchidas durante a implementação**: `UserProfileEntity` e `FavoriteGameEntity` não estavam na seção 4.2 do doc mas são necessárias pras telas de Perfil (7.4) — foram adicionadas como extensão natural.

**Regra de negócio implícita implementada**: ao salvar uma partida (`SaveGameSessionUseCase` → `GameSessionRepository.ensureLibraryEntryPlayed`), se o jogo não tem `UserLibraryEntry`, cria uma com `status = DONT_HAVE, played = true`; se já tem mas `played = false`, atualiza pra `true`. Isso é a regra da seção 8 do doc de produto ("UserLibraryEntry auto-criada").

## Navegação e telas

Bottom bar (4 abas): **Home**, **Jogos**, **Perfil**, **Configurações** — visível só nessas 4 rotas. FAB dourado sobreposto (visível junto com a bottom bar) abre o wizard de registro de partida.

- **Home** (reescrita pra seguir a seção 7.1 revisada do doc de produto): saudação + sino, botão "Registrar partida", card de estatísticas com 4 itens (partidas, **esta semana**, total de horas, streak), card "Última jogatina", e 4 widgets de gráfico abaixo:
  - **Atividade recente** (`ActivityHeatmap`): grade estilo GitHub, 90 dias, 13 colunas x 7 linhas (dias agrupados sequencialmente em blocos de 7, não alinhado ao calendário Dom-Sáb), 5 níveis de cor (`#2C2C2C`→`Gold`), toque num quadrado mostra data+contagem abaixo da grade. Sem estado vazio especial — dias sem partida já renderizam na cor base.
  - **Ranking dos mais jogados** (`HorizontalBarChart`, top 5): `BarChartEntry` ganhou `highlighted` (só a barra #1 fica dourada, resto em `onSurfaceVariant`) e `onClick` (toque navega pro detalhe do jogo, `onGameClick(gameId)` até `AppNavigation`). Estado vazio: texto "Jogue sua primeira partida...".
  - **Partidas por mês** (`LineChart` com `showAreaFill = true`, gradiente dourado→transparente abaixo da linha): ano corrente, Jan-Dez fixos (zero-preenchido), **não** é mais janela rolante de 12 meses nem soma tempo de jogo — conta nº de partidas. Se menos de 3 meses tiverem partida, mostra texto simples em vez do gráfico.
  - **Duração das partidas** (`VerticalBarChart`, novo componente, barras verticais ancoradas embaixo): 4 faixas fixas (0-30min/30-60min/1-2h/2h+), bucketizado em Kotlin (não em SQL) a partir de `getAllSessionDurations()`. Card inteiro só aparece se `totalSessions >= 5`.

  Usecases: `GetActivityHeatmapUseCase`, `GetSessionsByMonthUseCase`, `GetDurationHistogramUseCase` (substituíram `GetMonthlyPlaytimeUseCase`, removido). `GetHomeStatsUseCase` voltou a expor `weekMinutes` (soma desde a última segunda-feira) junto com `topGames` (agora limit 5) e `totalMinutes`.

  **Decisão consciente, divergindo do doc**: a seção 7.1 do doc de produto sugere a biblioteca **Vico** (`com.patrykandpatrick.vico`) pros gráficos de barra/linha/histograma. Mantive os componentes Compose feitos à mão (`HorizontalBarChart`, `VerticalBarChart`, `LineChart`, todos em `presentation/components/`) em vez de adotar a lib nova — não há emulador neste ambiente pra validar visualmente uma API desconhecida e ainda em evolução rápida (como o próprio doc observa), enquanto os componentes hand-rolled já são conhecidos, compilam e cobrem exatamente as cores/formas pedidas. Se quiser migrar pra Vico depois, é um passo isolado (trocar só a camada de renderização, os usecases/dados já estão prontos).
- **Jogos**: grade de 2 colunas com cards **quadrados** (imagem preenchendo tudo + nome embaixo à esquerda). Busca/ordenação/filtro de categoria ficam escondidos atrás de um ícone de lupa (`AnimatedVisibility` expand/collapse). 3 tabs (não 4 — "Todos" foi removido): **Estante** (só status Tenho), **Desejo** (status Quero), **Jogado** (`played = true`) — texto da tab selecionada fica dourado, com transição `AnimatedContent` (fade + slide) ao trocar de tab. Ordenação: A-Z, Z-A, Recente (mais jogado recentemente primeiro, ▼), Recente reverso (nunca jogados primeiro por ordem alfabética, depois os jogados há mais tempo, ▲) — baseado em `GetLastPlayedDatesUseCase` (MAX(date) por jogo).
- **Detalhe do jogo**: imagem de fundo do "header" (280dp, com botões voltar/editar flutuando por cima, gradiente escuro pra legibilidade) → nome + estrelas à esquerda → 3 ícones (jogadores/tempo/peso) → dropdown de status **com gradiente dourado→branco e texto preto** (`StatusDropdown`, não é o `ExposedDropdownMenuBox` padrão do M3, é um `Box` customizado com `Brush.horizontalGradient`) → checkbox "Emprestado" (só aparece se status = Tenho; campo "Emprestado para" só aparece se o checkbox estiver marcado) → card de Estatísticas com 3 ícones lado a lado (PlayArrow=vezes jogadas, Timer=horas de jogo total, AccessTime=tempo médio) → botão **"Configurar/Editar pontuação personalizada"** (texto muda conforme `state.hasScoreSchema`, abre `ScoreSchemaBuilderScreen` — ver seção própria abaixo) → histórico de partidas (cada linha é clicável, `onSessionClick(sessionId)`, navega pra `SessionDetail` — mesma rota/tela já usada a partir do feed de Atividades do Perfil) → **FAB** "Registrar partida" que abre o wizard **com aquele jogo pré-selecionado** (pula a etapa de escolha de jogo automaticamente na primeira entrada, via `consumeAutoAdvance()`).
- **Adicionar/Editar jogo**: mesma tela pra criar e editar (`AddEditGameViewModel` detecta pelo sentinela `gameId == "new"`); em modo edição não mostra o seletor de status (isso é gerenciado na tela de detalhe).
- **Wizard de partida** (`presentation/screens/wizard/`): Escolher jogo → Dados da sessão (data/duração/variante/foto) → Jogadores (busca + criar novo) → **Pontuação** (branch: grid simples se o jogo não tem schema ou schema é `SIMPLE`; `CompositeScoringStep` — uma tela por jogador em sequência — se o schema é `COMPOSITE`, ver seção "Pontuação personalizada" abaixo) → **Confirmação** (`ConfirmStep.kt`, também ramificada pro caso Composta: seletor de vencedor Manual, totais calculados + diálogo de empate no Automático, ou nada no Sem-vencedor) — **conteúdo dentro de 2 `Card`s** (resumo da partida com `StatIconItem` pra data/duração; jogadores+pontuação com título dourado), coluna com `verticalScroll` pra não cortar lista longa de jogadores — corrigido depois de sair "fora do padrão" (era só `Text` solto sem Card, único lugar do app assim). Suporta edição (reaproveita as mesmas telas, `sessionId` real em vez de `"new"`) e pré-seleção de jogo (`gameId` na rota, sentinela `"none"` quando não vem de lugar nenhum).
- **Perfil**: favoritos (editável, máx. 3, com diálogo de confirmação pra substituir o mais antigo) + Atividades (lista paginada via Paging 3 / `LazyPagingItems`).
- **Configurações**: toggle de tema, "Gerenciar jogadores locais" (renomear/excluir — exclusão bloqueada se o jogador tem histórico de partidas).

## Pontuação personalizada (seção 8 do doc de produto)

Construtor visual completo (`GameScoreSchema` por jogo) + integração real com o fluxo de lançamento de partida. Implementado por pedido explícito do usuário mesmo sendo nominalmente V2 — ver nota no topo do arquivo.

**Modelo de dados**: `GameScoreSchema` (`domain/model/GameScoreSchema.kt`) com `type` (SIMPLE/COMPOSITE), `fields: List<ScoreFieldType>` (sealed class: NumberField/BooleanField/EnumField/MultiSelectField/TextField, todas `@Serializable`), `winnerMode` (MANUAL/AUTOMATIC/NONE), `formula: ScoreFormula?` (terms + comparisonRule). Entidade Room (`GameScoreSchemaEntity`) usa **`gameId` como `@PrimaryKey` diretamente** (mesmo padrão de `UserLibraryEntryEntity`) — garante "um schema por jogo" via constraint de banco, sem precisar da semântica condicional-a-soft-delete que o doc original sugeria (o doc já reconhece essa imprecisão; aqui resolvida de forma direta). `fields`/`formula` persistidos como JSON (kotlinx.serialization) em colunas TEXT, não como tabelas relacionais. `ScoreEntry.fieldValues: Map<String, String>?` guarda os valores lançados por jogador por partida, também como JSON — valores de campo sempre viram `String` (número vira string, boolean "true"/"false", multi-select junta labels com `"||"` — constante `MULTI_SELECT_VALUE_SEPARATOR`), decodificados por tipo de campo só na hora de calcular/exibir.

**Construtor** (`presentation/screens/scoreschema/ScoreSchemaBuilderScreen.kt` + `ScoreSchemaBuilderViewModel`): entrada única em `score_schema_builder/{gameId}` (`Routes.ScoreSchemaBuilder`), a partir do botão na tela de detalhe do jogo — a mesma tela serve tanto pra criar quanto pra editar (carrega o schema existente se houver). Não é um nested nav graph como o wizard de partida — é **uma máquina de estados de tela única** (`BuilderStep`: TYPE_CHOICE → FIELD_LIST → WINNER_ASSEMBLY → TEST_MODE), navegação interna via `viewModel.goToStep()`, botão voltar da TopAppBar volta um step (ou sai da tela no TYPE_CHOICE). Decisão deliberada de simplificação: menos telas/rotas novas pra manter, dado que os sub-passos não precisam ser deep-linkáveis.

- **TYPE_CHOICE**: Simples (salva direto) vs Composta (avança) vs "Duplicar de outro jogo" (pré-popula campos/fórmula de outro schema Composto existente, permanece editável).
- **FIELD_LIST**: lista de campos com editar/mover/remover; **reordenação é por botões cima/baixo (`ArrowUpward`/`ArrowDownward`), não drag-and-drop** — o doc sugere drag-and-drop mas isso exigiria detecção de gesto customizada sem forma de validar visualmente neste ambiente (sem emulador); resultado funcional é o mesmo. Adicionar campo abre `FieldTypePickerDialog` (5 tipos) → `FieldConfigDialog` (formulário específico do tipo, incluindo lista de opções com pontos pra Enum/MultiSelect). `key` de cada campo é gerado automaticamente a partir do label (slug + sufixo aleatório), nunca editado pelo usuário.
- **WINNER_ASSEMBLY**: Manual/Automático/Sem vencedor. No Automático: termos da fórmula (campo + peso via stepper +/-1), preview textual da fórmula (`state.formulaPreviewText`), regra de comparação (maior/menor vence), botão pro modo de teste.
- **TEST_MODE**: reaproveita `CompositeFieldInputForm` (`presentation/components/`, também usado no lançamento real) com 2 jogadores fictícios, mostra total calculado ao vivo se Automático — nada é persistido.

**Paleta**: os componentes do construtor e da pontuação composta seguem o mesmo padrão visual já usado no resto do app (não usavam no primeiro pass, corrigido depois) — cards com **borda fina em gradiente dourado→branco** (`Modifier.border(1.dp, Brush.linearGradient(listOf(Gold, Color.White)), shape)`, mesmo padrão do card "Última jogatina" da Home) em `SchemaTypeCard`, `FieldCard` e no card "Adicionar campo"; **rótulos de campo em dourado** (`color = Gold`) em `CompositeFieldInputForm` (Boolean/Enum/MultiSelect) e nos `OutlinedTextField` do `FieldConfigDialog` (`goldTextFieldColors()`, cor do rótulo e da borda focada); título da opção selecionada em `SchemaTypeCard`/`WinnerModeOption` também fica dourado, mesmo padrão da tab selecionada na tela Jogos. Botões/RadioButton/Checkbox não precisam de override — já usam `colorScheme.primary` (= Gold) por padrão via `Theme.kt`.

**Integração com o wizard de partida**: `AddSessionViewModel` expõe `schema: StateFlow<GameScoreSchema?>` (deriva de `selectedGameId` via `flatMapLatest`). Depois da etapa Jogadores, a navegação em `AppNavigation.kt` verifica `schema.value?.type` e vai pra `WizardScoring` (tela genérica existente, cobre SIMPLE e "sem schema") ou `WizardCompositeScoring` (`CompositeScoringStep.kt`, novo — um jogador por vez, `CompositeFieldInputForm` compartilhado com o modo de teste do construtor, navegação "Jogador anterior/Próximo jogador" **dentro da própria tela**, não uma rota por jogador). Na Confirmação (`ConfirmStep.kt`, ramificada por `schema?.winnerMode`): Manual mostra lista de jogadores com `RadioButton` pra escolher o vencedor (obrigatório, Salvar fica desabilitado até escolher); Automático calcula o total de cada jogador (`CalculateScoreFormulaUseCase`, classe pura sem dependência de repositório) e, se detectar empate no topo, mostra `TieBreakDialog` ("Considerar empate" = ambos vencedores / "Escolher manualmente" = abre lista dos empatados); Sem-vencedor não mostra nada de vencedor.

**Ponto de atenção pra manter**: qualquer mudança em `state.compositeFieldValues` reseta `automaticWinnerIds`/`pendingTieCandidateIds` (ver `onCompositeFieldChange`), forçando recálculo — sem isso um empate resolvido ficaria "preso" mesmo depois do usuário editar uma pontuação e o empate deixar de existir.

## Tema

Paleta da seção 1 do documento de produto: fundo `#121212`/superfície `#1E1E1E` (escuro, padrão) ou claro alternativo, dourado `#D4AF37` como cor de destaque (`Gold` em `ui/theme/Color.kt`). Tema escolhido persiste via DataStore (`ThemePreferences`), lido em `MainActivity` com `collectAsState(initial = true)` (padrão = escuro).

## Fluxo de build

- **Sempre rodar `./gradlew compileDebugKotlin` após qualquer alteração de código**, sem esperar o usuário pedir. Se mexer em dependências/versões do Kotlin, rodar limpo (`./gradlew clean compileDebugKotlin`) por causa do bug de compilador documentado acima.
- `local.properties` aponta pro mesmo SDK do ShopControl (`sdk.dir=...\Android\Sdk`) — já configurado, não versionado (`.gitignore`).
- Não há emulador Android disponível neste ambiente de desenvolvimento, mas o usuário costuma ter um **device físico conectado via `adb`** (`adb devices -l`) — quando houver um listado, rodar `./gradlew installDebug` após o `assembleDebug` bem-sucedido para instalar a build mais recente nele. Sem device conectado, a verificação fica só na compilação.
- Builds costumam levar 10-30s incrementais; a primeira após mudar `libs.versions.toml` pode levar 1-3 min e ocasionalmente precisa de uma segunda tentativa (cache de configuração invalidado gera um "Unresolved reference 'R'" transitório na primeira tentativa — rodar de novo resolve).

## O que NÃO existe ainda (V1 propositalmente incompleto)

- **Onboarding** (seção 7.7 do doc) — decisão explícita do usuário de deixar pra depois.
- **Exportar dados / backup** (seção 7.5) — decisão explícita do usuário de deixar pra depois.
- **Seed de acervo curado** (~100-300 jogos, seção 5.1) — decisão explícita de não popular; acervo começa vazio, só cresce via "Adicionar Jogo" manual.
- Filtros de nº de jogadores/duração na tela Jogos (só tem busca por nome + tabs de status + categoria).
- Tudo de V2/V3 do roadmap (contas, sync, social, API Ludopedia/BGG, gamificação, construtor de fórmula de pontuação avançado — V1 só tem pontuação genérica: total numérico + vencedor).
