# ScoreQuest — Contexto do Produto (Snapshot Atual)

> Este documento descreve o app ScoreQuest do ponto de vista de produto: o que ele **já faz** hoje, como as telas e o modelo de dados estão organizados atualmente. Deve ser atualizado sempre que uma funcionalidade nova for implementada ou uma correção mudar o comportamento existente.
>
> - Decisões técnicas de engenharia (bugs já corrigidos, versões de bibliotecas, convenções de código, fluxo de build) continuam em `CLAUDE.md`.
> - Funcionalidades planejadas mas ainda não implementadas ficam em `Score_Quest_Planejamento.md`.
> - `ScoreQuest_Documento_de_Produto.md` é o documento original de visão/roadmap (V1/V2/V3) — mantido como histórico, mas partes dele já foram implementadas de forma diferente do que estava escrito ali; este arquivo reflete a realidade atual.

---

## 1. Visão Geral

App Android pessoal (nativo, Kotlin + Jetpack Compose) para histórico de jogatinas de jogos de tabuleiro: catálogo de jogos, estante pessoal, registro de partidas com pontuação, estatísticas e perfil.

- Persistência **100% local** via Room (`scorequest.db`) como fonte da verdade — **login com Google é opcional** e serve só de backup/sincronização entre aparelhos (ver seção 9), não é necessário pra usar o app.
- Escopo atual = **V1 completo** + o **Criador de Pontuação Personalizado** e a **Conta/sincronização com Firebase** (ambos nominalmente features de V2), implementados por pedido explícito do usuário — o resto do V2 (amigos, @username, feed social) continua no backlog.
- Projeto scaffolded a partir do skill `android-compose-scaffold` (mesma stack/arquitetura do projeto `ShopControl`).

## 2. Identidade Visual (como implementada)

- Tema **escuro como padrão** (alternável para claro em Configurações), paleta dourado/âmbar (`#D4AF37`) sobre fundo quase-preto (`#121212`/`#1E1E1E`) — mesma referência do mockup original.
- **Divergência consciente do doc original**: os gráficos (Home, Perfil, Grupos, detalhe de jogo/jogador) foram implementados como componentes Compose feitos à mão (`HorizontalBarChart`, `VerticalBarChart`, `LineChart`), em vez da biblioteca Vico sugerida — decisão tomada por falta de emulador para validar visualmente uma API em evolução rápida. O heatmap de atividade é `LazyVerticalGrid`, como planejado.
- Cards com borda em gradiente dourado→branco no construtor de pontuação personalizada e nos formulários de pontuação composta.
- Padrão de exibição de jogador reutilizado em várias telas: ícone de pessoa à esquerda do nome, troféu à direita marcando o vencedor (componente `PlayerIdentityRow`).

## 3. Modelo de Dados Atual

Banco Room `scorequest.db`, atualmente na **versão 3** (migrações reais aplicadas, nunca destrutivas — o app já tem dado real do usuário).

Convenção mantida do doc original: toda entidade tem `createdAt`/`updatedAt`/`deletedAt` (soft delete via `deletedAt`), colunas Room em `snake_case`, propriedades Kotlin em `camelCase`.

Entidades ativas:

- **BoardGameEntity** — catálogo de jogos.
- **UserLibraryEntryEntity** (PK = `gameId`, um único usuário local) — status Tenho/Quero/Não tenho, `played`, empréstimo, avaliação por estrelas.
- **PlayerEntity** — jogadores locais, com `avatarPath` (mesma coluna `avatar_color` do início, reaproveitada — não é mais só cor, guarda caminho de imagem ou cor de fallback).
- **GameSessionEntity** — sessões de partida. `participantIds` **não é coluna**, é derivado das linhas de `ScoreEntry`. Ganhou `group_id` (nullable, sem FK — ver seção de Grupos).
- **ScoreEntryEntity** (PK composta `session_id` + `player_id`) — pontuação lançada por jogador por partida.
- **UserProfileEntity** (linha única, id fixo `"local"`) — perfil do usuário.
- **FavoriteGameEntity** (máx. 3 — `SetFavoriteGameUseCase`) — jogos favoritos do perfil.
- **GameScoreSchemaEntity** (`gameId` como PK direta — garante um schema por jogo via constraint de banco) — schema de pontuação personalizada.
- **ActiveTimerEntity** (linha única, PK fixa `"active"`) — cronômetro de partida ao vivo em andamento.
- **PlayerGroupEntity** / **PlayerGroupMemberEntity** (tabela de junção, PK composta `group_id`+`player_id`, com `@Junction`) — grupos de jogadores.

`UserProfileEntity` e `FavoriteGameEntity` não estavam na seção 4.2 do doc original — foram adicionadas como extensão natural para as telas de Perfil.

## 4. Navegação Atual

Bottom bar com **5 abas fixas**, nesta ordem: **Extras, Jogos, Home, Jogadores, Perfil**. A ordem é escolha deliberada do usuário (reordenada mais de uma vez); o item **Home fica visualmente destacado** (círculo dourado sólido + label em negrito).

FAB dourado sobreposto abre o wizard de registro de partida — visível junto com a bottom bar, **exceto em Home e Extras**.

**Configurações deixou de ser aba** — agora é uma tela acessada pelo ícone de engrenagem na TopAppBar do Perfil. "Gerenciar jogadores locais" também saiu de Configurações e virou a própria aba **Jogadores**.

## 5. Telas e Funcionalidades Implementadas

### 5.1 Home
Saudação, sino, ícone de engrenagem (abre configuração de visibilidade dos widgets), banner de partida em andamento (se houver cronômetro ativo), botões "Registrar partida" e "Iniciar partida ao vivo", card de estatísticas (partidas, semana atual, horas totais, streak), card "Última jogatina", e 5 widgets ocultáveis individualmente:
- Atividade recente (heatmap, 90 dias)
- Ranking dos mais jogados (top 5)
- Mais vitórias (top 5 jogadores)
- Partidas por mês (linha, ano corrente, mínimo 3 meses com dado)
- Duração das partidas (histograma, só aparece com 5+ partidas)

### 5.2 Jogos (Acervo + Estante)
Grade de 2 colunas, cards quadrados. 3 sub-abas: **Estante** (Tenho), **Desejo** (Quero), **Jogado** (`played = true`) — sem aba "Todos". Busca/ordenação/filtro de categoria escondidos atrás de um ícone de lupa. Ordenação: A-Z, Z-A, Recente, Recente reverso.

### 5.3 Detalhe do jogo
Header com imagem de fundo, nome + estrelas, ícones de jogadores/tempo/peso, dropdown de status (Tenho/Quero/Não tenho) com gradiente dourado, empréstimo (se Tenho), estatísticas (vezes jogadas, horas totais, tempo médio) + estatísticas ricas (recordes, mais partidas jogadas, mais vitórias, maiores pontuações), botões "Iniciar partida ao vivo" e "Configurar/Editar pontuação personalizada", histórico de partidas, FAB de registro pré-selecionando o jogo.

### 5.4 Adicionar/Editar jogo
Mesma tela para criar e editar. Capa via Câmera, Galeria ou URL direta (as três escrevem no mesmo campo).

### 5.5 Wizard de registro de partida
5 etapas: Escolher jogo → Dados da sessão → Jogadores (com atalho de seleção por Grupo) → Pontuação (ramifica conforme o schema do jogo: grid simples, uma tela por jogador para Composta, ou tela única arrastável para Ranking) → Confirmação. Suporta edição e pré-seleção de jogo.

### 5.6 Cronômetro de partida ao vivo
Fluxo separado do wizard retrospectivo — inicia um cronômetro real ao começar a jogar (Suspender/Retomar/Cancelar/Finalizar), com banner de retomada na Home. Ao finalizar, entrega a duração já calculada pro wizard de pontuação. Só uma partida cronometrada por vez; sem notificação persistente do Android.

### 5.7 Detalhe de partida
Mesmo padrão visual da Confirmação do wizard — resumo da partida + placar, com nome do grupo (se houver).

### 5.8 Perfil
Header com avatar, nome e (se logado com Google) `@username` abaixo do nome em dourado, favoritos (editável, máx. 3), aba Atividades com lista paginada de todas as partidas.

### 5.9 Configurações
Seção de conta (**Entrar com Google** / nome+e-mail da conta + **Sair**, ver seção 9), toggle de tema claro/escuro, **Importar JSON** e **Exportar JSON** do acervo de jogos (ver seção 6).

### 5.10 Jogadores, Grupos e Amigos
Aba "Jogadores" com 3 sub-abas: **Jogadores** (grid, criar/editar/excluir, detalhe com estatísticas individuais), **Grupos** (conjuntos nomeados de 2+ jogadores, com estatísticas próprias filtradas pelas partidas daquele grupo) e **Amigos** (só com conta logada): buscar usuário por `@username` e enviar solicitação, aceitar/recusar solicitações recebidas, listar amigos. Ao registrar uma partida, o app reconcilia automaticamente a seleção de jogadores com grupos existentes (adota, sugere atualizar ou criar grupo novo).

Amigos entram no fluxo de partida **como um Player local**: a aba Amigos tem um botão "Adicionar como jogador" por amigo (cria/reaproveita um `Player` vinculado àquela conta), que passa a aparecer normalmente entre os jogadores do wizard de registro de partida — nenhuma tela do wizard precisou mudar. Em "Editar jogador", um `Player` ainda não vinculado ganha o botão "Vincular a um amigo" — útil pra quem já tinha um jogador cadastrado com histórico de partidas e a pessoa correspondente criou conta depois (o histórico continua todo ligado ao mesmo jogador, só passa a apontar pra conta real).

### 5.11 Extras / Ferramentas
Aba com 12 mini-ferramentas independentes de registro de partida: Moeda, Número aleatório, Letra aleatória, Sorteio por nome, Ordem de turno, Sorteio de equipes, Sorteio de papéis, Dados (d4-d20), Roleta customizável, Placar avulso, Cronômetro por turno, Dedo na tela (multi-toque). Nenhuma persiste dado — resetam ao sair da tela.

### 5.12 Pontuação Personalizada
Construtor visual (`GameScoreSchema` por jogo, um schema por jogo): tipo Simples ou Composta (ou "Duplicar de outro jogo"); campos configuráveis (Número, Sim/Não, Escolha única, Múltipla escolha, Texto); montagem do vencedor (Manual, Automático via fórmula visual com pesos, ou Sem vencedor/cooperativo, com diálogo de empate); modo de teste com dados fictícios antes de salvar. Terceiro tipo além do previsto no doc original: **Ranking** (jogadores ordenados por posição, pontos por jogador opcionais).

## 6. Fonte do Acervo Atual

O catálogo é populado hoje via **Importar/Exportar JSON** (não mais um seed pré-carregado hardcoded de 100-300 jogos como o doc original previa). Arquivo de referência: `Regras/Jogos iniciais.json` (20 jogos + 20 schemas de pontuação, um por jogo, cobrindo os principais padrões — SIMPLE, COMPOSITE/AUTOMATIC, COMPOSITE/MANUAL, COMPOSITE/NONE). Formato documentado em `Regras/Como montar o JSON de jogos.md`. Importar é idempotente (upsert por id).

## 7. Conta e sincronização com Firebase

Login com Google **opcional** (via Configurações) + backup/sincronização dos dados locais na nuvem (Firebase Auth + Firestore). O app continua funcionando 100% offline sem conta, exatamente como antes — conta é só um jeito de não perder/poder restaurar os dados em outro aparelho.

- **Entrar**: Configurações → "Entrar com Google" → tela de login dedicada (Credential Manager). Ao logar com sucesso, os dados pessoais já presentes localmente no aparelho (jogadores, partidas, grupos, perfil, estante) são automaticamente associados àquele usuário — se o aparelho já tinha jogador cadastrado, eles sobem pra nuvem; se estava vazio (ex.: depois de um logout), o backup existente da nuvem é baixado.
- **Catálogo de jogos é compartilhado entre todos os usuários** (jogos + pontuação personalizada), não pertence a uma conta — sincronizado nos dois sentidos a cada login. Sua **estante** (status Tenho/Quero/Jogado, empréstimo, avaliação) continua privada, só referenciando o jogo do catálogo compartilhado.
- **Sair**: Configurações → "Sair" — desloga **e apaga todos os dados locais do aparelho** (catálogo, estante, jogadores, partidas, pontuações personalizadas — inclusive o catálogo, que é baixado de novo no próximo login de qualquer conta), pra não misturar com o próximo usuário/conta que usar o mesmo aparelho.
- Escopo: um usuário só tem uma "cópia na nuvem" de cada vez (sem múltiplos dispositivos editando a mesma conta simultaneamente, sem resolução de conflito).
- **Amigos (solicitação/aceite) já implementado** — ver seção 5.10 e `CLAUDE.md` ("Amigos"). Sem feed de atividades social, e registrar partida com um amigo ainda não compartilha a sessão com a conta dele (fica só no seu próprio backup) — ver limitações em `CLAUDE.md`.

## 8. O que ainda não existe

- Onboarding de primeiro uso.
- Compartilhar a sessão/partida em si com a conta do amigo (hoje amigo = Player local vinculado, mas a partida registrada não aparece automaticamente pro outro lado).
- Feed de atividades social, busca de amigo por QR Code.
- Catálogo curado ainda não chegou na meta de 100-300 jogos (hoje são 20).

Backlog completo em `Score_Quest_Planejamento.md`.
