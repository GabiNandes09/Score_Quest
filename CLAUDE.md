# ScoreQuest — Contexto do Projeto

> Este arquivo é lido automaticamente pelo Claude Code ao abrir este diretório. Ele documenta o estado do app até o momento, para que qualquer nova conversa (neste ou em outro chat) tenha contexto completo sem precisar re-explorar tudo do zero.

## O que é o app

App Android pessoal para histórico de jogatinas de jogos de tabuleiro: catálogo de jogos, estante pessoal (Tenho/Quero/Não tenho + empréstimo), registro de partidas com pontuação, jogadores locais, perfil com favoritos e feed de atividades, home com estatísticas (streak, top jogos, tempo de jogo).

- Projeto scaffolded a partir do skill `android-compose-scaffold` (mesma stack/arquitetura do projeto de referência `ShopControl`), depois implementado como V1 completo numa sessão longa.
- Documento de produto completo (modelo de dados, telas, roadmap V1/V2/V3): [ScoreQuest_Documento_de_Produto.md](ScoreQuest_Documento_de_Produto.md) — **fonte da verdade para requisitos**; este CLAUDE.md documenta o que foi **implementado e decidido durante a implementação**, não repete o doc de produto.
- V1 é **100% local (Room)**, sem contas/nuvem. **Exceção: o Criador de Pontuação Personalizado (seção 8 do doc, nominalmente V2) foi implementado por pedido explícito do usuário, cherry-picked pra dentro do app V1**, já que não depende de conta/nuvem — só do modelo de permissão "fase atual" descrito em 8.1 (qualquer usuário edita, sem restrição). Ver seção própria abaixo.

## Stack técnica

- Kotlin **2.3.20** (⚠️ ver seção "Bug do compilador" abaixo — não é a versão original do template, foi ajustada durante a implementação).
- Jetpack Compose (Material3), Compose BOM `2026.06.01`.
- **Koin** 4.2.2 para DI (não Hilt/Dagger).
- **Room** 2.8.4 + `room-paging` para persistência local, banco `scorequest.db`, versão **2** (schema ainda não exportado — `exportSchema` não configurado, ver warning de build). Subiu de 1→2 com uma `Migration` real (`data/local/database/Migrations.kt`, `MIGRATION_1_2`, registrada em `DatabaseModule.kt` via `.addMigrations(...)`) pra adicionar a tabela `active_timer` (ver "Cronômetro de partida ao vivo" abaixo) — só `CREATE TABLE`, não toca nas 8 tabelas anteriores. Continua **sem** `fallbackToDestructiveMigration()`; se subir a versão de novo, sempre escrever uma `Migration` de verdade (ou confirmar com o usuário aceitar perda de dados) — nunca assumir destructive migration silenciosamente, já que o app tem dado real do usuário.
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
  repository/    — um repository por agregado: BoardGameRepository, PlayerRepository, GameSessionRepository, ProfileRepository (`BoardGameRepository.deleteGame()` existe mas **não é usada em lugar nenhum** — não há tela/usecase de "excluir jogo" no V1, só edição)
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

## ⚠️ Bug de orientação de foto já corrigido — cuidado ao reintroduzir

`ImageStorage.persistImage()` (`utils/ImageStorage.kt`, usado por toda captura/seleção de imagem do app — capa de jogo, avatar, foto de partida, tanto câmera quanto galeria) decodificava a imagem com `BitmapFactory.decodeStream()` e recomprimia num JPEG novo **sem nunca ler a tag EXIF de orientação**. A câmera grava os pixels na orientação bruta do sensor e conta com essa tag pra girar na exibição; como a recompressão descartava a tag sem aplicar a rotação correspondente, a foto persistida saía sempre girada (retrato virava paisagem e vice-versa). Corrigido lendo `ExifInterface.TAG_ORIENTATION` (`android.media.ExifInterface`, plataforma — sem dependência nova) a partir dos bytes originais antes de decodificar, e aplicando a rotação (`Matrix.postRotate` + `Bitmap.createBitmap`) no bitmap antes de redimensionar/salvar (`rotateIfNeeded`, roda antes de `resizeIfNeeded`). **Se voltar a mexer em `persistImage()`, manter a leitura do EXIF antes do `BitmapFactory.decode*` — decodificar direto do stream sem isso reintroduz o bug.**

## ⚠️ Bug de layout já corrigido (Compose) — cuidado ao reintroduzir

Em telas com `Column { LazyColumn(Modifier.fillMaxSize()) {...}; Button(...) }`, o `fillMaxSize()` da `LazyColumn` consome todo o espaço da Column e **empurra o Button pra fora da tela** (inacessível). Isso já aconteceu em `PlayersStep.kt` e `ScoringStep.kt` e foi corrigido trocando para `Modifier.weight(1f).fillMaxWidth()`. Regra geral: `fillMaxSize()` numa lista só é seguro quando ela é o **último/único filho** da Column; se vier algo depois (botão, etc.), usar `weight(1f)`.

## Banco de dados

Entidades principais: `BoardGameEntity`, `UserLibraryEntryEntity` (PK = `gameId`, um usuário local só), `PlayerEntity`, `GameSessionEntity`, `ScoreEntryEntity` (PK composta `session_id`+`player_id`), `UserProfileEntity` (linha única, id fixo `"local"`), `FavoriteGameEntity` (máx. 3, ver `SetFavoriteGameUseCase`), mais `GameScoreSchemaEntity`, `ActiveTimerEntity`, `PlayerGroupEntity`/`PlayerGroupMemberEntity` (ver seções próprias). `version = 3` (subiu 2→3 com `MIGRATION_2_3`, ver "Grupos de jogadores" abaixo).

**Lacunas do documento de produto preenchidas durante a implementação**: `UserProfileEntity` e `FavoriteGameEntity` não estavam na seção 4.2 do doc mas são necessárias pras telas de Perfil (7.4) — foram adicionadas como extensão natural.

**Regra de negócio implícita implementada**: ao salvar uma partida (`SaveGameSessionUseCase` → `GameSessionRepository.ensureLibraryEntryPlayed`), se o jogo não tem `UserLibraryEntry`, cria uma com `status = DONT_HAVE, played = true`; se já tem mas `played = false`, atualiza pra `true`. Isso é a regra da seção 8 do doc de produto ("UserLibraryEntry auto-criada").

## Navegação e telas

Bottom bar (5 abas, nesta ordem): **Extras**, **Jogos**, **Home**, **Jogadores**, **Perfil**
— visível só nessas 5 rotas (`mainTabRoutes` em `AppNavigation.kt`). A ordem de renderização
em `ScoreQuestBottomBar` é escolha deliberada do usuário, reordenada mais de uma vez conforme
o gosto (Extras — antes "Ferramentas", nome encurtado por decisão do usuário — na ponta
esquerda, Home no meio) — não segue a ordem "natural" de quando cada aba foi adicionada ao
código. **O item Home é visualmente destacado dos outros 4** (pedido do usuário): ícone dentro
de um círculo dourado sólido 40dp (`Box` + `clip(CircleShape)` + `background(Gold)`, ícone
preto por cima, maior que o ícone simples dos outros itens) e label em negrito dourado — o
indicador de seleção padrão do `NavigationBarItem` é desligado só nesse item
(`NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)`) pra não desenhar um
pill de seleção por baixo do círculo dourado que já é permanente (não muda com seleção). FAB
dourado sobreposto (visível junto com a bottom bar, exceto em Home e Extras) abre o wizard de
registro de partida.

**Configurações deixou de ser aba** (decisão do usuário) — agora é uma tela empurrada normal (`Routes.Settings`, fora de `mainTabRoutes`, some a bottom bar/FAB como qualquer tela de detalhe), acessada por um ícone de engrenagem nas `actions` da TopAppBar do Perfil (ao lado do botão "Editar"), com botão de voltar próprio. **"Gerenciar jogadores locais" trocou de lugar com ela** — antes vivia dentro de Configurações (acessada por um `TextButton`), agora é a própria aba "Jogadores" da bottom bar (`ManagePlayersScreen`, sem botão de voltar — como as outras 3 abas principais, título simples "Jogadores" na TopAppBar). `SettingsScreen`/`ManagePlayersScreen` trocaram de assinatura de acordo (`onManagePlayersClick` saiu de `SettingsScreen`; `onBackClick` saiu de `ManagePlayersScreen`, entrou em `SettingsScreen`).

- **Home** (reescrita pra seguir a seção 7.1 revisada do doc de produto): saudação + sino + **ícone de engrenagem à direita do sino** (abre `WidgetSettingsDialog`, ver "Visibilidade dos widgets da Home" abaixo), **banner de partida em andamento** (só aparece se houver um cronômetro ativo — ver "Cronômetro de partida ao vivo" abaixo), botão "Registrar partida", botão "Iniciar partida ao vivo", card de estatísticas com 4 itens (partidas, **esta semana**, total de horas, streak), card "Última jogatina", e 5 widgets de gráfico abaixo (cada um ocultável, ver seção própria):
  - **Atividade recente** (`ActivityHeatmap`): grade estilo GitHub, 90 dias, 13 colunas x 7 linhas (dias agrupados sequencialmente em blocos de 7, não alinhado ao calendário Dom-Sáb), 5 níveis de cor (`#2C2C2C`→`Gold`), toque num quadrado mostra data+contagem abaixo da grade. Sem estado vazio especial — dias sem partida já renderizam na cor base.
  - **Ranking dos mais jogados** (`HorizontalBarChart`, top 5): `BarChartEntry` ganhou `highlighted` (só a barra #1 fica dourada, resto em `onSurfaceVariant`) e `onClick` (toque navega pro detalhe do jogo, `onGameClick(gameId)` até `AppNavigation`). Estado vazio: texto "Jogue sua primeira partida...".
  - **Mais vitórias** (`HorizontalBarChart`, top 5 jogadores): mesmo componente/padrão visual do ranking de jogos acima, mas por jogador — `PlayerWinCount` (`domain/model/HomeStats.kt`) vem de uma query nova em `ScoreEntryDao.getTopPlayersByWins` (join `score_entry`+`game_session`+`player`, filtra `is_winner = 1`, agrupa por jogador, ordena por contagem desc). Toque na barra navega pro detalhe do jogador (`onPlayerClick(playerId)` até `AppNavigation` → `Routes.PlayerDetail`). `GetHomeStatsUseCase` combina essa flow junto das demais (usa o overload de 5 flows do `combine`).
  - **Partidas por mês** (`LineChart` com `showAreaFill = true`, gradiente dourado→transparente abaixo da linha): ano corrente, Jan-Dez fixos (zero-preenchido), **não** é mais janela rolante de 12 meses nem soma tempo de jogo — conta nº de partidas. Se menos de 3 meses tiverem partida, mostra texto simples em vez do gráfico.
  - **Duração das partidas** (`VerticalBarChart`, novo componente, barras verticais ancoradas embaixo): 4 faixas fixas (0-30min/30-60min/1-2h/2h+), bucketizado em Kotlin (não em SQL) a partir de `getAllSessionDurations()`. Card inteiro só aparece se `totalSessions >= 5`.

  Usecases: `GetActivityHeatmapUseCase`, `GetSessionsByMonthUseCase`, `GetDurationHistogramUseCase` (substituíram `GetMonthlyPlaytimeUseCase`, removido). `GetHomeStatsUseCase` voltou a expor `weekMinutes` (soma desde a última segunda-feira) junto com `topGames` (agora limit 5) e `totalMinutes`.

  **Visibilidade dos widgets da Home**: `domain/model/HomeWidget.kt` (enum com os 5 widgets acima + `label`) + `ThemePreferences` (`data/local/ThemePreferences.kt`, mesma classe/DataStore já usada pro tema — **não** criar um segundo `preferencesDataStore(name = "settings")` em outro arquivo, isso derruba o app em runtime com "There are multiple DataStores active for the same file", o DataStore precisa ser um singleton por nome de arquivo) guarda só os widgets **escondidos** (`stringSetPreferencesKey`, um `Set<String>` com o `.name` do enum) — ausência da chave = tudo visível por padrão, sem precisar popular o Set inteiro na primeira leitura. `GetHomeWidgetVisibilityUseCase`/`SetHomeWidgetVisibleUseCase` expõem isso pro `HomeViewModel` (`state.visibleWidgets: Set<HomeWidget>`). Cada `if (HomeWidget.X in state.visibleWidgets) { ... }` envolve o card correspondente em `HomeScreen.kt` (Duração das partidas mantém as duas condições, visibilidade **e** `totalSessions >= 5`). Ícone de engrenagem (`Icons.Filled.Settings`) na Home abre `WidgetSettingsDialog` — `AlertDialog` simples com um `Switch` por widget, mesmo padrão do toggle de tema em `SettingsScreen.kt` — sem precisar de rota/tela nova, já que é só 5 switches.

  **Decisão consciente, divergindo do doc**: a seção 7.1 do doc de produto sugere a biblioteca **Vico** (`com.patrykandpatrick.vico`) pros gráficos de barra/linha/histograma. Mantive os componentes Compose feitos à mão (`HorizontalBarChart`, `VerticalBarChart`, `LineChart`, todos em `presentation/components/`) em vez de adotar a lib nova — não há emulador neste ambiente pra validar visualmente uma API desconhecida e ainda em evolução rápida (como o próprio doc observa), enquanto os componentes hand-rolled já são conhecidos, compilam e cobrem exatamente as cores/formas pedidas.
- **Jogos**: grade de 2 colunas com cards **quadrados** (imagem preenchendo tudo + nome embaixo à esquerda). Busca/ordenação/filtro de categoria ficam escondidos atrás de um ícone de lupa (`AnimatedVisibility` expand/collapse). 3 tabs (não 4 — "Todos" foi removido): **Estante** (só status Tenho), **Desejo** (status Quero), **Jogado** (`played = true`) — texto da tab selecionada fica dourado, com transição `AnimatedContent` (fade + slide) ao trocar de tab. Ordenação: A-Z, Z-A, Recente (mais jogado recentemente primeiro, ▼), Recente reverso (nunca jogados primeiro por ordem alfabética, depois os jogados há mais tempo, ▲) — baseado em `GetLastPlayedDatesUseCase` (MAX(date) por jogo).
- **Detalhe do jogo**: imagem de fundo do "header" (280dp, com botões voltar/editar flutuando por cima, gradiente escuro pra legibilidade) → nome + estrelas à esquerda → 3 ícones (jogadores/tempo/peso) → dropdown de status **com gradiente dourado→branco e texto preto** (`StatusDropdown`, não é o `ExposedDropdownMenuBox` padrão do M3, é um `Box` customizado com `Brush.horizontalGradient`) → checkbox "Emprestado" (só aparece se status = Tenho; campo "Emprestado para" só aparece se o checkbox estiver marcado) → card de Estatísticas com 3 ícones lado a lado (PlayArrow=vezes jogadas, Timer=horas de jogo total, AccessTime=tempo médio) → **cards de estatísticas ricas** (ver "Estatísticas por jogo" abaixo) → botão **"Iniciar partida ao vivo"** (abre o cronômetro pré-selecionado pra este jogo, com diálogo de conflito se já houver outra partida ativa — ver "Cronômetro de partida ao vivo" abaixo) → botão **"Configurar/Editar pontuação personalizada"** (texto muda conforme `state.hasScoreSchema`, abre `ScoreSchemaBuilderScreen` — ver seção própria abaixo) → histórico de partidas (cada linha é clicável, `onSessionClick(sessionId)`, navega pra `SessionDetail` — mesma rota/tela já usada a partir do feed de Atividades do Perfil) → **FAB** "Registrar partida" que abre o wizard **com aquele jogo pré-selecionado** (pula a etapa de escolha de jogo automaticamente na primeira entrada, via `consumeAutoAdvance()`).
- **Adicionar/Editar jogo**: mesma tela pra criar e editar (`AddEditGameViewModel` detecta pelo sentinela `gameId == "new"`); em modo edição não mostra o seletor de status (isso é gerenciado na tela de detalhe). Capa: Câmera/Galeria (captura local, via `ImageStorage`) **ou URL direta** (campo de texto + botão "Usar") — as três opções escrevem no mesmo `state.coverImagePath`/`viewModel.onCoverCaptured(...)`, sem campo novo nem lógica separada, já que `coverImageUrl`/`GameCoverImage` (Coil) sempre aceitaram tanto caminho de arquivo local quanto URL remota nessa mesma string (é assim que os jogos importados via seed/Ludopedia já funcionavam). `ImageStorage.deleteImage()` é seguro de chamar com uma URL (não existe como arquivo local, vira no-op) — troca entre os três modos não vaza arquivo nem quebra.
- **Wizard de partida** (`presentation/screens/wizard/`): Escolher jogo → Dados da sessão (data/duração/variante/foto — `durationMinutes` pode vir pré-preenchido de um cronômetro finalizado, ver abaixo) → Jogadores (busca + criar novo) → **Pontuação** (branch: grid simples se o jogo não tem schema ou schema é `SIMPLE`; `CompositeScoringStep` — uma tela por jogador em sequência — se o schema é `COMPOSITE`; `RankingScoringStep` — todos os jogadores numa tela só, arrastável — se o schema é `RANKING`, ver seção "Pontuação personalizada" abaixo) → **Confirmação** (`ConfirmStep.kt`, também ramificada pro caso Composta: seletor de vencedor Manual, totais calculados + diálogo de empate no Automático, ou nada no Sem-vencedor; caso Ranking: lista só-leitura na ordem definida) — **conteúdo dentro de 2 `Card`s** (resumo da partida com `StatIconItem` pra data/duração; jogadores+pontuação com título dourado), coluna com `verticalScroll` pra não cortar lista longa de jogadores. Suporta edição (reaproveita as mesmas telas, `sessionId` real em vez de `"new"`) e pré-seleção de jogo (`gameId` na rota, sentinela `"none"` quando não vem de lugar nenhum).
  **Jogo travado em modo edição**: `ChooseGameStep.kt` pula automaticamente (`LaunchedEffect(state.selectedGameId, state.isEditMode)`) assim que o jogo da sessão carrega, sempre que `state.isEditMode == true` — o jogo de uma partida já salva não pode ser trocado. Como esse efeito reavalia a cada composição, a tela fica permanentemente inalcançável durante edição; por isso o "Voltar" de `SessionDataStep` (primeira tela visível nesse modo) sai do wizard inteiro em vez de voltar pra ela (`AppNavigation.kt`, condicional em `viewModel.isEditMode`).
  **⚠️ O botão/gesto de voltar do sistema Android não passa por esse `onBack` customizado por padrão** — o `NavHost` já intercepta o back do sistema sozinho e faz um `popBackStack()` puro, ignorando a lógica condicional acima. Sem tratar isso, voltar pelo botão físico/gesto do celular (em vez do "Voltar" da TopAppBar) durante edição caía de volta em `WizardChooseGame`, causando um flash visível dela antes do auto-skip corrigir. Fix: `SessionDataStep.kt` tem um `BackHandler(onBack = onBack)` (`androidx.activity.compose`, já uma dependência transitiva de `activity-compose`) que intercepta o back do sistema e chama o mesmo lambda `onBack` já correto — nenhuma outra etapa do wizard precisou disso, porque só o `onBack` de `SessionDataStep` diverge do `popBackStack()` padrão.
- **Detalhe de partida** (`SessionDetailScreen.kt`, aberto ao tocar num item de histórico — na Home, no detalhe do jogo ou nas Atividades do Perfil): **mesmo padrão de 2 `Card`s do `ConfirmStep`** (resumo com `StatIconItem` pra data/duração + variante/foto opcionais; jogadores+pontuação com título dourado), coluna com `verticalScroll`. As duas telas mostram essencialmente o mesmo tipo de conteúdo (dados da partida + placar) em momentos diferentes (confirmando antes de salvar vs. revisando depois de salvo) — **manter os dois em sincronia visual se um dos dois for alterado**, já corrigi uma vez uma divergência entre eles (`SessionDetailScreen` tinha ficado com o layout antigo, só `Text` solto sem `Card`, depois do `ConfirmStep` ser atualizado).
- **Perfil**: favoritos (editável, máx. 3, com diálogo de confirmação pra substituir o mais antigo) + Atividades (lista paginada via Paging 3 / `LazyPagingItems`).
- **Configurações** (acessada pelo ícone no Perfil, não é mais aba): toggle de tema, botões **"Importar JSON"** (`OutlinedButton` + `Icons.Filled.FileDownload`) e **"Exportar JSON"** (`OutlinedButton` + `Icons.Filled.FileUpload`, mesmo padrão visual) — ver seção "Importação/exportação de jogos via JSON" abaixo.
- **Jogadores** (aba da bottom bar, era "Gerenciar jogadores locais" dentro de Configurações — fica **antes** do Perfil na bottom bar): `ManagePlayersScreen.kt` agora tem **2 sub-abas** (`SecondaryTabRow` + `AnimatedContent`, mesmo padrão de fade+slide da tela Jogos, estado da tab local — `selectedTab`, não no ViewModel, já que não há filtro compartilhado entre elas): **Jogadores** e **Grupos** (ver seção "Grupos de jogadores" abaixo). Ambas são **grid de 2 colunas** (mesmo padrão visual da tela Jogos) — cada item é um `Card` quadrado (`aspectRatio(1f)`) com `PlayerAvatarImage` preenchendo tudo + nome abaixo, fora do card. Busca por nome fica **escondida** atrás de um ícone de lupa na TopAppBar (`AnimatedVisibility` expand/collapse), filtrando a lista da tab ativa; o botão de adicionar na TopAppBar também troca de texto/ação conforme a tab ("Adicionar Jogador"/"Adicionar Grupo"). `ManagePlayersViewModel` expõe `players` e `groups`, ambos só leitura — criar/editar/excluir vive nas telas dedicadas. Card de jogador clicável abre `PlayerDetailScreen`; card de grupo abre `GroupDetailScreen`.
- **Foto de perfil do jogador** (`Player.avatarPath: String?`, mesma dualidade caminho-local-ou-URL de `BoardGame.coverImageUrl`): **reaproveita a coluna Room `avatar_color`** de `PlayerEntity` — esse campo existia desde o início mas nunca foi de fato usado (só plumbing morto, confirmado por busca antes de mexer) —, só o nome do símbolo Kotlin mudou (`avatarColor` → `avatarPath`), a coluna do banco continua se chamando `avatar_color`. Escolha deliberada pra não precisar de `Migration` nova (mesma cautela já registrada em "Cronômetro de partida ao vivo" e "Pontuação Ranking" — o projeto não usa `fallbackToDestructiveMigration` e já tem dado real do usuário). Renderizado por `PlayerAvatarImage.kt` (novo, espelha `GameCoverImage.kt`: `AsyncImage` se houver `avatarPath`, senão um quadrado com a primeira letra do nome).
- **`ImagePickerSection.kt`** (novo, `presentation/components/`): o bloco Câmera/Galeria/URL que só existia inline em `AddEditGameScreen` foi extraído pra um componente compartilhado — `AddEditGameScreen` foi refatorada pra usá-lo, e a nova `AddEditPlayerScreen` usa o mesmo componente pra foto de perfil. Mesmo contrato de sempre: as três formas de capturar escrevem no mesmo `String?` (caminho local via `ImageStorage.persistImage` ou URL direta), sem branch nenhum no consumidor.
- **Criar/editar/excluir jogador** (`AddEditPlayerScreen.kt` + `AddEditPlayerViewModel.kt`, rota `add_edit_player/{playerId}` com sentinela `"new"` — mesmo padrão de `AddEditGameScreen`/`AddEditGameViewModel`): tela única cobre os dois modos, `ImagePickerSection` no topo, campo de nome, botão "Salvar", e (só em modo edição) botão "Excluir jogador" com diálogo de confirmação — a exclusão saiu do grid e passou a viver só dentro da edição. Exclusão continua bloqueada se o jogador tem histórico de partidas (`DeletePlayerUseCase`, erro mostrado num `AlertDialog`). Mesma limpeza de imagem órfã dos outros 3 fluxos que capturam imagem (guarda `originalAvatarPath`, só apaga arquivo antigo depois do save confirmar).
- **Detalhe do jogador** (`PlayerDetailScreen.kt`, `player_detail/{playerId}`): toque num card de `ManagePlayersScreen`. Ganhou `PlayerAvatarImage` centralizado (96dp) no topo da coluna e um ícone de editar (`Icons.Filled.Edit`) nas `actions` da TopAppBar, que navega pra `AddEditPlayerScreen`. Cards no mesmo estilo dos já existentes (Home/Detalhe do Jogo): partidas jogadas/vitórias/taxa de vitória/tempo total (`StatIconItem` x4), sequência de vitórias (atual + recorde), jogo favorito (mais vitórias) e "jogos mais jogados" reaproveitando o mesmo `HorizontalBarChart`/`BarChartEntry` da Home. Ver "Estatísticas por jogador" abaixo pra como isso é calculado.

## Cronômetro de partida ao vivo

Fluxo **separado** do wizard retrospectivo de sempre (decisão do usuário) — em vez de digitar a
duração depois de jogar, dá pra iniciar um cronômetro real ao começar a partida.

- **Persistência**: tabela Room de linha única `active_timer` (`ActiveTimerEntity`, PK fixa
  `"active"` — mesmo padrão de `UserProfileEntity`), colunas `game_id`/`game_name`
  (denormalizado, evita join pro banner)/`started_at`/`status` (`RUNNING`/`PAUSED`, enum
  `TimerStatus`)/`paused_at`/`accumulated_paused_millis`. **Só existe uma linha (uma partida
  cronometrada por vez, decisão do usuário)** — `ActiveTimerDao.upsert()` sempre substitui.
  `ActiveTimer.elapsedMillis(now)` (domain/model) é a função pura que calcula o tempo
  decorrido a partir dos timestamps — `agora − started_at − accumulated_paused_millis` se
  `RUNNING`, congelado em `paused_at − started_at − accumulated_paused_millis` se `PAUSED`.
  **Não há Service/WorkManager nem notificação persistente** (decisão do usuário, pra não
  introduzir infraestrutura nova) — o tempo só precisa estar certo quando o app reabre, então
  é recalculado por timestamp de parede, nunca "contado" em segundo plano.
- **Use cases** (`domain/usecase/`, um por ação): `GetActiveTimerUseCase` (`Flow<ActiveTimer?>`),
  `StartTimerUseCase`, `PauseTimerUseCase`, `ResumeTimerUseCase`, `CancelTimerUseCase` (só
  `dao.clear()`, nenhuma `GameSession` é criada), `FinishTimerUseCase` (calcula os minutos
  finais arredondados, limpa a linha, devolve o valor pro chamador).
- **Tela** (`presentation/screens/livematch/LiveMatchScreen.kt` + `LiveMatchViewModel`):
  relógio grande (`HH:MM:SS` via `utils/Formatters.kt#formatElapsed`), atualizado a cada
  segundo por um `LaunchedEffect` local enquanto `RUNNING` (congela se `PAUSED` — não tem
  nenhum timer "vivo" fora da tela, cada composição recalcula do zero a partir da linha
  persistida). Botões: `RUNNING` → Suspender/Cancelar/Finalizar; `PAUSED` →
  Retomar/Cancelar/Finalizar. Cancelar pede confirmação (`AlertDialog`, decisão do usuário).
  Entrar na tela é **idempotente**: se já existe uma linha ativa pro `gameId` pedido, só
  observa (caso de retomar via banner); se não existe, chama `StartTimerUseCase` (caso de
  iniciar do zero) — mesma tela cobre os dois casos.
- **Handoff pro wizard existente, sem inventar mecanismo novo**: ao Finalizar, a rota do
  wizard (`Routes.AddSessionWizardGraph`) ganhou um **terceiro segmento sentinela**
  (`{prefillDurationMinutes}`, default `"0"` = `NO_DURATION`), no mesmo padrão de
  `NEW_SESSION`/`NO_GAME` já usado — como tem valor default, nenhum call site existente
  precisou mudar. `AddSessionViewModel` ganhou o parâmetro `initialDurationMinutes`, que só
  é aplicado quando `hasPreselectedGame` (mesma condição do jogo pré-selecionado). O usuário
  cai em `SessionDataStep` já com a duração preenchida (mas editável) e segue o wizard normal.
- **Pontos de entrada**: botão "Iniciar partida ao vivo" na Home (→ `LiveMatchChooseGameScreen`,
  lista de jogos com busca, mesmo estilo do `ChooseGameStep` do wizard mas **desacoplada**
  dele — não compartilha `AddSessionViewModel`) e no Detalhe do Jogo (já sabe o `gameId`, vai
  direto). Os dois checam se já existe uma partida ativa **de outro jogo** antes de navegar —
  se sim, mostram um `AlertDialog` de conflito ("retome ou cancele antes de iniciar outra",
  decisão do usuário) em vez de sobrescrever a partida em andamento.
- **Retomar depois de fechar o app**: banner no topo da Home (`HomeState.activeTimer`, populado
  via `GetActiveTimerUseCase` no `HomeViewModel`), relógio ao vivo, toque navega pra
  `LiveMatchScreen` daquele jogo. **Sem notificação do Android** (decisão do usuário).
- **Migração de banco**: essa feature foi o motivo da versão do Room subir de 1→2 (ver Stack
  técnica acima) — só criou uma tabela nova, não migrou nenhuma das 8 tabelas anteriores.
- **Botões "Iniciar partida ao vivo" somem quando já existe um timer ativo** (Home e Detalhe
  do Jogo, `if (state.activeTimer == null) { ... }`) — antes ficavam visíveis e dependiam de
  um diálogo de conflito pra evitar sobrepor uma partida em andamento; agora o caminho nem
  aparece. Isso tornou o diálogo de conflito do `GameDetailScreen` (`onGoToConflictingMatchClick`)
  morto de verdade (nunca mais disparável dali) — removido junto, ao contrário do de
  `LiveMatchChooseGameScreen`, que **continua existindo** como defesa pro caso raro de um
  timer começar a existir *enquanto* o usuário já está naquela tela (ex.: duas janelas/telas
  abertas) — Home só esconde o botão de entrada, não impede a corrida em si.
- **Finalizar não apaga mais o timer na hora — só pausa.** Antes, `FinishTimerUseCase` já
  limpava a linha `active_timer` no mesmo instante em que o usuário tocava "Finalizar", então
  se ele saísse do wizard de pontuação sem salvar (ex.: voltar até a Home), a partida "sumia"
  de verdade — reabrir o cronômetro pra aquele jogo criava um **novo**, do zero. Agora
  `FinishTimerUseCase` só **pausa** (se ainda estava `RUNNING`; se já estava `PAUSED`, não
  mexe em `pausedAtMillis` pra não esticar o tempo contado) — os minutos calculados na hora
  do toque já foram passados pro wizard via `prefillDurationMinutes`, então isso não muda o
  valor pré-preenchido. O timer só é **de fato** encerrado (`ActiveTimerRepository.clearIfGameMatches`,
  via `ClearActiveTimerForGameUseCase`) dentro de `AddSessionViewModel.save()`, depois que a
  partida é realmente persistida — e só quando a sessão veio de um cronômetro finalizado
  (`hasPrefillDuration`) e ainda é do mesmo jogo (defesa contra o caso raro de trocar de jogo
  no wizard depois de finalizar o cronômetro de outro). Se o usuário sair do wizard sem
  salvar, o cronômetro continua ali pausado com o tempo certo — retomável ou cancelável
  normalmente pela Home.
- **⚠️ Bug já corrigido, cuidado ao reintroduzir**: `LiveMatchViewModel`'s flag de controle
  do `init` (evita chamar `StartTimerUseCase` de novo a cada emissão nula da Flow) se
  chamava `startRequested` e só era marcada `true` dentro do branch `timer == null` — ou
  seja, só no fluxo de **iniciar** um cronômetro novo. No fluxo de **retomar** um já
  existente (toque no banner da Home), a 1ª emissão da Flow já vem não-nula, então essa
  flag nunca era marcada — resultado: cancelar um cronômetro retomado disparava a mesma
  condição de "nunca vi nada, preciso criar" e recriava a partida na hora, em vez de
  encerrar de verdade. Renomeada pra `hasSeenActiveTimer` e marcada `true` em **todos** os
  branches que observam um timer real (criado ou retomado, mesmo jogo ou conflito de
  outro) — só falta criar quando `timer == null` **e** essa flag ainda nunca foi marcada.

## Estatísticas por jogador

`domain/model/PlayerStats.kt`: `gamesPlayed`, `wins`, `decidedGames` (partidas com vencedor definido — exclui cooperativas `winnerMode = NONE`, onde `ScoreEntry.isWinner` é `null` pra todo mundo), `winRate` (`wins / decidedGames`, `null` se `decidedGames == 0`, decisão do usuário de não punir quem só jogou cooperativo), `totalMinutes`, `currentWinStreak`/`bestWinStreak`, `topGames`/`favoriteGame` (reaproveitam `GamePlayCount`, o mesmo model do ranking da Home — em `favoriteGame` o campo `playCount` significa "nº de vitórias nesse jogo", não "nº de partidas", mesma struct com semântica diferente por contexto).

- **Todas as queries são novas em `ScoreEntryDao.kt`** (`getPlayerCoreCounts`/`getPlayerTotalMinutes`/`getPlayerSessionResults`/`getPlayerTopGames`/`getPlayerFavoriteGame`), sempre `score_entry` unido com `game_session` (e `board_game` quando precisa do nome do jogo) filtrado por `player_id` — mesmo padrão de `getMaxScoreForGame`, que já fazia esse join. `GameSessionRepository.getPlayerStats(playerId)` combina as 5 flows (igual `getGameStats` já fazia pra jogo) e calcula a sequência de vitórias em Kotlin.
- **Sequência de vitórias** (`calculateWinStreaks` em `GameSessionRepository`): pega os resultados do jogador em ordem cronológica e quebra a sequência em qualquer resultado que não seja vitória (derrota **ou** partida sem vencedor definido). Diferente da streak diária da Home (`GetStreakUseCase`), não tem conceito de "expirar por data" — é só a maior sequência consecutiva de vitórias na lista, sem precisar saber se "ainda está ativa hoje".
- Nenhuma coluna nova no banco, nenhuma migração — tudo derivado das colunas já existentes de `score_entry`/`game_session`/`board_game`.

Botões "Importar JSON" e "Exportar JSON" em Configurações — os dois lêem/escrevem o **mesmo formato** de arquivo (`SeedGamesFile`), documentado em `Regras/Como montar o JSON de jogos.md` e exemplificado em `Regras/Jogos iniciais.json` (catálogo completo: 20 jogos + 20 `scoreSchemas`, um por jogo, extraídos do Ludopedia + das regras oficiais de cada jogo — inclui SIMPLE, COMPOSITE/AUTOMATIC, COMPOSITE/MANUAL e COMPOSITE/NONE, cobrindo os principais padrões de pontuação encontrados na prática). Exportar e reimportar o mesmo arquivo é um round-trip válido.

**Jogos cooperativos sem pontuação individual** (Balde de Caranguejo, Gears of War: The Board Game): modelados como `COMPOSITE` + `winnerMode: NONE` com um único campo (NUMBER ou BOOLEAN) preenchido com o **mesmo valor pra todos os jogadores** da partida — contorno deliberado enquanto o app não modela pontuação por sessão/grupo (`FieldScope`/`PER_SESSION`, lacuna identificada na seção 8 do doc de produto, não implementada).

- **Camada de parsing** (`data/seed/`): `SeedGamesFile`/`SeedGame`/`SeedScoreSchema`/`SeedScoreField`/`SeedEnumOption`/`SeedFormula`/`SeedTerm` — DTOs `@Serializable` isolados dos modelos de domínio (evita acoplar o formato externo do JSON à shape interna do `GameScoreSchema`/`ScoreFieldType`). `SeedGamesMapper.kt` tem os dois sentidos: `toDomain()` (import) e `toSeedGame()`/`toSeedScoreSchema()`/`toSeed()` (export). `Json { ignoreUnknownKeys = true }` na leitura — o JSON de exemplo tem campos descritivos extras (`description`, `metadata`, `notes`) que não viram dado nenhum, só documentação dentro do próprio arquivo; na escrita, `Json { prettyPrint = true }`.
- **Importar — upsert por Id/gameId** (`ImportSeedGamesUseCase`): pra cada jogo, busca por `id` exato (`BoardGameRepository.findGameOnce`) — se existe, `UPDATE` preservando `createdAt`; se não, `INSERT` usando o `id` do JSON como PK real (não gera UUID novo, ao contrário do fluxo normal de "Adicionar Jogo") — isso é o que torna reimportar o mesmo arquivo idempotente. Mesma lógica pra `GameScoreSchema` via `gameId`. Erros são coletados por item (`ImportResult.errors`), um jogo/schema malformado não derruba o resto da importação.
- **Jogo novo entra na estante como "Quero"**: `UserLibraryEntry(status = WANT)` é criado automaticamente pra todo jogo inserido pela primeira vez — sem isso, o jogo ficaria invisível nas 3 tabs da tela Jogos (Estante/Desejo/Jogado, todas filtram por status/played; não existe uma aba "Todos" no V1). Jogos já existentes (update) não têm o status mexido, preserva a escolha do usuário.
- **Exportar** (`ExportGamesUseCase`): lê todos os `BoardGame` (via `BoardGameRepository.getGames().first()`, sem variante "once" dedicada — não precisou, é só um `first()` na Flow existente) + todos os `GameScoreSchema` não deletados (`GameScoreSchemaRepository.getAllOnce()`, nova query — a existente `getAllCompositeSchemas()` filtra só `type = COMPOSITE`, não serve pra export completo). **Escopo deliberado**: só acervo (jogos + schemas), não é um backup completo do app — não inclui jogadores, partidas registradas, perfil. Isso é o que "exportação dos jogos existentes" pediu.
- **UI de export**: fluxo em 2 passos por causa do Storage Access Framework do Android — (1) botão gera o conteúdo (`SettingsViewModel.onExportRequested()`, roda o usecase, guarda o resultado em `state.pendingExport`); (2) um `LaunchedEffect(state.pendingExport)` dispara o launcher de `ActivityResultContracts.CreateDocument("application/json")` assim que o conteúdo fica pronto — só *depois* de escolher o local é que o conteúdo já pronto é escrito nele. Nome sugerido: `scorequest_jogos.json`.
- **UI geral**: `SettingsViewModel` guarda `isImporting`/`importResult`/`importReadError`/`isExporting`/`pendingExport`/`exportSuccessMessage`/`exportError` num `MutableStateFlow` combinado com a preferência de tema (que continua vindo de `GetThemePreferenceUseCase` via `collect` no `init`, não mais um `map` direto — precisou virar esse padrão híbrido pra não resetar esse estado a cada emissão do tema). Resultado mostrado num `AlertDialog` (contagem de adicionados/atualizados/pontuações + lista de erros, se houver, na importação; contagem exportada na exportação).

## Estatísticas por jogo

`domain/model/GameStats.kt` expandido (mirror do padrão já estabelecido em `PlayerStats`/
`GroupStats`, ver seções acima) — antes só tinha `timesPlayed`/`avgDurationMinutes`/`highScore`
(esse último nunca chegava a ser renderizado na tela), agora também: `longestSessionMinutes`
(partida mais longa, `MAX(duration_minutes)`), `topPlayersByPlays` (ranking de quem mais jogou
**esse jogo específico**, tipo novo `PlayerPlayCount` em `domain/model/HomeStats.kt` — mesma
forma de `PlayerWinCount`, mas pra contagem de partidas em vez de vitórias, já que nenhum tipo
existente cobria "jogador + contagem" sem o significado ser "vitórias"), `topPlayersByWins`
(mirror de `ScoreEntryDao.getGroupMemberWins`, com `gs.game_id` no lugar de `gs.group_id`), e
`topScores: List<GameScoreRecord>` (novo tipo, top 5 **maiores pontuações já registradas nesse
jogo**, um registro por sessão/jogador — não agrupado, pode repetir jogador se ele tiver mais
de uma pontuação no top 5 — com `sessionId` pra navegação).

- **`GameSessionRepository.getGameStats`**: como agora são 7 flows (`combine()` só tem overload
  tipado até 5 argumentos), o método monta um `GameCoreStats` privado combinando os 5
  primeiros, depois faz um segundo `combine` com esse holder + os 2 rankings restantes — evita
  a forma vararg/`Array<*>` do `combine`, que perderia a tipagem forte de cada campo.
- **Novas queries** (`ScoreEntryDao.getTopPlayersByPlaysForGame`/`getTopPlayersByWinsForGame`/
  `getTopScoresForGame`, `GameSessionDao.getLongestSessionMinutes`), todas filtrando
  `gs.game_id = :gameId` — mesmo padrão de join/guarda de `deleted_at IS NULL` já usado em
  todas as outras queries de estatística do projeto.
- **`totalScore` não é uniformemente comparável entre tipos de pontuação** (é o total digitado
  no Simples, o resultado da fórmula no Composto/Automático, os pontos por posição no Ranking
  se ativados, ou `null` no Composto/Manual e Composto/Sem-vencedor) — mas como "maiores
  pontuações" e "recorde" são sempre escopados a **um jogo só**, isso é seguro na prática
  (todas as sessões de um mesmo jogo compartilham o mesmo schema, exceto se o usuário editar a
  fórmula depois de já ter partidas registradas — caso raro, não tratado). A query de
  `topScores` já filtra `total_score IS NOT NULL`, então jogos sem pontuação numérica (Composto
  Manual/Sem-vencedor) simplesmente não mostram a seção "Maiores pontuações" nem "Recordes"
  (ambas condicionais a terem dado, `GameDetailScreen.kt`).
- **UI** (`GameDetailScreen.kt`): 4 novos `Card`s empilhados depois do card "Estatísticas" já
  existente (mesmo padrão visual de título dourado + `HorizontalBarChart`/`BarChartEntry` já
  usado em Home/`PlayerDetailScreen`/`GroupDetailScreen`) — **Recordes** (maior partida +
  recorde de pontuação, cada `StatIconItem` só aparece se não-nulo), **Mais partidas jogadas**,
  **Mais vitórias** (ambos gráfico de barras), **Maiores pontuações** (lista, não gráfico —
  cada linha usa `PositionBadge` + nome + data relativa + pontuação, clicável via
  `onSessionClick(record.sessionId)`, o mesmo callback já usado pelo histórico de partidas).
  Cada card só renderiza se a lista/valor correspondente não estiver vazio — jogo com poucas
  partidas simplesmente mostra menos cards, sem estado vazio explícito (diferente da Home, que
  usa textos de estado vazio — aqui não fez sentido, dado que a lista de "Histórico de
  partidas" logo abaixo já deixa claro que não há dados).

## Grupos de jogadores

Conjuntos nomeados de 2+ jogadores (nome + foto opcional) que funcionam como atalho de
seleção ao registrar uma partida, com estatísticas próprias (partidas jogadas, tempo total,
ranking de vitórias dos membros, jogos mais jogados) filtradas pelas partidas linkadas àquele
grupo. Feature nova do zero (`version 2 → 3`, `MIGRATION_2_3` em
`data/local/database/Migrations.kt`) — primeiro relacionamento N:N do projeto.

- **Schema**: `player_group` (`PlayerGroupEntity`: id/name/photo_path/auditáveis, mesmo padrão
  de `Player`) + `player_group_member` (`PlayerGroupMemberEntity`, tabela de junção nova, PK
  composta `group_id`+`player_id` com `ForeignKey` real pras duas tabelas — como é criada do
  zero na migração, os FKs entram sem problema). `PlayerGroupWithMembersEntity` (`@Embedded` +
  `@Relation` com `@Junction`, primeiro uso de `@Junction` no projeto) resolve a lista de
  `PlayerEntity` membros a partir da tabela de ligação.
- **`game_session.group_id`** (nova coluna, nullable, via `ALTER TABLE ADD COLUMN`):
  **deliberadamente sem `ForeignKey`** na anotação `@Entity` — o SQLite não permite acrescentar
  uma constraint FK numa tabela já existente por `ALTER TABLE` (só em `CREATE TABLE`), e
  recriar a tabela inteira só pra isso seria desnecessário pra uma referência que é só
  apresentacional. Consequência aceita: exclusão de grupo é **livre, sem bloqueio**
  (`DeletePlayerGroupUseCase`, diferente de `DeletePlayerUseCase` que bloqueia por histórico) —
  se o grupo referenciado por uma partida antiga for excluído depois, o `group_id` fica órfão e
  a UI trata como "sem grupo". `SessionWithScoresEntity` ganhou um `@Relation` opcional
  (`parentColumn = "group_id"`) pro grupo, e `SessionWithDetails.groupName` é populado a partir
  dele sem query adicional — Room resolve `@Relation` só por uma segunda `SELECT ... WHERE id
  IN (...)`, não precisa da FK pra isso.
- **Camada de domínio/repositório/use cases**: mirror exato do padrão de `Player`
  (`PlayerGroup`, `PlayerGroupRepository`, `Get/Create/Update/DeletePlayerGroupUseCase`).
  `FindGroupWithExactMembersUseCase` compara conjuntos de membros **em Kotlin**
  (`getGroupsOnce().find { it.memberIds.toSet() == memberIds }`), não em SQL — número de
  grupos esperado é pequeno o suficiente pra não precisar de query dedicada.
- **Aba Grupos** (`ManagePlayersScreen.kt`, ver seção "Navegação e telas" acima): grid igual à
  de Jogadores, card clicável abre `GroupDetailScreen`. **Criar/editar/excluir**
  (`AddEditGroupScreen.kt`/`AddEditGroupViewModel.kt`, rota `add_edit_group/{groupId}` com
  sentinela `"new"`, mirror de `AddEditPlayerScreen`): `ImagePickerSection` pra foto + campo de
  nome + seletor de membros (`PlayerRow` — checkbox + nome, **extraído** de
  `wizard/PlayersStep.kt` pra `presentation/components/PlayerRow.kt`, reaproveitado nos dois
  lugares — só aqui ganhou busca por nome local em cima da lista, o `PlayersStep` do wizard
  continua sem busca) + botão Salvar (`enabled` exige nome preenchido **e** 2+ membros) + (modo
  edição) "Excluir grupo" sem diálogo de bloqueio, já que exclusão é livre.
- **Estatísticas de grupo** (`domain/model/GroupStats.kt`, mirror de `PlayerStats`, mas
  filtrado por `game_session.group_id = :groupId` em vez de por jogador): partidas
  jogadas/tempo total (`GameSessionDao.getGroupSessionCount`/`getGroupTotalMinutes`), jogos
  mais jogados (`getGroupTopGames`, mirror de `getTopPlayedGames`), ranking de vitórias dos
  membros **dentro das partidas do grupo** (`ScoreEntryDao.getGroupMemberWins`, mirror de
  `getTopPlayersByWins`, mesmo shape `PlayerWinCount` já usado na Home). `favoriteGame` = jogo
  mais jogado (não mais vencido — quem vence é jogador, não o grupo), sem query separada.
  `GroupDetailScreen.kt` (`GroupDetailViewModel`) mostra isso no mesmo estilo visual de
  `PlayerDetailScreen` (avatar central, `HorizontalBarChart` reaproveitado), mas **sem**
  "vitórias"/"taxa de vitória" no card principal — esses conceitos não fazem sentido pro
  grupo em si, só pra seus membros individualmente (por isso o card "Mais vitórias no grupo").
- **Selecionar grupo no wizard** (`PlayersStep.kt`): se existem grupos, uma `LazyRow` de
  `FilterChip` (avatar + nome) aparece acima da lista de jogadores. Tocar num grupo
  não-selecionado (`AddSessionViewModel.onGroupSelected`) **substitui** `selectedPlayerIds`
  pelos membros do grupo e marca `AddSessionState.selectedGroupId`; tocar de novo no chip já
  ativo só desmarca o rastreamento (`selectedGroupId = null`), sem mexer na seleção de
  jogadores. A partir daí a lista de checkboxes continua **totalmente editável**
  (`onPlayerToggled` **não** limpa `selectedGroupId`) — é essa divergência entre "grupo de
  origem" e "seleção atual" que alimenta a reconciliação no save.
- **Reconciliação ao salvar** (`AddSessionViewModel.save()`): antes de persistir, roda um
  algoritmo unificado — a checagem de coincidência acontece **sempre primeiro**, não só
  quando nenhum grupo foi selecionado:
  1. `FindGroupWithExactMembersUseCase(selectedIds)` busca em **todos** os grupos (não só o
     originalmente selecionado). Se achar — mesmo que seja um grupo diferente do que foi
     tocado no wizard, ou nenhum grupo tenha sido tocado — **adota automaticamente, sem
     diálogo nenhum**. Essa é a correção mais importante do design: se o usuário parte do
     grupo A mas edita a seleção até ela bater exatamente com o grupo B (pré-existente), a
     partida linka em B, não em A, sem perguntar nada.
  2. Se não achou match e `selectedGroupId != null` (partiu de um grupo, divergiu, e a
     divergência não virou nenhum outro grupo existente): mostra `GroupDriftDialog`
     (`ConfirmStep.kt`, mesmo padrão não-dispensável do `TieBreakDialog` já existente) com 3
     opções — **Atualizar grupo** (`UpdatePlayerGroupUseCase` substitui os membros do grupo
     original pelos atuais), **Manter como está** (grupo não muda, mas a partida ainda linka
     nele), **Criar novo grupo** (sub-passo pedindo nome, `CreatePlayerGroupUseCase`, grupo
     original fica intocado).
  3. Se não achou match, `selectedGroupId == null` e 2+ jogadores selecionados: mostra
     `GroupCreateOfferDialog` (nome + "Criar"/"Pular").
  4. Menos de 2 jogadores: salva sem grupo, sem diálogo.

  Implementação: `scoreInputs` já calculado fica guardado em `pendingScoreInputs` (campo
  privado do ViewModel, não em `AddSessionState` — não precisa renderizar) até a decisão ser
  tomada; `persistSession(...)` privado é o único lugar que de fato chama
  `Save/UpdateGameSessionUseCase` e seta `state.saved = true` — o
  `LaunchedEffect(state.saved) { onSaved() }` que já existia em `ConfirmStep.kt` não precisou
  mudar, porque continua só disparando quando a gravação de fato acontece (agora sempre depois
  de qualquer diálogo ser resolvido). `SaveGameSessionUseCase`/`UpdateGameSessionUseCase`
  ganharam parâmetro `groupId: String?`.
- **Exibição no detalhe da partida**: `SessionDetailScreen.kt` mostra "Grupo: {nome}" no Card
  de resumo (mesmo estilo da linha de variante), condicional a `SessionWithDetails.groupName`.

## Pontuação personalizada (seção 8 do doc de produto)

Construtor visual completo (`GameScoreSchema` por jogo) + integração real com o fluxo de lançamento de partida. Implementado por pedido explícito do usuário mesmo sendo nominalmente V2 — ver nota no topo do arquivo.

**Modelo de dados**: `GameScoreSchema` (`domain/model/GameScoreSchema.kt`) com `type` (SIMPLE/COMPOSITE/RANKING), `fields: List<ScoreFieldType>` (sealed class: NumberField/BooleanField/EnumField/MultiSelectField/TextField, todas `@Serializable`), `winnerMode` (MANUAL/AUTOMATIC/NONE), `formula: ScoreFormula?` (terms + comparisonRule). Entidade Room (`GameScoreSchemaEntity`) usa **`gameId` como `@PrimaryKey` diretamente** (mesmo padrão de `UserLibraryEntryEntity`) — garante "um schema por jogo" via constraint de banco, sem precisar da semântica condicional-a-soft-delete que o doc original sugeria (o doc já reconhece essa imprecisão; aqui resolvida de forma direta). `fields`/`formula` persistidos como JSON (kotlinx.serialization) em colunas TEXT, não como tabelas relacionais. `ScoreEntry.fieldValues: Map<String, String>?` guarda os valores lançados por jogador por partida, também como JSON — valores de campo sempre viram `String` (número vira string, boolean "true"/"false", multi-select junta labels com `"||"` — constante `MULTI_SELECT_VALUE_SEPARATOR`), decodificados por tipo de campo só na hora de calcular/exibir.

**Construtor** (`presentation/screens/scoreschema/ScoreSchemaBuilderScreen.kt` + `ScoreSchemaBuilderViewModel`): entrada única em `score_schema_builder/{gameId}` (`Routes.ScoreSchemaBuilder`), a partir do botão na tela de detalhe do jogo — a mesma tela serve tanto pra criar quanto pra editar (carrega o schema existente se houver). Não é um nested nav graph como o wizard de partida — é **uma máquina de estados de tela única** (`BuilderStep`: TYPE_CHOICE → FIELD_LIST → WINNER_ASSEMBLY → TEST_MODE), navegação interna via `viewModel.goToStep()`, botão voltar da TopAppBar volta um step (ou sai da tela no TYPE_CHOICE). Decisão deliberada de simplificação: menos telas/rotas novas pra manter, dado que os sub-passos não precisam ser deep-linkáveis.

- **TYPE_CHOICE**: Simples (salva direto) vs Composta (avança) vs Ranking (mostra inline um `Switch` "Ativar pontos por jogador" + salva direto, sem avançar de step — ver bullet próprio abaixo) vs "Duplicar de outro jogo" (pré-popula campos/fórmula de outro schema Composto existente, permanece editável).
- **FIELD_LIST**: lista de campos com editar/mover/remover; **reordenação é por botões cima/baixo (`ArrowUpward`/`ArrowDownward`), não drag-and-drop** — o doc sugere drag-and-drop mas isso exigiria detecção de gesto customizada sem forma de validar visualmente neste ambiente (sem emulador); resultado funcional é o mesmo. Adicionar campo abre `FieldTypePickerDialog` (5 tipos) → `FieldConfigDialog` (formulário específico do tipo, incluindo lista de opções com pontos pra Enum/MultiSelect). `key` de cada campo é gerado automaticamente a partir do label (slug + sufixo aleatório), nunca editado pelo usuário.
- **WINNER_ASSEMBLY**: Manual/Automático/Sem vencedor. No Automático: termos da fórmula (campo + peso via stepper +/-1), preview textual da fórmula (`state.formulaPreviewText`), regra de comparação (maior/menor vence), botão pro modo de teste.
- **TEST_MODE**: reaproveita `CompositeFieldInputForm` (`presentation/components/`, também usado no lançamento real) com 2 jogadores fictícios, mostra total calculado ao vivo se Automático — nada é persistido.

**Paleta**: os componentes do construtor e da pontuação composta seguem o mesmo padrão visual já usado no resto do app (não usavam no primeiro pass, corrigido depois) — cards com **borda fina em gradiente dourado→branco** (`Modifier.border(1.dp, Brush.linearGradient(listOf(Gold, Color.White)), shape)`, mesmo padrão do card "Última jogatina" da Home) em `SchemaTypeCard`, `FieldCard` e no card "Adicionar campo"; **rótulos de campo em dourado** (`color = Gold`) em `CompositeFieldInputForm` (Boolean/Enum/MultiSelect) e nos `OutlinedTextField` do `FieldConfigDialog` (`goldTextFieldColors()`, cor do rótulo e da borda focada); título da opção selecionada em `SchemaTypeCard`/`WinnerModeOption` também fica dourado, mesmo padrão da tab selecionada na tela Jogos. Botões/RadioButton/Checkbox não precisam de override — já usam `colorScheme.primary` (= Gold) por padrão via `Theme.kt`.

**Integração com o wizard de partida**: `AddSessionViewModel` expõe `schema: StateFlow<GameScoreSchema?>` (deriva de `selectedGameId` via `flatMapLatest`). Depois da etapa Jogadores, a navegação em `AppNavigation.kt` verifica `schema.value?.type` e vai pra `WizardScoring` (tela genérica existente, cobre SIMPLE e "sem schema") ou `WizardCompositeScoring` (`CompositeScoringStep.kt`, novo — um jogador por vez, `CompositeFieldInputForm` compartilhado com o modo de teste do construtor, navegação "Jogador anterior/Próximo jogador" **dentro da própria tela**, não uma rota por jogador). Na Confirmação (`ConfirmStep.kt`, ramificada por `schema?.winnerMode`): Manual mostra lista de jogadores com `RadioButton` pra escolher o vencedor (obrigatório, Salvar fica desabilitado até escolher); Automático calcula o total de cada jogador (`CalculateScoreFormulaUseCase`, classe pura sem dependência de repositório) e, se detectar empate no topo, mostra `TieBreakDialog` ("Considerar empate" = ambos vencedores / "Escolher manualmente" = abre lista dos empatados); Sem-vencedor não mostra nada de vencedor.

**Ponto de atenção pra manter**: qualquer mudança em `state.compositeFieldValues` reseta `automaticWinnerIds`/`pendingTieCandidateIds` (ver `onCompositeFieldChange`), forçando recálculo — sem isso um empate resolvido ficaria "preso" mesmo depois do usuário editar uma pontuação e o empate deixar de existir.

**Pontuação Simples opcional (override por partida, mesmo com schema COMPOSITE)**: `AddSessionState.useSimpleEntry` (padrão `false`) deixa o usuário pular o formulário campo-a-campo mesmo num jogo com schema Composto — um `Switch` em `PlayersStep.kt` (só visível quando `schema?.type == COMPOSITE`) liga essa flag via `viewModel.onToggleSimpleEntry()`. Com a flag ligada, a rota escolhida em `AppNavigation.kt` (clique em "Próximo" da etapa Jogadores) vai pra `WizardScoring` em vez de `WizardCompositeScoring`, e tanto `ConfirmStep.kt` (`isComposite = schema?.type == COMPOSITE && !state.useSimpleEntry`) quanto `AddSessionViewModel.save()` tratam a sessão exatamente como uma partida Simples (total + vencedor manual, `ScoreEntry.fieldValues = null`). **Isso não exigiu nenhuma mudança de banco/model** — `ScoreEntry`/`ScoreInput`/`ScoreEntryEntity` já suportavam `fieldValues = null` independente do tipo de schema, e nenhuma estatística (`GetGameStatsUseCase`, `GetHomeStatsUseCase`, `SessionDetailScreen`, etc.) nunca leu `fieldValues` — todas já operam só sobre `totalScore`/`isWinner`, então uma partida Simples e uma Composta do mesmo jogo se misturam nas estatísticas sem tratamento especial.

**Pontuação Ranking**: todos os jogadores numa tela só (`RankingScoringStep.kt`), reordenados arrastando (posição 1 = 1º lugar). Vencedor é sempre a posição 1 — `winnerMode` não é perguntado nesse tipo (fica com o sentinela `AUTOMATIC`, igual ao `SIMPLE`, nunca consultado). Pontos por jogador são **opcionais e digitados na hora de cada partida** (não uma tabela fixa por posição) — decisão do usuário, reaproveitando o mesmo `NumberField` já usado na Composta.

- **Sem migração de banco pra nada disso** (decisão deliberada, dado que o projeto não tem `Migration`/`fallbackToDestructiveMigration` configurados e já tem dado real do usuário — mesma cautela já registrada na seção "Cronômetro de partida ao vivo"): o sinal "pontos ativados?" é a própria presença de um campo em `schema.fields` (`[NumberField(key = RANKING_POINTS_FIELD_KEY, label = "Pontos")]` ou lista vazia) — reaproveita a coluna `fields_json` que já existia, sem precisar de um booleano novo em lugar nenhum. Por sessão, `ScoreEntry.fieldValues` ganha `RANKING_POSITION_FIELD_KEY` (posição 1-based, sempre) e `RANKING_POINTS_FIELD_KEY` (só se o schema tiver o campo) — constantes em `domain/model/ScoreFieldType.kt`. `totalScore` = pontos (se ativado) ou `null`; `isWinner` = só posição 1.
- **Restauração em modo edição**: no `init` do `AddSessionViewModel`, reconstrói `state.rankingOrder`/`rankingPoints` a partir de `detail.scores`, ordenando por `fieldValues[RANKING_POSITION_FIELD_KEY]` — a própria presença dessa chave já basta como heurística (só sessões Ranking a gravam), sem precisar esperar o schema carregar, mesmo espírito da heurística do `useSimpleEntry`.
- **Arrastar pra reordenar**: `pointerInput` + `detectDragGestures` escopado só no ícone de alça (`Icons.Filled.DragHandle`) de cada linha, deslocamento vertical acumulado (`Modifier.offset { IntOffset(...) }`) que troca a posição na lista ao passar da metade da altura da linha (`onGloballyPositioned` mede a altura), com `Modifier.animateItem()` cuidando do reflow suave dos itens não-arrastados. **Zero precedente de gesto de toque no app antes disso** (confirmado via busca ampla) — por segurança, cada linha também tem **setas cima/baixo de reserva** (mesmo padrão do `FIELD_LIST` da Composta), então reordenar nunca depende só do gesto novo funcionar bem de primeira.
- `onPlayerToggled` mantém `rankingOrder`/`rankingPoints` sincronizados (remove quem foi desmarcado, acrescenta quem foi marcado no fim da lista) — sem isso, ir e voltar entre as etapas Jogadores/Ranking dessincronizaria a ordem.
- **Exibição da posição nas telas de leitura**: `SessionDetailScreen.kt` e o card "Última jogatina" da Home (`HomeScreen.kt`) mostram "1º"/"2º"/etc. pra sessões Ranking, em vez de cair no `"${score.totalScore ?: "-"}"` genérico (que só mostraria os pontos, se ativados, sem nenhuma posição). Detecção via `List<ScoreEntry>.isRankingSession`/`.sortedByRankingPosition()`/`ScoreEntry.rankingPosition` (`domain/model/ScoreEntry.kt`) — mesma heurística de presença de `RANKING_POSITION_FIELD_KEY` já usada na restauração de edição, sem precisar carregar o schema do jogo nessas telas. `GameDetailScreen.kt` não precisou de mudança — seu histórico de sessões nunca mostrou placar nenhum, pra nenhum tipo de schema.
- **A ideia de posição foi estendida pra Simples e Composta Automática** (pedido do usuário: "sempre trazendo o vencedor em primeiro e os outros seguintes em suas respectivas posições"), sem precisar de nenhum campo/coluna novo — é só ordenação na hora de exibir, derivada de `totalScore`/`isWinner` que já existiam:
  - `domain/model/ScoreEntry.kt`: `List<ScoreEntry>.orderedForDisplay()` (vencedor primeiro, resto por `totalScore` decrescente quando existe pontuação numérica por trás; Composta Manual só sobe o vencedor sem reordenar o resto — não há número; cooperativo/sem vencedor mantém a ordem original) e `.hasNumberedPositionsForDisplay()` (quando de fato mostrar as badges "1º"/"2º"). Igual à detecção de Ranking, é heurística baseada só na forma dos dados (`fieldValues` vazio ou não, `totalScore` presente ou não) — não carrega o schema.
  - Novo componente compartilhado `presentation/components/PositionBadge.kt` (extraído do que já existia só pro Ranking) — usado agora em `SessionDetailScreen`, `HomeScreen` e `ConfirmStep.kt` (Simples, Composta Automática — com `comparisonRule` do schema, disponível ali — e Ranking) de forma uniforme.
  - **Decisão do usuário sobre quando ordenar**: só na tela de Confirmação (revisão antes de salvar) e nas telas de visualização — a tela de pontuação em si (`ScoringStep`/`CompositeScoringStep`, onde o usuário ainda está digitando) **não foi tocada**, continua na ordem de seleção dos jogadores, pra não reordenar linhas enquanto o campo de texto está sendo editado.
  - `ConfirmStep.kt`'s `AutomaticWinnerList` ordena respeitando `schema.formula.comparisonRule` (tem acesso ao schema ali, então é preciso); a heurística das telas de leitura (sem esse acesso) sempre assume decrescente — imprecisão aceitável já que nenhum jogo do catálogo atual usa `LOWEST_WINS`.
  - `ManualWinnerList` (seletor interativo de vencedor em Composta Manual) **não foi alterado** — é uma lista de escolha, não de exibição; reordenar enquanto a pessoa ainda está escolhendo atrapalharia mais do que ajudaria.

Ao **editar** uma partida existente, o modo (Simples vs. Composto) é redetectado por heurística no `init` do `AddSessionViewModel` — não por um campo persistido: `useSimpleEntry = detail.scores.isNotEmpty() && detail.scores.all { it.fieldValues.isNullOrEmpty() }`. Decisão deliberada (confirmada com o usuário) de não adicionar coluna nova em `ScoreEntryEntity`, já que `DatabaseModule.kt` não tem `Migration`/`fallbackToDestructiveMigration` configurados — subir a versão do Room quebraria o banco local já em uso. Limitação aceita: se alguém abrir o formulário Composto detalhado e deixar literalmente todos os campos em branco, a heurística reabre essa sessão em modo Simples na próxima edição (caso raro).

## Extras (aba "Ferramentas" — nome encurtado por decisão do usuário; 12 ferramentas, 4 fases, completas)

Aba dedicada a mini-ferramentas de jogatina que **não** dependem de registrar uma partida
completa (sorteios, dados, cronômetros...). As 12 ferramentas foram implementadas em 4 fases
por afinidade técnica: Fase 1 (sorteios genéricos: moeda, número, letra), Fase 2 (sorteios com
jogadores/grupos), Fase 3 (dado e roleta, com animação mais elaborada), Fase 4 (placar avulso,
cronômetro por turno, sorteio por dedo na tela).

- **`ToolsScreen.kt`** (tela-índice da aba, `Routes.Tools`): grid 2 colunas de atalhos
  estáticos (`ToolDestination(route, label, icon)`, lista fixa no arquivo, não vem de
  banco/DI). **Única tela do app sem ViewModel/Koin** — decisão deliberada, já que não há
  nenhum dado pra buscar (todo o resto do app usa 1 ViewModel por tela, confirmado antes de
  abrir essa exceção).
- **`presentation/components/SpinTicker.kt`** (`SpinTickerState<T>` + `rememberSpinTicker`):
  hook compartilhado de "sorteio com suspense" — cicla valores aleatórios (via uma lambda
  `randomValue: () -> T` fornecida por cada ferramenta) cada vez mais devagar (`delay`
  crescente de 40ms a 220ms ao longo de 18 passos) até parar no valor final passado pra
  `spin(final)`, sem precisar de `Animatable`/biblioteca de animação — primeiro uso de
  `kotlin.random.Random` no projeto. Cada ferramenta só decide o que renderizar pra cada
  valor (texto, número, letra); a cadência de tempo é 100% compartilhada. Reaproveitado por
  `CoinFlipScreen`, `RandomNumberScreen`, `RandomLetterScreen` (todos em
  `presentation/screens/tools/`) — cada uma: local `remember`/`rememberCoroutineScope` só,
  sem ViewModel (não há dado persistido nem lido de lugar nenhum).
- **Moeda** (`CoinFlipScreen.kt`): sorteia entre "Cara"/"Coroa".
- **Número aleatório** (`RandomNumberScreen.kt`): campos min/max (texto numérico), valida
  `min <= max` antes de habilitar o botão "Sortear".
- **Letra aleatória** (`RandomLetterScreen.kt`): sorteia entre A-Z.
- **FAB de "Registrar partida" não aparece na aba Extras** (`showFab` em
  `AppNavigation.kt` ganhou mais uma exclusão, igual já tinha pra Home).

**Fase 2 — sorteios com jogadores** (reaproveita `GetPlayersUseCase`/`GetPlayerGroupsUseCase`,
já registrados, nenhum use case novo, nenhuma escrita no banco):

- **`presentation/components/PlayerMultiSelectSection.kt`** (novo): busca por nome + lista de
  checkboxes de jogadores, extraído de `AddEditGroupScreen.kt` (que foi refatorada pra usar
  esse componente em vez do bloco inline que tinha antes) — reaproveitado pelas 4 telas novas.
- **`presentation/components/GroupChipRow.kt`** (novo): fileira de chips de grupo (avatar +
  nome), extraído do `GroupChip` que antes vivia só dentro de `wizard/PlayersStep.kt`
  (refatorado pra importar daqui) — reaproveitado pelas mesmas 4 telas.
- Cada uma das 4 telas segue o mesmo esqueleto: chips de grupo (se houver grupos) +
  `PlayerMultiSelectSection` + campo(s) específico(s) + botão de sortear. ViewModels
  praticamente idênticos entre si (`players`/`groups` StateFlow + `onPlayerToggled` que limpa
  `selectedGroupId` ao editar manualmente + `onGroupSelected` que substitui a seleção inteira)
  — duplicação pequena e deliberada entre eles (2-3 campos de state), consistente com a
  decisão de manter cada ferramenta como tela/rota separada (ver Contexto do plano).
- **Sorteio por nome** (`PickNamesScreen.kt`/`PickNamesViewModel.kt`): `rememberSpinTicker`
  cicla nomes até parar em 1 jogador selecionado.
- **Ordem de turno** (`ShuffleOrderScreen.kt`/`ShuffleOrderViewModel.kt`): ticker cicla listas
  inteiras embaralhadas (`SpinTickerState<List<Player>>` — o hook é genérico, funciona igual
  pra ciclar uma lista inteira em vez de um valor único) até parar numa ordem final, exibida
  numerada com `PositionBadge` (mesmo componente já usado em Ranking/Confirmação/Detalhe de
  partida).
- **Sorteio de equipes** (`ShuffleTeamsScreen.kt`/`ShuffleTeamsViewModel.kt`): stepper
  "N times" (2-12) + distribuição round-robin (`shuffled().filterIndexed { i, _ -> i % n ==
  teamIndex }`) — sem animação de ticker aqui (resultado é N listas, não um valor único),
  revelado instantaneamente em N `Card`s ao tocar "Sortear equipes".
- **Sorteio de papéis** (`AssignRolesScreen.kt`/`AssignRolesViewModel.kt`): lista de texto
  livre de papéis (adicionar/remover linha a linha) — botão "Sortear papéis" só habilita
  quando `roles.size == jogadoresSelecionados.size`; ticker cicla embaralhamentos da lista de
  papéis (`SpinTickerState<List<String>>`) até parar, resultado exibido como
  `jogador.zip(papéisEmbaralhados)`.

**Fase 3 — animação mais elaborada** (sem jogadores/ViewModel, só estado local — mesmo padrão
das ferramentas genéricas da Fase 1):

- **`presentation/components/EditableTextList.kt`** (novo): campo "+ Adicionar" + lista com
  botão de excluir por linha, extraído do bloco que `AssignRolesScreen` tinha inline (que foi
  refatorada pra usar esse componente) — reaproveitado também pela Roleta.
- **Dados** (`DiceRollerScreen.kt`): chips pra escolher o número de lados (d4/d6/d8/d10/d12/d20)
  + stepper pro número de dados (1-10) — `rememberSpinTicker` cicla `List<Int>` (uma rolagem
  por dado) até parar, exibido em cards numa grade simples (`chunked(5)` + `Row`s dentro de um
  `Column`, **não** `LazyVerticalGrid`, que quebra com "infinity height constraints" dentro de
  uma `Column` não roláveis por altura fixa — como o número de dados é pequeno (≤10), uma
  grade não-lazy é mais simples e evita esse problema) + total somado.
- **Roleta** (`SpinWheelScreen.kt`): opções customizadas via `EditableTextList`, desenhada com
  `Canvas` (`androidx.compose.foundation.Canvas`, **primeiro uso de `drawArc`/`nativeCanvas`
  no projeto** — antes só `LineChart.kt` usava `Canvas`, só pra linha/path, nunca fatias nem
  texto nativo) — fatias proporcionais (`drawArc(useCenter = true)`) alternando 2 cores
  (dourado/escuro, texto preto/branco por cima pra contraste), rótulo de cada fatia desenhado
  via `nativeCanvas.drawText` com `translate`+`rotate` pra ficar radial. **Rotação real do
  ponteiro é calculada matematicamente antes de girar**: escolhe o índice vencedor primeiro
  (`Random.nextInt`), calcula o ângulo de rotação final que traz o centro daquela fatia até o
  topo (onde fica o ponteiro "▼" fixo), soma `+ 5*360°` de voltas extras só pro efeito visual, e
  anima com `Animatable<Float>` + `tween(3200ms, FastOutSlowInEasing)` — o resultado nunca é
  "lido" da posição final da roda, é decidido antes e a animação só ilustra ele. A roleta
  alterna **3 tons** (dourado, âmbar `#B8860B`, escuro `#2C2C2C`) em vez de 2, pra diferenciar
  fatias vizinhas melhor com muitas opções — pedido do usuário depois do primeiro teste.
  **Layout travado**: a roda + resultado ficam num bloco fixo no topo (fora de qualquer
  `verticalScroll`), o botão "Girar" fixo na base, e só a lista de opções (`EditableTextList`)
  rola entre os dois — outro ajuste de usabilidade pedido depois do primeiro teste, pra roda e
  botão nunca saírem da tela conforme a lista de opções cresce.
  **⚠️ Bug já corrigido, cuidado ao reintroduzir**: o ponteiro "▼" usava
  `Modifier.padding(top = (-4).dp)` pra subir um pouco — `padding` **não aceita valor
  negativo** (`IllegalArgumentException: Padding must be non-negative`, só estourava depois de
  ter 2+ opções, quando a roda de fato renderizava) — o jeito certo pra deslocar um composable
  pra fora do fluxo normal (inclusive em direção negativa) é `Modifier.offset(y = ...)`, não
  `padding`.

**Fase 4 — utilidades e território tecnicamente novo** (todas sem ViewModel/DI, estado local só
— mesmo padrão das ferramentas genéricas; nenhuma persiste nada, tudo reseta ao sair da tela):

- **Placar avulso** (`ScratchScoreboardScreen.kt`): jogadores de texto livre (não precisam ser
  cadastrados — ferramenta rápida, sem fricção), cada um com contador +/- (pode ficar
  negativo, sem clamp — alguns jogos têm penalidade) e botão de remover, mais um "Zerar
  placar" geral. `LazyColumn` com `key = { it.id }` (UUID gerado na criação) pra manter
  identidade estável entre recomposições ao incrementar/decrementar.
- **Cronômetro por turno** (`TurnTimerScreen.kt`): 2 modos na mesma tela (`hasStarted`
  boolean) — modo configuração (`EditableTextList` de nomes + stepper de segundos por turno,
  10-300s) e modo cronômetro (nome do jogador da vez em destaque, contagem regressiva,
  `LinearProgressIndicator`, Pausar/Retomar, Próximo manual, Encerrar volta pra configuração).
  Tick via `LaunchedEffect(isRunning) { while (isRunning) { delay(1000); ... } }` — mesmo
  espírito do relógio ao vivo (`HomeScreen.ActiveTimerBanner`), só que contando pra baixo e
  fechando o turno automaticamente ao chegar em zero (`currentIndex = (currentIndex + 1) %
  players.size`).
- **Dedo na tela** (`FingerPickerScreen.kt`) — **item de maior risco técnico do plano,
  primeiro rastreio multi-toque do projeto**: `Modifier.pointerInput(Unit) {
  awaitPointerEventScope { while (true) { awaitPointerEvent() ... } } }` no `Box` de tela
  cheia, atualizando um `Map<PointerId, Offset>` a cada evento (adiciona no map quando
  `change.pressed`, remove quando solto) — essa é a API de baixo nível necessária pra
  múltiplos dedos simultâneos; os gesture detectors prontos do Compose
  (`detectDragGestures`/`detectTapGestures`, já usados em `RankingScoringStep.kt`) só cobrem 1
  ponteiro por vez. Interpretação deliberada de "dedos parados": estabilidade do **conjunto**
  de ponteiros (ninguém encostou/soltou), não ausência de micro-movimento de cada dedo —
  rastrear "sem tremer" de verdade seria bem mais complexo e não traz benefício real pro caso
  de uso. **Só dá pra validar direito com teste manual no device físico com múltiplos
  dedos/pessoas reais** — não há como simular multi-touch de verdade via `adb`.
  - **Máquina de estados** (revisada a pedido do usuário depois do primeiro teste, pra melhorar
    a animação): `touches` (ao vivo) → `lockedPositions` (congelado) → resultado. Assim que
    `touches.size >= 2`, um `LaunchedEffect(touches.keys, isLocked)` inicia uma contagem de
    **5s** (`repeat(5) { delay(1000); waitSecondsLeft-- }` — reinicia do zero se algum dedo
    encostar/soltar nesse meio tempo, já que `touches.keys` muda e recria o efeito). Ao
    zerar, `lockedPositions = touches` e `isLocked = true` — a partir daí o loop de
    `awaitPointerEvent()` **para de atualizar `touches`** (só continua checando se **todos**
    os dedos soltaram, pra liberar um novo sorteio) — isso mantém o sorteio e o resultado
    estáveis mesmo se alguém tirar/encostar outro dedo no meio da animação.
  - **Animação de sorteio reaproveita o `SpinTicker`** (mesmo hook de outras ferramentas,
    generalizado aqui pra `T = PointerId?`): `rememberSpinTicker(lockedPositions.keys,
    randomValue = { lockedPositions.keys.randomOrNull() })`, disparado num
    `LaunchedEffect(lockedPositions) { ticker.spin(lockedPositions.keys.random()) }` — durante
    o `spin()`, cada dedo travado é desenhado, mas só o que bate com `ticker.current` fica na
    cor dourada cheia (os outros ficam semitransparentes) — é isso que dá o efeito de "cor
    circulando" entre os dedos até desacelerar e parar no sorteado.
  - **Resultado**: quando `!ticker.isSpinning`, só o dedo vencedor (`lockedPositions[winnerId]`)
    continua sendo desenhado — os outros somem da tela — com o círculo animando
    (`animateDpAsState`) de 72dp pra **140dp**. Reset automático: assim que **todos** os dedos
    saem da tela (`event.changes.none { it.pressed }` enquanto `isLocked`), tudo volta pro
    estado inicial pra um novo sorteio.
  - **8 cores distintas** (`FINGER_COLORS`, pedido do usuário): cada dedo recebe uma cor pela
    ordem em que encostou (`colorForIndex(index) = FINGER_COLORS[index % 8]`, calculado sobre
    `touches.entries`/`lockedPositions.entries` — `Map` em Kotlin preserva ordem de inserção,
    então o índice é estável enquanto o dedo não solta) — dourado continua sendo a 1ª cor, as
    outras 7 cobrem o espectro (vermelho/laranja/verde/ciano/azul/roxo/rosa) pra ficar fácil
    diferenciar visualmente vários dedos ao mesmo tempo. Durante o sorteio, cada dedo mantém a
    própria cor mas só o "aceso" no momento fica em opacidade cheia; o resultado final mostra o
    vencedor na cor que já era dele (não vira dourado à força), só maior.

## Tema

Paleta da seção 1 do documento de produto: fundo `#121212`/superfície `#1E1E1E` (escuro, padrão) ou claro alternativo, dourado `#D4AF37` como cor de destaque (`Gold` em `ui/theme/Color.kt`). Tema escolhido persiste via DataStore (`ThemePreferences`), lido em `MainActivity` com `collectAsState(initial = true)` (padrão = escuro).

## Fluxo de build

- **Sempre rodar `./gradlew compileDebugKotlin` após qualquer alteração de código**, sem esperar o usuário pedir. Se mexer em dependências/versões do Kotlin, rodar limpo (`./gradlew clean compileDebugKotlin`) por causa do bug de compilador documentado acima.
- `local.properties` aponta pro mesmo SDK do ShopControl (`sdk.dir=...\Android\Sdk`) — já configurado, não versionado (`.gitignore`).
- Não há emulador Android disponível neste ambiente de desenvolvimento, mas o usuário costuma ter um **device físico conectado via `adb`** (`adb devices -l`) — quando houver um listado, rodar `./gradlew installDebug` após o `assembleDebug` bem-sucedido para instalar a build mais recente nele. Sem device conectado, a verificação fica só na compilação.
- Builds costumam levar 10-30s incrementais; a primeira após mudar `libs.versions.toml` pode levar 1-3 min e ocasionalmente precisa de uma segunda tentativa (cache de configuração invalidado gera um "Unresolved reference 'R'" transitório na primeira tentativa — rodar de novo resolve).
