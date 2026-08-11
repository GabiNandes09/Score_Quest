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
    val totalScore: Int?,
    val isWinner: Boolean?,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val deletedAt: LocalDateTime? = null
) : Auditable
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

**Objetivo**: dashboard rápido do progresso e hábitos do usuário.

**Conteúdo**
- Saudação com nome do usuário
- Card de streak: "🔥 N dias seguidos jogando" OU "😴 N dias sem jogar" (mutuamente exclusivos)
- Top 3 jogos mais jogados (nome + contagem)
- Tempo de jogo: semana atual + total acumulado
- Preview das últimas 3 partidas, com link "ver tudo" → aba Atividades do Perfil

**Estados vazios**: usuário sem nenhuma partida registrada vê uma chamada para ação ("Registre sua primeira partida" → botão que leva ao fluxo do FAB)

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

A forma de lançar a pontuação depende de como aquele jogo foi configurado (schema simples vs. complexo — ver Roadmap V2, seção 3), mas em todos os casos **o preenchimento acontece no mesmo aparelho**, passado entre os jogadores presentes fisicamente — não há lançamento remoto/distribuído entre celulares diferentes.

- **Jogo simples** (uma única categoria de pontuação, ex: total direto): **uma tela só**, em formato de grid — jogadores nas colunas, pontuação nas linhas, todos preenchidos juntos na mesma tela
- **Jogo complexo** (múltiplas categorias de pontuação, ex: 7 Wonders com pontos de civil, militar, ciência, ouro, etc.): **uma tela por jogador**, navegável em sequência (ex: swipe ou "Próximo jogador"), mas todas as telas preenchidas em seguida no mesmo aparelho, sem enviar/receber de outro dispositivo
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

## 8. Pontos de Atenção Técnica

- **IDs**: gerar todos os IDs locais (Player, GameSession) como UUID, não sequenciais — evita conflitos quando a sincronização em nuvem (V2) for implementada
- **Auditoria**: todas as entidades possuem `createdAt`, `updatedAt`, `deletedAt` (soft delete). Nomes de campo/coluna sempre em inglês, `camelCase` no domínio Kotlin e `snake_case` nas colunas Room/SQLite. `updatedAt` também serve como base pra resolução de conflitos na sincronização do V2
- **Analytics**: separar métricas universais (contagem, duração, win/loss — funcionam pra qualquer jogo) de métricas específicas de schema (só existem em V2, quando houver pontuação customizada por jogo)
- **Feature flags**: não hardcodar limites de uso (ex: nº de jogos na estante) diretamente no código, já que o modelo de monetização ainda está em aberto
- **Privacidade de vínculo**: vínculo Player local → User real (V2) deve exigir confirmação do usuário vinculado, nunca ser automático
- **QR Code**: geração via biblioteca tipo ZXing (`com.google.zxing`), encapsulando apenas o `@username` (ou um token de convite de curta duração, mais seguro que expor o username bruto) — leitura via CameraX + ZXing. O QR nunca deve efetivar o vínculo direto; sempre passa pelo fluxo padrão de convite/confirmação
- **@username**: precisa de validação de unicidade no backend (V2), normalização (lowercase, sem espaços/acentos) e checagem de disponibilidade em tempo real na tela de criação de conta
- **UserLibraryEntry auto-criada**: ao salvar uma `GameSession`, se o jogo ainda não tiver uma `UserLibraryEntry` pra aquele usuário, uma é criada automaticamente com `status = DONT_HAVE` e `played = true`; se já existir, apenas atualiza `played = true`. Isso garante que rating e o filtro "Jogados" funcionem pra qualquer jogo, independente de estar na estante
- **Imagem de jogo criado pelo usuário**: salva em armazenamento interno do app (ex: `context.filesDir`), não na galeria pública — `coverImageUrl` guarda o path local. Precisa de rotina de limpeza (evitar arquivos órfãos se o jogo for excluído) e de compressão/resize antes de salvar, já que capas em alta resolução acumulam espaço rápido em um catálogo que cresce por múltiplos usuários criando jogos
- **Sync de jogos `USER_CREATED`** (V2): ao logar/sincronizar, o app varre jogos locais com `syncedAt = null`, faz upload da imagem pro storage em nuvem, envia os dados ao backend e atualiza `coverImageUrl` (URL remota) + `syncedAt`. Precisa de estratégia de retry caso o upload falhe (rede instável) sem duplicar o registro

---

## 9. Itens em Aberto (decidir antes da V2)

- Modelo de monetização definitivo
- Backend de autenticação: Firebase Auth vs. backend próprio (C#/EF Core)
- Regras específicas de LGPD para dados de partidas compartilhadas entre jogadores
- Confirmação com a Ludopedia (`api@ludopedia.com.br`) sobre termos de uso comercial da API, antes de arquitetar a integração da seção 5.2
