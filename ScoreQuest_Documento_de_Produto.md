# ScoreQuest — Documento de Produto

Aplicativo Android para histórico de jogatinas de jogos de tabuleiro: catálogo, estante pessoal, registro de partidas com pontuação customizável, estatísticas e perfil social.

---

## 1. Identidade Visual e Estilo

Baseado no mockup de referência aprovado (tema escuro, estética "boutique" de board game).

### 1.1 Paleta de cores

| Uso | Cor | Hex (referência) |
|---|---|---|
| Fundo principal | Preto/quase-preto | `#121212` |
| Fundo de card/superfície | Cinza muito escuro | `#1E1E1E` |
| Cor de destaque (primária) | Dourado/âmbar | `#D4AF37` |
| Texto principal | Branco/quase-branco | `#F5F5F5` |
| Texto secundário | Cinza claro | `#A0A0A0` |
| Bordas/divisores | Cinza escuro sutil | `#2C2C2C` |
| Estado de sucesso (ex: vitória) | Verde discreto | `#4CAF50` |
| Estado de alerta/erro | Vermelho discreto | `#E57373` |

O dourado é a cor de destaque para **ações primárias** (botões principais, chips selecionados, ícones ativos, FAB) e **elementos de conquista/prestígio** (badges, troféus, streak). Não deve ser usado em excesso — funciona como acento, não como cor de fundo.

### 1.2 Tema

- **V1**: modo escuro como padrão/identidade visual da marca (não é só uma opção de acessibilidade, é o tema definidor do app)
- Modo claro disponível como alternativa em Configurações (ver seção 7.5), mas o dourado sobre escuro é a experiência de referência mostrada no mockup e deve orientar decisões de design

### 1.3 Tipografia

- Fonte sem serifa, moderna e legível em telas pequenas (ex: Inter, Roboto ou similar)
- Hierarquia: título de tela em peso bold/semibold, corpo de texto em peso regular, textos secundários (metadados, timestamps) em cinza claro e tamanho reduzido
- Números de destaque (contadores, pontuação) podem usar peso mais forte para chamar atenção nos cards

### 1.4 Componentes e padrões visuais

- **Cards escuros com cantos arredondados**: unidade visual repetida em quase toda tela (feed, biblioteca, perfil, convites) — manter raio de borda consistente em todo o app
- **Chips coloridos** para status (Tenho/Quero/Não tenho, Jogado) — cor de fundo diferenciada por status, mas sempre com o texto legível sobre fundo escuro
- **Avatares circulares** em toda referência a jogador/usuário (feed, convites, comentários, header de perfil)
- **Badges hexagonais** para conquistas (reservado para V3, gamificação)
- **Botão flutuante (FAB) dourado**, destacado sobre a bottom nav, para a ação de registrar partida
- **Grid de pontuação**: tabela com linhas/colunas bem demarcadas por bordas sutis (`#2C2C2C`), célula ativa/editável com leve destaque de fundo

### 1.5 Iconografia

- Ícones de linha (outline), não preenchidos, exceto quando ativos/selecionados (nesse caso preenchidos em dourado)
- Ícones temáticos de board game (dado, coroa, troféu, escudo) reservados para conquistas e métricas de destaque — uso pontual, não decorativo genérico

### 1.6 Observação

A paleta exata (hex) é uma referência inicial extraída do mockup e pode ser refinada com um design system mais formal (ex: tokens de cor no Compose `MaterialTheme`) antes da implementação final — o objetivo aqui é registrar a direção estética acordada, não travar valores definitivos de pixel/hex.

---

## 2. Visão Geral

| Item | Definição |
|---|---|
| Plataforma | Android (Kotlin, Jetpack Compose sugerido) |
| Persistência V1 | 100% local (Room), sem dependência de conectividade |
| Persistência V2+ | Sincronização em nuvem, contas de usuário |
| Monetização | Em aberto — arquitetura não deve travar um modelo específico |
| Login (V2) | Email/senha + login social (Google) |

---

## 3. Roadmap por Versão

### V1 — MVP (local)
- Acervo de jogos (incluindo peso/complexidade do jogo) + Estante pessoal + Empréstimos — **fonte: curadoria própria + adição manual pelo usuário** (ver seção 5)
- Avaliação de jogos por estrelas (rating pessoal do usuário)
- Registro de partidas (CRUD completo) com pontuação genérica
- Jogadores locais por apelido
- Perfil (avatar, bio, favoritos, atividades)
- Home com estatísticas básicas (streak, top jogos, tempo de jogo) — Home permanece focada em estatísticas enquanto a parte social não existir
- Navegação por bottom nav com botão flutuante (FAB) de ação central
- Onboarding + Export/backup manual

### V2 — Contas & Nuvem
- Contas de usuário reais (login email/senha + Google)
- Sincronização em nuvem
- **Identificador único de usuário (@username)**, público e pesquisável
- Vínculo Player local → User real, com convite/confirmação
- **Adicionar amigos por @username (busca) ou por QR Code (gerar/ler)** — modelo estritamente de **amigos mútuos** (ambos os lados confirmam; sem modelo de "seguir" assimétrico)
- Criador de schema de pontuação visual (modo simples: soma + multiplicador), com **tela de lançamento adaptada à complexidade do jogo** (ver seção 7.3)
- Adicionar jogos ao acervo (curadoria própria ou API do BGG) — **fonte: API da Ludopedia** (ver seção 5.2), condicionada à confirmação de termos comerciais
- Estatísticas comparativas: head-to-head, "nêmesis", evolução de desempenho
- Insights de coleção (jogos "tenho" nunca jogados)
- Amigos (lista) + **feed social de atividades com curtidas e comentários**
- Notificações (ex: "faz tempo que não joga X")
- Compartilhamento de resultado de partida (imagem pra redes sociais)
- Política de privacidade/LGPD (obrigatória com conta de usuário)
- **Transição da Home**: uma vez que a parte social esteja madura, a Home passa a ser centrada no feed de atividades (dos amigos), com as estatísticas pessoais migrando para a aba Perfil

### V3 — Avançado
- Construtor de fórmulas avançado
- Merge de jogadores duplicados
- Conquistas/gamificação (badges)
- Sugestões de pessoas para adicionar (com base em amigos mútuos/jogos em comum)
- Import do BGG
- Modo torneio/campeonato

---

## 4. Modelo de Dados (V1)

### 4.1 Convenção de auditoria

Todas as entidades persistidas herdam os mesmos três campos de auditoria. Exclusão é sempre **soft delete** — o registro nunca é removido fisicamente, só marcado com `deletedAt`, preservando referências de histórico (ex: uma `GameSession` que aponta pra um `Player` "excluído" continua íntegra).

```kotlin
interface Auditable {
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
    val deletedAt: LocalDateTime?  // null = registro ativo
}
```

Regra de query padrão: toda leitura de listagem filtra `WHERE deletedAt IS NULL`, a menos que a tela seja explicitamente de "itens excluídos"/lixeira (não prevista no V1, mas o campo já viabiliza isso no futuro).

### 4.2 Entidades

```kotlin
data class BoardGame(
    val id: String,
    val name: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val avgDurationMinutes: Int,
    val coverImageUrl: String?,   // caminho local (V1) ou URL remota (após sync no V2)
    val category: String?,
    val weight: Double?, // complexidade, escala 1.0 (leve) a 5.0 (pesado), estilo BGG
    val source: GameSource, // CURATED, USER_CREATED, LUDOPEDIA_IMPORT, BGG_IMPORT
    val createdByUserId: String? = null, // preenchido quando source = USER_CREATED
    val syncedAt: LocalDateTime? = null, // null enquanto não sincronizado com o backend (V2)
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

enum class GameSource { CURATED, USER_CREATED, LUDOPEDIA_IMPORT, BGG_IMPORT }

data class UserLibraryEntry(
    val gameId: String,
    val status: LibraryStatus, // HAVE, WANT, DONT_HAVE
    val played: Boolean = false, // true assim que o jogo é registrado em ao menos uma partida, independente do status
    val lentTo: String? = null, // apelido de quem pegou emprestado
    val rating: Int? = null, // avaliação pessoal do usuário, 1 a 5 estrelas
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

data class Player(
    val id: String,           // UUID local
    val nickname: String,
    val linkedUserId: String? = null,
    val avatarColor: String?,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

data class GameSession(
    val id: String,           // UUID local
    val gameId: String,
    val date: LocalDateTime,
    val durationMinutes: Int,
    val variantOrExpansion: String? = null,
    val photoUri: String? = null,
    val participantIds: List<String>,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

data class ScoreEntry(
    val sessionId: String,
    val playerId: String,
    val totalScore: Int?,      // resultado final: input direto (Simples) ou calculado pela fórmula (Composta Automático); null se Manual/Sem vencedor sem total aplicável
    val isWinner: Boolean?,
    val fieldValues: String? = null, // JSON blob {"fieldKey": valor}, preenchido somente quando a partida usa um schema COMPOSITE — null para Pontuação Simples
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable

// GameScoreSchema (V2) — define como a pontuação de um jogo específico é lançada e calculada.
// Definição completa (todos os campos, enums e regras de negócio) está na seção 8.6,
// que é a fonte única de verdade para esta entidade — não duplicar a classe aqui.
```

### 4.3 Mapeamento para colunas de banco (Room)

Como as entidades Room mapeiam para tabelas SQLite reais, os nomes de coluna seguem `snake_case` via `@ColumnInfo`, mesmo com as propriedades Kotlin em `camelCase`:

```kotlin
@Entity(tableName = "player")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val nickname: String,
    @ColumnInfo(name = "linked_user_id") val linkedUserId: String?,
    @ColumnInfo(name = "avatar_color") val avatarColor: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long, // epoch millis
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?
)
```

---

## 5. Fonte do Acervo de Jogos

Estratégia de população do catálogo (`BoardGame`), combinando quatro abordagens conforme a versão.

### 5.1 V1 — Curadoria própria (pré-carregada)

- Ficha de cada jogo (nome, jogadores, duração, categoria, peso, capa) escrita manualmente pela equipe, em português, `source = CURATED`
- Volume alvo: ~100-300 jogos mais relevantes/populares pra validar o produto sem depender de licenciamento de terceiros
- **Datasets públicos usados só como referência de pesquisa** (não como fonte direta de importação em massa), para identificar quais jogos priorizar e conferir dados objetivos (jogadores, duração):
  - Snapshots do BGG no Kaggle (ex: bases com 20-94 mil jogos raspados do BoardGameGeek) — úteis pra pesquisa, mas a origem dos dados (BGG) exige licença comercial caso o app venha a ser monetizado, então não devem ser importados diretamente pro banco de produção
  - **Ludopedia Rank** (Kaggle) e o dataset do projeto **project-Ludo** (GitHub) — snapshots menores, já em português e focados no mercado brasileiro, mais alinhados ao público do ScoreQuest; mesma cautela: usar como referência de curadoria, não import direto, até confirmação formal de termos de uso com a Ludopedia

### 5.2 V1 — Adição manual pelo usuário (`USER_CREATED`)

Complementa a curadoria: cobre jogos que ainda não estão no acervo pré-carregado (lançamentos recentes, jogos nacionais nichados, protótipos caseiros), sem bloquear o usuário esperando uma atualização de catálogo.

- Botão **"Adicionar Jogo"** na tela Jogos (ver seção 7.2), separado do FAB de partida
- Formulário simples: nome, min/max jogadores, duração média, categoria (opcional), peso (opcional), capa (câmera ou galeria), **status na estante** (Tenho/Quero/Não tenho — pergunta obrigatória, sem valor padrão pré-selecionado)
- Imagem salva **localmente** no armazenamento do app (não há upload/nuvem no V1); `coverImageUrl` guarda o caminho do arquivo local
- `UserLibraryEntry` é criada junto, com o `status` escolhido pelo usuário no formulário
- `source = USER_CREATED`, `createdByUserId` preenchido, `syncedAt = null`

### 5.3 V2 — Sincronização dos jogos criados pelo usuário

- Ao sincronizar com o backend, jogos `USER_CREATED` (ainda `syncedAt = null`) são enviados ao servidor: a imagem local sobe para storage em nuvem, `coverImageUrl` passa a apontar pra URL remota, e `syncedAt` é preenchido
- **Deduplicação**: se dois usuários diferentes cadastrarem manualmente o "mesmo" jogo (nome muito similar), isso gera entradas duplicadas no acervo global — vale prever, já no V2, uma rotina de moderação/merge (curadoria revisa jogos `USER_CREATED` populares e os promove a `CURATED`, fundindo duplicatas)

### 5.4 V2 — Expansão via API da Ludopedia

- Integração com a API REST/OAuth2 da Ludopedia (`ludopedia.com.br/api`) para:
  - Buscar e importar novos jogos ao acervo (reduz/elimina a curadoria manual contínua), `source = LUDOPEDIA_IMPORT`
  - Permitir que o usuário conecte a própria conta Ludopedia e importe coleção/histórico existente (recurso forte de onboarding)
- Exige backend próprio (fluxo OAuth2 authorization code não é seguro 100% client-side — precisa guardar `APP_KEY` no servidor)
- **Pré-requisito antes de arquitetar isso**: confirmar por e-mail (`api@ludopedia.com.br`) se uso comercial é permitido e em quais termos — item já registrado em "Itens em Aberto"

### 5.5 V3 (ou antes, se a Ludopedia negar uso comercial) — BoardGameGeek

- Alternativa/complemento à Ludopedia, com base muito maior (100mil+ jogos), `source = BGG_IMPORT`
- Requer licença comercial paga da BGG caso o app seja monetizado (confirmado nos termos oficiais)

---

## 6. Navegação

Bottom Navigation Bar com 4 itens fixos + **Floating Action Button (FAB)** sobreposto, central:

```
[Home]  [Jogos]   (FAB ➕)   [Perfil]  [Configurações]
```

- **Home, Jogos, Perfil, Configurações**: destinos reais na bottom nav (4 itens)
- **FAB (➕)**: não é um item da bottom nav, é um botão flutuante sobreposto (visual e estruturalmente separado da barra, como no Material Design padrão), sempre visível independente da aba ativa. Ao tocar, abre o fluxo fullscreen de criação de partida. A bottom nav permanece visível/acessível por trás do FAB; o fluxo de criação é que ocupa a tela cheia por cima de tudo.

---

## 7. Telas — Descrição Detalhada

### 7.1 Home

**Objetivo**: dashboard rápido do progresso e hábitos do usuário, com estatísticas visuais.

**Conteúdo geral**
- Saudação com nome do usuário
- Card de streak: "🔥 N dias seguidos jogando" OU "😴 N dias sem jogar" (mutuamente exclusivos)
- Tempo de jogo: semana atual + total acumulado
- **Heatmap de atividade** (detalhado em 7.1.1)
- **Ranking dos mais jogados** (detalhado em 7.1.2)
- **Linha do tempo de partidas por mês** (detalhado em 7.1.3)
- **Histograma de duração de partida** (detalhado em 7.1.4)
- Preview das últimas 3 partidas, com link "ver tudo" → aba Atividades do Perfil

**Estados vazios**: usuário sem nenhuma partida registrada vê uma chamada para ação ("Registre sua primeira partida" → botão que leva ao fluxo do FAB). Cada gráfico individualmente também precisa de um estado vazio (ver detalhamento de cada um abaixo) — não renderizar um gráfico "zerado"/quebrado antes de haver dado suficiente.

**Biblioteca sugerida**: [Vico](https://github.com/patrykandpatrick/vico) (`patrykandpatrick.vico`) para os gráficos de linha/barra/histograma, com tema customizado nas cores da seção 1 (fundo `#1E1E1E`, destaque `#D4AF37`). O heatmap não tem componente pronto em libs comuns — implementar como `LazyVerticalGrid` (ver 7.1.1).

---

#### 7.1.1 Heatmap de Atividade

**O que é**: grade de quadrados (estilo GitHub contribution graph), um quadrado por dia, com intensidade de cor proporcional ao nº de partidas jogadas naquele dia.

**Fonte de dados**
```kotlin
@Query("""
    SELECT DATE(date) as day, COUNT(*) as sessionCount
    FROM GameSession
    WHERE :playerId IN (participants) AND deletedAt IS NULL
      AND date >= :rangeStart
    GROUP BY DATE(date)
""")
fun getActivityByDay(playerId: String, rangeStart: LocalDate): List<DayActivity>

data class DayActivity(val day: LocalDate, val sessionCount: Int)
```

**Período exibido**: últimos 90 dias no card da Home (grade compacta ~13 semanas x 7 dias). Uma versão expandida (últimos 365 dias, como no GitHub) fica disponível ao tocar no card, abrindo em tela cheia ou bottom sheet.

**Escala de cor** (4-5 níveis, do fundo escuro até o dourado da paleta):
| Partidas no dia | Cor |
|---|---|
| 0 | `#2C2C2C` (mesma cor de borda/divisor, "apagado") |
| 1 | `#5C4A1A` (dourado bem escurecido) |
| 2 | `#8A6E20` |
| 3 | `#B08F28` |
| 4+ | `#D4AF37` (dourado cheio, cor de destaque) |

**Interação**: toque num quadrado mostra um tooltip/popover simples: "12 de março — 2 partidas". Não navega pra outra tela no V1 (evita complexidade de deep link por data).

**Implementação sugerida**: `LazyVerticalGrid(columns = GridCells.Fixed(7))` (uma coluna por dia da semana, como o GitHub gira 7 linhas x N colunas — pode implementar transposto e usar `LazyRow` de colunas semanais, cada uma um `Column` de 7 dias, se quiser scroll horizontal pro histórico completo). Cada célula é um `Box` colorido de tamanho fixo (~12-14dp) com pequeno espaçamento.

**Estado vazio**: se não houver nenhuma partida nos últimos 90 dias, mostrar a grade toda "apagada" (`#2C2C2C`) em vez de esconder o componente — reforça visualmente a ausência de atividade, incentivando a jogar.

---

#### 7.1.2 Ranking dos Mais Jogados (barras horizontais)

**O que é**: substitui a lista "🥇🥈🥉" por um gráfico de barras horizontais, cada barra representando um jogo, comprimento proporcional ao nº de partidas.

**Fonte de dados**
```kotlin
@Query("""
    SELECT bg.id as gameId, bg.name, COUNT(*) as playCount
    FROM GameSession gs
    JOIN BoardGame bg ON bg.id = gs.gameId
    WHERE :playerId IN (gs.participants) AND gs.deletedAt IS NULL
    GROUP BY gs.gameId
    ORDER BY playCount DESC
    LIMIT 5
""")
fun getTopPlayedGames(playerId: String): List<GamePlayCount>

data class GamePlayCount(val gameId: String, val name: String, val playCount: Int)
```

**Exibição**: top 5 jogos, barra do jogo mais jogado sempre em dourado cheio (`#D4AF37`), demais em tom mais neutro (ex: `#A0A0A0` ou dourado escurecido) para destacar só o 1º lugar. Nome do jogo à esquerda da barra, contagem numérica à direita/dentro da barra.

**Interação**: toque na barra navega pro detalhe do jogo (tela de detalhe já especificada na seção 7.2).

**Estado vazio**: card não aparece (ou aparece com texto "Jogue sua primeira partida pra ver seu ranking aqui") se não houver nenhuma `GameSession`.

**Componente Vico**: `ColumnChart` do Vico configurado na horizontal (ou `HorizontalBarChart`, dependendo da versão da lib) — checar a API atual do Vico no momento da implementação, já que a lib evolui rápido.

---

#### 7.1.3 Linha do Tempo — Partidas por Mês

**O que é**: gráfico de linha (ou área) mostrando o volume de partidas jogadas mês a mês, no ano corrente.

**Fonte de dados**
```kotlin
@Query("""
    SELECT strftime('%m', date) as month, COUNT(*) as sessionCount
    FROM GameSession
    WHERE :playerId IN (participants) AND deletedAt IS NULL
      AND strftime('%Y', date) = :year
    GROUP BY strftime('%m', date)
""")
fun getSessionsByMonth(playerId: String, year: String): List<MonthActivity>

data class MonthActivity(val month: String, val sessionCount: Int)
```

**Exibição**: eixo X = Jan-Dez (meses sem dado renderizam como 0, não ficam ausentes no eixo — mantém a escala visual correta), eixo Y = nº de partidas. Linha em dourado, com leve preenchimento de área abaixo (gradiente dourado→transparente) para dar volume visual sem poluir.

**Interação**: nenhuma interação obrigatória no V1 (só leitura). V2 pode considerar tocar num ponto do mês pra ver lista de partidas daquele mês.

**Estado vazio**: se o usuário tem menos de 2 meses com dado, o gráfico de linha fica pouco informativo — considerar mostrar como texto simples ("Você começou a registrar partidas em [mês]") em vez do gráfico até haver dado suficiente (ex: mínimo 3 meses com ao menos 1 partida).

**Componente Vico**: `LineChart` padrão da lib.

---

#### 7.1.4 Histograma de Duração de Partida

**O que é**: distribuição de partidas por faixa de duração, respondendo "eu jogo mais partidas rápidas ou longas?".

**Faixas fixas** (buckets):
- 0-30 min
- 30-60 min
- 1-2h
- 2h+

**Fonte de dados**
```kotlin
@Query("""
    SELECT
        CASE
            WHEN durationMinutes <= 30 THEN '0-30min'
            WHEN durationMinutes <= 60 THEN '30-60min'
            WHEN durationMinutes <= 120 THEN '1-2h'
            ELSE '2h+'
        END as bucket,
        COUNT(*) as sessionCount
    FROM GameSession
    WHERE :playerId IN (participants) AND deletedAt IS NULL
    GROUP BY bucket
""")
fun getDurationHistogram(playerId: String): List<DurationBucket>

data class DurationBucket(val bucket: String, val sessionCount: Int)
```
Nota: `CASE` em SQL puro funciona no Room via `@RawQuery`/query customizada; se preferir manter tudo em `@Query` tipado, alternativa é trazer só `durationMinutes` de todas as sessões e bucketizar em Kotlin (mais simples de manter, ligeiramente menos eficiente — como o volume de partidas de um usuário individual é baixo, isso não é problema de performance).

**Exibição**: gráfico de barras verticais simples, uma barra por faixa, ordem fixa da mais curta pra mais longa.

**Estado vazio**: esconder o card se houver menos de 5 partidas registradas no total — com poucos dados, o histograma não é informativo e pode até confundir.

**Componente Vico**: `ColumnChart` padrão.

---

### 7.2 Jogos (Acervo + Estante)

**Objetivo**: consultar o catálogo de jogos e gerenciar a coleção pessoal.

**Sub-abas ou filtro superior**
- **Todos** (acervo completo)
- **Minha estante** (filtra por status: Tenho / Quero / Não tenho)
- **Jogados** (filtro adicional, independente do status — jogos com `played = true`)

**Conteúdo por item de jogo**
- Capa, nome, nº de jogadores, duração média
- Tag de status (chip colorido: Tenho / Quero / Não tenho)
- Selo "Jogado" quando aplicável — não é mutuamente exclusivo com o status (ex: um jogo pode ser "Não tenho" + "Jogado", caso jogado na casa de um amigo)
- Se "Tenho" e emprestado: indicador "emprestado para [apelido]"

**Busca e filtros**
- Busca por nome
- Filtro por categoria, nº de jogadores, duração

**Botão "Adicionar Jogo"**
- Ícone/botão no topo da tela (não é o FAB de partida — ação independente)
- Abre formulário de criação manual (ver seção 5.2): nome, min/max jogadores, duração, categoria, peso e capa (câmera/galeria), tudo salvo localmente
- Jogo criado aparece imediatamente no acervo com um selo discreto "Adicionado por você" (visível só localmente no V1; no V2, pode indicar se já foi sincronizado/promovido a jogo oficial)

**Tela de detalhe do jogo** (ao tocar em um item)
- Informações completas do jogo
- Botão para alterar status na estante (Tenho/Quero/Não tenho)
- Se "Tenho": campo para marcar empréstimo
- **Avaliação por estrelas (1 a 5)**: registrada na própria `UserLibraryEntry` do jogo — como toda partida gera/atualiza a entrada com `played = true`, qualquer jogo já jogado pode ser avaliado, mesmo sem estar marcado como "Tenho"
- Histórico de partidas daquele jogo especificamente (reaproveita o componente de lista da aba Atividades)
- Estatísticas do jogo: nº de vezes jogado, duração média real, recorde de pontuação

---

### 7.3 Adicionar Partida (fluxo do FAB)

**Objetivo**: registrar uma nova sessão de jogo, em etapas.

**Etapa 1 — Escolher jogo**
- Busca/seleção a partir do acervo (prioriza jogos da estante "Tenho")

**Etapa 2 — Dados da sessão**
- Data (padrão: hoje)
- Duração (em minutos, com opção de cronômetro futuro em V2)
- Variante/expansão (campo opcional)
- Foto do tabuleiro (opcional, câmera ou galeria)

**Etapa 3 — Jogadores**
- Busca/criação por apelido (autocomplete com "+ criar novo")
- Multi-seleção dos participantes daquela partida

**Etapa 4 — Pontuação**

A forma de lançar a pontuação depende de como aquele jogo foi configurado (schema simples vs. complexo — ver seção 8, "Criador de Pontuação Personalizado"), mas em todos os casos **o preenchimento acontece no mesmo aparelho**, passado entre os jogadores presentes fisicamente — não há lançamento remoto/distribuído entre celulares diferentes.

- **Se o jogo tem um schema `SIMPLE`**: **uma tela só**, em formato de grid — jogadores nas colunas, campo único de pontuação nas linhas, todos preenchidos juntos na mesma tela. Vencedor = maior pontuação (regra fixa)
- **Se o jogo tem um schema `COMPOSITE`**: **uma tela por jogador**, navegável em sequência (ex: swipe ou "Próximo jogador"), mas todas as telas preenchidas em seguida no mesmo aparelho, sem enviar/receber de outro dispositivo. Vencedor conforme a regra definida no schema (automática ou manual)
- V1: independente da complexidade, só existe o modo "pontuação genérica" (total numérico + vencedor) — a diferenciação simples/complexo só passa a existir a partir do V2, quando o schema por jogo é implementado

**Etapa 5 — Confirmação**
- Resumo da partida antes de salvar
- Botão "Salvar partida" → volta para a tela de onde o usuário veio, com a bottom nav restaurada

**Edição/exclusão**: acessível a partir do detalhe de uma partida já salva (na aba Atividades ou no detalhe do jogo), reaproveitando as mesmas etapas em modo edição.

---

### 7.4 Perfil

**Objetivo**: identidade do usuário, estatísticas pessoais e histórico completo.

**Conteúdo**
- Avatar (upload local via galeria/câmera)
- Nome de usuário (placeholder em V1, editável a partir de V2)
- Descrição/bio (texto curto, ex.: até 150 caracteres)
- Botão "Editar Perfil" (avatar, bio — username fica bloqueado até V2)
- Contadores: nº de partidas (contador de amigos oculto até V2)

**Aba Jogos Favoritos**
- Exibição de até 3 jogos escolhidos pelo usuário
- Botão "editar favoritos" → busca na estante, seleção limitada a 3 (ao tentar marcar um 4º, bloqueia ou substitui o mais antigo, com aviso)

**Aba Atividades**
- Lista paginada (scroll infinito, via Paging 3) de todas as partidas do usuário, mais recentes primeiro
- Cada item: nome do jogo, tempo relativo ("há 2 dias"), nº de jogadores, duração, resultado do usuário (pontuação + vitória/derrota, quando aplicável)
- Toque no item → tela de detalhe da partida (edição/exclusão disponível aqui)

---

### 7.5 Configurações

**Conteúdo sugerido para V1**
- Tema (claro/escuro)
- Exportar dados (gera arquivo JSON com todo o histórico local)
- Sobre o app / versão
- Gerenciar jogadores locais (editar apelido, excluir jogador sem histórico)

**V2+**
- Conta (login, logout, dados vinculados)
- Notificações (ativar/configurar lembretes tipo "faz tempo que não joga X")
- Privacidade (LGPD — gestão de dados, exclusão de conta)

---

### 7.6 Adicionar Amigos (V2)

**Objetivo**: conectar o `Player` local do usuário a outra conta real, via duas vias.

**Modelo social**: estritamente **amigos mútuos** — não existe modelo de "seguir" assimétrico (sem "seguidores"/"seguindo" independentes). Uma conexão só existe depois que ambos os lados confirmam.

**Opção A — por @username**
- Campo de busca, digita o `@username` do outro usuário
- Resultado mostra nome, avatar e botão "Adicionar"
- Envia convite; vínculo só se confirma quando o outro lado aceitar (mesma regra de confirmação já definida pro vínculo Player→User)

**Opção B — por QR Code**
- Aba com duas sub-opções: **Meu QR Code** (gera um código único vinculado ao `@username`) e **Ler QR Code** (abre câmera)
- Caso de uso principal: duas pessoas jogando presencialmente, uma mostra o QR na tela, a outra escaneia — resolve o "adicionar amigo" sem digitar nada
- Ao escanear, mesmo fluxo de convite/confirmação da opção A (não pula a etapa de aceite, mesmo sendo presencial — evita abuso caso o QR seja compartilhado indevidamente, ex.: print/screenshot)

**Tela "Meus Amigos"**
- Lista de amigos confirmados, com atalho pra ver estante/perfil público de cada um
- Convites pendentes (enviados e recebidos) em uma sub-aba separada

---

### 7.7 Onboarding (primeiro uso)

**Objetivo**: orientar o usuário nas primeiras ações sem fricção.

**Fluxo sugerido**
1. Tela de boas-vindas (breve, 1-2 telas explicando o propósito do app)
2. Sugestão de adicionar os primeiros jogos à estante (a partir do acervo)
3. Convite para registrar a primeira partida (call-to-action direto pro fluxo do FAB)

---

## 8. Criador de Pontuação Personalizado (V2)

Construtor 100% visual, sem exposição de código/JSON ao usuário, que define como a pontuação de um jogo específico é lançada e calculada. Substitui a pontuação genérica (total + vencedor) do V1 para jogos que tiverem um schema configurado.

### 8.1 Quem cria e onde

- Acessível a partir da tela de detalhe do jogo (seção 7.2), botão "Configurar pontuação personalizada"
- **Um jogo tem exatamente um schema**, único e editável — sem versionamento, sem escopo Global/Pessoal (simplificações descartadas nesta fase; ficam registradas como possíveis extensões futuras em 8.7, caso a necessidade reapareça)
- Botão **"Editar"** disponível na tela de detalhe do schema, permitindo alterar campos, fórmula e regra de vencedor a qualquer momento — a edição sobrescreve o schema existente diretamente (partidas antigas mantêm os dados já lançados, mas o schema em si não é versionado; ver nota de escopo abaixo)

**Nota de escopo — permissão de edição, por fase de distribuição:**

| Fase | Quem pode editar |
|---|---|
| **Versão atual (uso próprio/interno)** | Qualquer usuário — botão "Editar" sempre visível, sem restrição |
| **Versões futuras distribuídas publicamente** | Apenas **usuários permitidos** (papel/permissão a definir — ex: curadoria própria, moderadores). Para o público geral, o botão "Editar" **não existe**; a pontuação por jogo passa a ser somente leitura/uso, mantida por quem tem permissão |

Isso segue o mesmo padrão já usado pro acervo de jogos (`CURATED` vs `USER_CREATED`, seção 5) — controle de qualidade centralizado quando o app for distribuído, mas sem essa fricção na fase atual, onde o objetivo é validar o produto rapidamente.

### 8.2 Primeira pergunta: Simples ou Composta

Antes de qualquer configuração de campo, o construtor pergunta o **tipo de pontuação** — essa escolha define toda a experiência de lançamento, não é inferida depois pela quantidade de campos.

| | **Pontuação Simples** | **Pontuação Composta** |
|---|---|---|
| Tela de lançamento | Única, todos os jogadores juntos (grid) | Uma tela por jogador, em sequência (contexto explicado ao criador — ver 8.4) |
| Campos configuráveis | Nenhum — campo fixo único "Pontuação" | Livre (ver 8.3, tipos de campo), começando por um card "+" |
| Definição do vencedor | Fixa: **maior pontuação vence** | Escolhida na tela de Montagem do Vencedor (8.5): Manual, Automático (fórmula visual) ou Sem vencedor |

- **Pontuação Simples**: não passa pelo restante do construtor — usuário confirma o tipo e salva direto. É essencialmente a formalização da pontuação genérica do V1 como uma opção explícita do V2, pra jogos que realmente só têm "quem fez mais pontos".
- **Pontuação Composta**: segue para os passos 8.3-8.5 abaixo (adicionar campos, montagem do vencedor).

```kotlin
enum class ScoreSchemaType { SIMPLE, COMPOSITE }
```

### 8.2.1 Duplicar de outro jogo

Antes (ou em vez) de começar do zero, a tela inicial do construtor oferece "Duplicar de outro jogo" como atalho, útil para jogos com pontuação parecida (ex: expansões do mesmo jogo, ou jogos da mesma "família" de mecânica).

- Usuário busca um jogo do acervo que já tenha um schema Composta configurado
- O construtor pré-preenche todos os campos e a fórmula daquele schema como ponto de partida
- Usuário segue editando normalmente a partir daí (adicionar/remover/renomear campos, ajustar fórmula) antes de salvar como o schema do jogo atual — a duplicação **não** cria nenhum vínculo entre os dois schemas depois de salva; são independentes a partir desse momento (editar um não afeta o outro)



### 8.3 Tipos de campo (somente Pontuação Composta)

| Tipo | Seleção | Pontua? | Caso de uso |
|---|---|---|---|
| Número (`NUMBER`) | — | Sim (direto ou com multiplicador) | Contadores, moedas, pontos de vitória |
| Sim/Não (`BOOLEAN`) | única | Opcional | Objetivo cumprido, condição binária |
| Escolha única (`ENUM`) | uma opção | Opcional (por opção) | Facção, resultado, categoria exclusiva |
| Múltipla escolha (`MULTI_SELECT`) | várias opções | Opcional (por opção) | Conquistas, bônus acumuláveis |
| Texto (`TEXT`) | — | Não | Observações, anotações livres |

**Modelo de dados**

```kotlin
sealed class ScoreFieldType {
    data class NumberField(
        val key: String,
        val label: String,
        val default: Int = 0,
        val min: Int? = null,
        val max: Int? = null,
        val allowNegative: Boolean = false
    ) : ScoreFieldType()

    data class BooleanField(
        val key: String,
        val label: String,
        val pointsIfChecked: Int? = null // null = só anotação, não pontua
    ) : ScoreFieldType()

    data class EnumField(
        val key: String,
        val label: String,
        val options: List<EnumOption>
    ) : ScoreFieldType()

    data class MultiSelectField(
        val key: String,
        val label: String,
        val options: List<EnumOption>
    ) : ScoreFieldType()

    data class TextField(
        val key: String,
        val label: String
    ) : ScoreFieldType()
}

data class EnumOption(
    val label: String,
    val points: Int = 0 // 0 = opção sem impacto direto no total
)
```

### 8.4 Fluxo de criação completo (visual, sem código)

1. Usuário abre "Configurar pontuação personalizada" a partir da tela do jogo
2. **Escolhe o tipo**: Simples ou Composta (8.2) — ou opta por **"Duplicar de outro jogo"** (8.2.1) como atalho, pré-preenchendo os passos seguintes com os dados do schema duplicado, que seguem editáveis normalmente
   - Se Simples → pula direto pro passo 7
3. *(Composta)* **Tela de contexto**: antes de começar a adicionar campos, um banner/texto fixo no topo explica: *"Esta configuração será preenchida uma vez para cada jogador ao lançar o placar da partida"* — garante que o criador do schema entenda que está desenhando um formulário por-jogador, não uma tela única compartilhada
4. *(Composta)* **Adiciona campos**: a tela começa com um único **card "+"** (borda tracejada, ícone de adicionar). Ao tocar:
   - Abre um seletor visual com os 5 tipos de campo (8.3), em formato de lista ou grid de opções
   - Ao escolher um tipo, abre o formulário de configuração específico daquele tipo (rótulo, valor padrão, min/máx, permitir negativo, opções, etc. — conforme já detalhado em 8.3)
   - Ao confirmar, o campo configurado vira um card na lista, e o card "+" desce pro final, pronto pra adicionar o próximo campo
5. *(Composta)* Reordena campos existentes por drag-and-drop (a ordem de criação é a ordem de exibição na tela de lançamento)
6. *(Composta)* Ao finalizar os campos, avança pra **tela de Montagem do Vencedor** (8.5)
7. *(Composta)* **Modo de teste** (8.5.1): antes de salvar, simula o schema com dados fictícios para 2 jogadores
8. Salva — schema passa a valer para futuras partidas daquele jogo

### 8.5 Montagem do Vencedor (somente Pontuação Composta)

Tela dedicada, exibida depois que todos os campos já foram criados. Substitui os antigos passos separados de "cálculo do total" e "regra de vencedor" — agora é uma única decisão com duas (ou três) rotas.

**Opção A — Manual**
- Nenhum cálculo é feito pelo app
- Na tela de lançamento da partida, depois que todos os campos forem preenchidos para todos os jogadores, aparece um passo final: "Quem venceu?" — o próprio usuário seleciona o vencedor entre os participantes
- Ideal pra jogos onde o resultado depende de julgamento (ex: melhor construção, votação) e não de soma direta de números

**Opção B — Automático (equação visual)**
- O criador monta uma fórmula de pontuação usando os campos já criados, através de um **construtor visual de termos** (sem digitar texto/sintaxe):
  - Toca em "+ Adicionar termo", escolhe um campo elegível (ver tabela abaixo — campos puramente de anotação, como Texto ou Sim/Não sem pontos definidos, não aparecem como opção)
  - Define o peso daquele termo com um stepper (ex: `×1`, `×2`, `×-1` — peso negativo cobre subtração, ex: "cartas de penalidade valem -3 cada")
  - Repete pra quantos campos quiser incluir; campos não adicionados como termo simplesmente não entram no total
- Total do jogador = soma de `(valor do campo × peso do termo)` para todos os termos

**Valor considerado por tipo de campo** (o que entra na multiplicação pelo peso):

| Tipo de campo | Elegível como termo? | Valor usado |
|---|---|---|
| Número | Sim | O número preenchido pelo jogador |
| Sim/Não | Sim, só se `pointsIfChecked` estiver definido | `pointsIfChecked` se marcado, senão 0 |
| Escolha única | Sim | Pontos da opção selecionada (`EnumOption.points`) |
| Múltipla escolha | Sim | Soma dos pontos de todas as opções marcadas |
| Texto | Não | — (nunca pontua) |

O **peso do termo (`weight`) multiplica o valor acima** — para a maioria dos casos, o peso fica em `×1` (o campo entra "como está", já que campos de Escolha única/Múltipla já carregam seus próprios pontos por opção). Usar um peso diferente de `×1` num campo que já tem pontos por opção é uma decisão deliberada de "pontuar dobrado" — o construtor deve deixar isso visualmente claro (ex: "×2" ao lado do termo) pra não virar um erro de configuração não percebido.

- Depois de montada a fórmula, escolhe a regra de vitória sobre esse total: **maior total vence** ou **menor total vence**
- **Pré-visualização**: enquanto os termos são montados, um texto de leitura (gerado pelo app, não digitado pelo usuário) mostra a fórmula resultante em linguagem legível, ex: *"Total = Ovos ×2 + Moedas ×1 − Penalidades ×1"* — ajuda o criador a validar visualmente antes de salvar

**Critério de desempate**: quando dois ou mais jogadores calculam o mesmo total (o "maior" ou "menor", conforme a regra escolhida), o app **não decide sozinho** — na tela de lançamento da partida (7.3), ao final do preenchimento, se um empate for detectado o app avisa explicitamente e pergunta o que fazer:
- *"[Jogador A] e [Jogador B] empataram com [N] pontos. O que você quer fazer?"*
  - **Considerar empate** → ambos os jogadores envolvidos são marcados como vencedores (`isWinner = true` para os dois em `ScoreEntry`)
  - **Escolher o vencedor manualmente** → usuário seleciona um dos jogadores empatados como o vencedor único (útil pra jogos com regra de desempate própria, ex: "quem tem mais recursos guardados vence no empate", que o app não tem como calcular automaticamente sem essa regra estar modelada)
- Esse aviso só aparece se houver empate de fato — no caso comum (totais diferentes), o vencedor é atribuído automaticamente sem nenhuma pergunta extra

**Opção C — Sem vencedor**
- Pra jogos cooperativos, onde não existe "quem ganhou" individualmente — só existe resultado do grupo (vitória/derrota coletiva)
- Nesse caso, os campos numéricos continuam sendo preenchidos por jogador (estatística individual), mas nenhum vencedor é calculado ou perguntado

```kotlin
enum class WinnerMode { MANUAL, AUTOMATIC, NONE }

data class ScoreFormula(
    val terms: List<ScoreTerm>,
    val comparisonRule: ComparisonRule // HIGHEST_WINS, LOWEST_WINS — só relevante se winnerMode = AUTOMATIC
)

data class ScoreTerm(
    val fieldKey: String,
    val weight: Double = 1.0 // pode ser negativo, cobre subtração; multiplicação por peso constante
)

enum class ComparisonRule { HIGHEST_WINS, LOWEST_WINS }
```

### 8.5.1 Modo de teste (antes de salvar)

Última etapa do fluxo (Composta), acessível a partir da tela de Montagem do Vencedor: um botão "Testar com dados fictícios" abre uma prévia com **2 jogadores de exemplo** ("Jogador A", "Jogador B"), reaproveitando exatamente a mesma UI que será usada na tela real de lançamento (grid ou uma tela por jogador, conforme o caso).

- Usuário preenche valores de teste livremente (sem gravar nenhuma `GameSession` real)
- O app mostra o total calculado (se `winnerMode = AUTOMATIC`) e quem seria o vencedor, em tempo real, conforme os campos de teste são preenchidos
- Objetivo: pegar erros de configuração antes de usar o schema numa partida de verdade (ex: peso errado, campo esquecido na fórmula, regra de vencedor invertida)
- Opcional — usuário pode pular direto pra "Salvar" sem testar, mas o botão fica sempre visível/sugerido nessa etapa final

> **Nota de escopo**: esse construtor de fórmula (soma ponderada com pesos positivos/negativos por campo) é mais flexível do que o "modo simples" que eu tinha desenhado antes — ele já cobre subtração e multiplicação por peso constante, sem esperar o V3. Fórmulas envolvendo dois campos multiplicados entre si (ex: "campo A × campo B", ambos variáveis por partida) continuam fora de escopo por ora — não surgiu um caso de uso claro pra isso ainda, mas fica registrado como possível extensão futura se aparecer a necessidade.

### 8.6 Entidade do schema

```kotlin
data class GameScoreSchema(
    val id: String,
    val gameId: String, // um schema por jogo — constraint de unicidade: no máximo um registro ativo com (gameId, deletedAt IS NULL)
    val type: ScoreSchemaType, // SIMPLE ou COMPOSITE — determina qual UI de lançamento é usada (ver 7.3)
    val fields: List<ScoreFieldType> = emptyList(), // vazio quando type = SIMPLE
    val winnerMode: WinnerMode, // irrelevante quando type = SIMPLE (regra fixa: maior pontuação vence, sem fórmula configurável)
    val formula: ScoreFormula? = null, // preenchido somente quando type = COMPOSITE e winnerMode = AUTOMATIC
    val createdByUserId: String, // autoria/registro histórico — não define permissão de edição por si só (ver nota de permissão em 8.1)
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable
```

A edição sobrescreve o registro existente (`updatedAt` reflete a última alteração); não há histórico de versões anteriores mantido — ver "Versionamento" em 8.7.

Armazenamento: `fields` e `formula` persistidos como JSON serializado numa coluna do Room (não precisa de tabela relacional por campo) — mesmo padrão de blob JSON usado por `ScoreEntry.fieldValues` (ver seção 4.2) pra guardar os valores lançados por jogador em cada partida.

### 8.7 Itens em aberto desta feature

- **Versionamento**: o schema atual é editado em cima do mesmo registro, sem versionar — decisão deliberada por ora (ver nota de escopo em 8.1). Se no futuro isso causar problema de estatísticas inconsistentes entre partidas antigas e novas (ver riscos discutidos), reavaliar
- **Permissão de edição em versões distribuídas**: qual será exatamente o critério de "usuário permitido" (papel fixo tipo curador/moderador, allowlist manual, ou outro modelo) — a definir antes da primeira distribuição pública (ver nota de escopo em 8.1)
- **Escopo Global vs. Pessoal**: alternativa considerada e descartada por ora (permitiria cada usuário ter sua própria variação sem mexer na de terceiros) — fica registrada como candidata caso o modelo de "usuário permitido" da fase distribuída se mostre insuficiente
- **Empate no modo Automático**: resolvido — ver seção 8.5, "Critério de desempate" (o app pergunta ao usuário: considerar empate com dupla vitória, ou escolher manualmente)

---

## 9. Pontos de Atenção Técnica

- **IDs**: gerar todos os IDs locais (Player, GameSession) como UUID, não sequenciais — evita conflitos quando a sincronização em nuvem (V2) for implementada
- **Auditoria**: todas as entidades possuem `createdAt`, `updatedAt`, `deletedAt` (soft delete). Nomes de campo/coluna sempre em inglês, `camelCase` no domínio Kotlin e `snake_case` nas colunas Room/SQLite. `updatedAt` também serve como base pra resolução de conflitos na sincronização do V2
- **Gráficos da Home**: usar a biblioteca **Vico** (`com.patrykandpatrick.vico`) para os gráficos de barra/linha/histograma (seções 7.1.2, 7.1.3, 7.1.4), com tema customizado nas cores da seção 1. O heatmap de atividade (7.1.1) é implementado à mão via `LazyVerticalGrid`, sem lib externa. Todas as queries agregadas desses gráficos podem ser recalculadas on-the-fly via Room (volume de dados de um único usuário é baixo o suficiente pra não precisar de cache/pré-computação no V1)
- **Analytics**: separar métricas universais (contagem, duração, win/loss — funcionam pra qualquer jogo) de métricas específicas de schema (só existem em V2, quando houver pontuação customizada por jogo)
- **Feature flags**: não hardcodar limites de uso (ex: nº de jogos na estante) diretamente no código, já que o modelo de monetização ainda está em aberto
- **Privacidade de vínculo**: vínculo Player local → User real (V2) deve exigir confirmação do usuário vinculado, nunca ser automático
- **QR Code**: geração via biblioteca tipo ZXing (`com.google.zxing`), encapsulando apenas o `@username` (ou um token de convite de curta duração, mais seguro que expor o username bruto) — leitura via CameraX + ZXing. O QR nunca deve efetivar o vínculo direto; sempre passa pelo fluxo padrão de convite/confirmação
- **@username**: precisa de validação de unicidade no backend (V2), normalização (lowercase, sem espaços/acentos) e checagem de disponibilidade em tempo real na tela de criação de conta
- **UserLibraryEntry auto-criada**: ao salvar uma `GameSession`, se o jogo ainda não tiver uma `UserLibraryEntry` pra aquele usuário, uma é criada automaticamente com `status = DONT_HAVE` e `played = true`; se já existir, apenas atualiza `played = true`. Isso garante que rating e o filtro "Jogados" funcionem pra qualquer jogo, independente de estar na estante
- **Imagem de jogo criado pelo usuário**: salva em armazenamento interno do app (ex: `context.filesDir`), não na galeria pública — `coverImageUrl` guarda o path local. Precisa de rotina de limpeza (evitar arquivos órfãos se o jogo for excluído) e de compressão/resize antes de salvar, já que capas em alta resolução acumulam espaço rápido em um catálogo que cresce por múltiplos usuários criando jogos
- **Sync de jogos `USER_CREATED`** (V2): ao logar/sincronizar, o app varre jogos locais com `syncedAt = null`, faz upload da imagem pro storage em nuvem, envia os dados ao backend e atualiza `coverImageUrl` (URL remota) + `syncedAt`. Precisa de estratégia de retry caso o upload falhe (rede instável) sem duplicar o registro

---

## 10. Itens em Aberto (decidir antes da V2)

- Modelo de monetização definitivo
- Backend de autenticação: Firebase Auth vs. backend próprio (C#/EF Core)
- Regras específicas de LGPD para dados de partidas compartilhadas entre jogadores
- Confirmação com a Ludopedia (`api@ludopedia.com.br`) sobre termos de uso comercial da API, antes de arquitetar a integração da seção 5.4
- Critério de "usuário permitido" para editar pontuação personalizada nas versões distribuídas do Criador de Pontuação Personalizado (ver seção 8.7)
