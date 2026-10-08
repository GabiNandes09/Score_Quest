# ScoreQuest — Planejamento (Backlog)

> Este documento guarda o backlog: features já pensadas/refinadas mas **ainda não implementadas**. Assim que algo daqui for implementado, remova o item deste arquivo e atualize `Score_Quest_Context.md` para refletir a nova realidade — este arquivo deve ficar sempre só com coisas futuras.

---

## V1 — Itens ainda pendentes

- **Onboarding de primeiro uso**: boas-vindas (1-2 telas explicando o app), sugestão de adicionar os primeiros jogos à estante, convite para registrar a primeira partida.
- **Expandir o catálogo curado**: hoje só 20 jogos via `Regras/Jogos iniciais.json`; a meta original era 100-300 jogos mais populares.
- **Campo de pontuação com escopo `PER_SESSION`** para jogos cooperativos (ex.: "dificuldade escolhida", "cartas restantes no baralho"): hoje contornado gravando o mesmo valor em um campo comum a todos os jogadores; a modelagem própria de campo único por partida não foi implementada.

## V2 — Contas & Nuvem

- **Login com Google + backup/sync na nuvem (Firebase Auth + Firestore) já implementado**, cherry-picked pra dentro do V1 por pedido do usuário — ver CLAUDE.md ("Conta e sincronização com Firebase") e Score_Quest_Context.md (seção 7). Decide a pergunta "Firebase Auth vs. backend próprio" que estava em aberto abaixo: **Firebase**, não backend próprio.
- **Catálogo de jogos (jogos + pontuação personalizada) virou compartilhado entre todos os usuários** (pedido do usuário, também já implementado) — deixou de ser por-conta. Estante (`UserLibraryEntry`), jogadores, grupos, partidas e perfil continuam privados por conta.
- Pendente ainda dentro do que já foi iniciado: publicar `firestore.rules` no Firebase Console (arquivo já existe na raiz do repo, só falta o deploy manual); sync do dado pessoal é só full push/pull determinado por "tem jogador local ou não" — **sem merge bidirecional real** (dois aparelhos editando a mesma conta ao mesmo tempo não é tratado); login só por Google (sem e-mail/senha); catálogo compartilhado sem moderação/deduplicação (ver item próprio mais abaixo).
- **Modelo de conexões/amigos + compartilhamento de sessões e grupos entre contas: desenhado, não implementado** — rascunho completo em CLAUDE.md ("Modelo de conexões/amigos"), pedido explícito do usuário mas decidido por ele mesmo ficar só no papel por enquanto. Cobre boa parte do que os 3 itens abaixo (@username, Amigos, vínculo Player→User) pedem — ao implementar, usar esse rascunho como ponto de partida em vez de desenhar do zero.
- Vínculo Player local → User real, com convite/confirmação (perfis de jogadores continuam sendo só locais, não contas).
- **@username: geração automática + exibição no Perfil já implementadas** (primeiro nome + contador, `_2`/`_3` pra duplicados, único via transação Firestore — ver CLAUDE.md "Diretório público de usuários + @username automático"). **Ainda falta**: pesquisável (Firestore não tem `LIKE`, precisa de `orderBy` + range de prefixo pra autocomplete) e um fluxo de usuário **escolher** o próprio @username (hoje é só gerado, não editável).
- Amigos: busca por @username ou QR Code (gerar/ler), modelo estritamente de **amigos mútuos** (sem "seguir" assimétrico).
- Feed social de atividades com curtidas e comentários; quando o social amadurecer, a Home passa a ser centrada nele, com as estatísticas pessoais migrando pra aba Perfil.
- Notificações (ex.: "faz tempo que não joga X").
- Compartilhamento de resultado de partida como imagem para redes sociais.
- Política de privacidade / LGPD (obrigatória com conta de usuário).
- Sincronização dos jogos `USER_CREATED`: upload de imagem pra storage em nuvem, `syncedAt` preenchido, com retry em caso de falha de rede.
- **Deduplicação/moderação de jogos `USER_CREATED` duplicados ganhou urgência real** (curadoria promove os populares a `CURATED`, fundindo duplicatas) — agora que o catálogo é compartilhado entre contas (ver acima), dois usuários adicionando "Catan" à mão por fora do catálogo de seed geram **dois registros duplicados de verdade na nuvem** (cada um com um UUID aleatório diferente, sem nenhuma reconciliação hoje) — antes isso só sujava o banco de uma pessoa, agora suja o catálogo de todo mundo.
- **Integração com a API da Ludopedia** (`https://ludopedia.com.br/api/v1/`, doc oficial em `ludopedia.com.br/api/documentacao.html`, estágio ALPHA) — pesquisada em 14/09/2026, resumo técnico:
  - **Acesso**: self-service, sem precisar esperar aprovação por e-mail — criar o app em `ludopedia.com.br/aplicativo` (nome, ícone, redirect URI) gera `app_id` + `app_key`. Exige aceitar os Termos de Uso na hora de criar.
  - **Termos de uso confirmados (resolve o item que estava em aberto)**: acesso é **gratuito só para uso NÃO COMERCIAL**, com atribuição obrigatória à Ludopedia como fonte dos dados + link para o site nas páginas onde os dados aparecem. Proibido usar os dados pra criar um "clone" do site. Ludopedia pode bloquear o acesso a qualquer momento. Em caso de dúvida se o projeto se enquadra, contato `api@ludopedia.com.br`. **Isso trava a integração enquanto o modelo de monetização do ScoreQuest não estiver definido como não-comercial** (ver item de monetização abaixo) — se o app for monetizado, precisa negociar diretamente com a Ludopedia antes de integrar.
  - **Auth**: OAuth2 Authorization Code. Autorizar em `GET https://ludopedia.com.br/oauth?app_id=...&redirect_uri=...` (usuário aprova/nega) → retorno com `code` (ou `error`/`error_description`) na `redirect_uri` cadastrada → trocar por token em `POST https://ludopedia.com.br/tokenrequest` (`code`) → recebe `access_token` em JSON. Todas as chamadas usam header `Authorization: Bearer {access_token}`. Scopes declarados: `usuarios:read`/`usuarios:write`.
  - **Endpoints relevantes pro ScoreQuest**:
    - `GET /jogos` (busca por nome, paginação até 100/página) e `GET /jogos/{id_jogo}` (ficha completa: min/max jogadores, duração, idade mínima, ano, mecânicas/categorias/temas/designers/artistas, thumbnail) — cobre a expansão do catálogo (`BoardGame.source = LUDOPEDIA_IMPORT`).
    - `GET/POST /colecao` — lê e **escreve** a coleção do usuário na Ludopedia (flags `fl_tem`/`fl_quer`/`fl_teve`/`fl_jogou`, nota `vl_nota`, comentário, tags) → mapeia quase 1:1 pro `UserLibraryEntry` do ScoreQuest (status Tenho/Quero/Não tenho + avaliação). Abre a possibilidade de sync **bidirecional** de estante, não só import de catálogo.
    - `GET/POST /partidas` — **também escreve partidas**: objeto `Partida` (jogo, expansões, duração em minutos, data, descrição, array `jogadores` com `id_usuario` OU `nome` avulso, `fl_vencedor`, `vl_pontos` de -999 a 999) mapeia muito de perto pro par `GameSession`+`ScoreEntry` do ScoreQuest — inclusive jogador sem conta (`nome`) é equivalente ao `Player` local. Isso significa que dá pra cogitar sincronizar **o histórico de partidas** com a Ludopedia, não só o acervo (ideia nova, fora do escopo original do doc de produto — avaliar se faz sentido).
    - `GET /me` (dados do usuário logado, pra obter `id_usuario` — necessário porque o array `jogadores` do POST de partida exige que o usuário logado esteja entre os participantes).
  - **Limitações observadas**: sem rate limit documentado; paginação máxima de 100 registros por página; `/jogos/{id}/notas` e `/videos` estão marcados como "não implementado" na doc oficial; API segue em estágio Alpha (pode mudar sem aviso).
  - Continua exigindo backend próprio pro fluxo OAuth2 (não é seguro 100% client-side manter `app_key`).

## V3 — Avançado

- Construtor de fórmula avançado — hoje não suporta termos com dois campos multiplicados entre si (ex.: campo A × campo B, ambos variáveis por partida); não surgiu caso de uso claro ainda.
- Merge de jogadores duplicados.
- Conquistas / gamificação (badges hexagonais).
- Sugestões de pessoas para adicionar (com base em amigos mútuos / jogos em comum).
- Import do catálogo do **BoardGameGeek** — alternativa/complemento à Ludopedia, base muito maior (100mil+ jogos), requer licença comercial paga se o app for monetizado.
- Modo torneio / campeonato.

## Pontuação Personalizada — Itens em Aberto

- **Versionamento do schema**: hoje a edição sobrescreve o registro existente, sem histórico de versões anteriores. Reavaliar se isso causar inconsistência de estatísticas entre partidas antigas e novas.
- **Critério de "usuário permitido"** para editar pontuação personalizada quando o app for distribuído publicamente (papel fixo tipo curador/moderador, allowlist manual, ou outro modelo) — a definir antes da primeira distribuição pública. Hoje qualquer usuário pode editar, sem restrição.
- **Escopo Global vs. Pessoal do schema** — alternativa descartada por ora (permitiria cada usuário ter sua própria variação sem mexer na de terceiros); fica como candidata caso o modelo de "usuário permitido" da fase distribuída se mostre insuficiente.

## Itens em Aberto Gerais

- **Modelo de monetização definitivo** — decisão ganhou urgência: os termos de uso da API da Ludopedia só permitem uso não-comercial (ver seção Ludopedia em V2), então essa escolha também define se a integração com a Ludopedia é viável como está ou se precisa de negociação direta com eles.
- Regras específicas de LGPD para dados de partidas compartilhadas entre jogadores (fica mais urgente agora que existe conta de usuário de verdade, mesmo que só com Firebase Auth + backup).
