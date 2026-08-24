# Como montar o JSON de entrada de jogos

Guia pra escrever/editar arquivos como `Jogos iniciais.json` — o formato lido pelo botão **"Importar JSON"** (Configurações) e escrito pelo botão **"Exportar JSON"**. Reflete exatamente o que o código aceita hoje (`data/seed/SeedGamesFile.kt` + `ImportSeedGamesUseCase.kt`) — não documenta nada que o app ainda não implementa.

---

## 1. Estrutura geral

```json
{
  "schemaVersion": 1,
  "description": "texto livre, só documentação — não é lido pelo app",
  "games": [ /* lista de jogos, seção 2 */ ],
  "scoreSchemas": [ /* lista de pontuações personalizadas, opcional, seção 3 */ ]
}
```

- `schemaVersion` e `description` são **decorativos** — o parser ignora qualquer campo que não reconheça (`ignoreUnknownKeys = true`). Pode e deve usar esses (e outros, como `metadata`/`notes` dentro de cada jogo) pra deixar registrado de onde veio cada dado, sem medo de quebrar o import.
- `games` é obrigatório (pode ser vazio). `scoreSchemas` é opcional — um jogo sem entrada em `scoreSchemas` simplesmente usa a pontuação genérica do V1 (total numérico + vencedor por maior pontuação), que é o mesmo comportamento de um schema `SIMPLE`.

---

## 2. Cada jogo (`games[]`)

```json
{
  "id": "seed-nome-do-jogo",
  "name": "Nome do Jogo",
  "minPlayers": 2,
  "maxPlayers": 4,
  "avgDurationMinutes": 60,
  "coverImageUrl": "https://...",
  "category": "Categoria / Subcategoria",
  "weight": 2.5,
  "source": "CURATED"
}
```

| Campo | Obrigatório | Tipo | Observações |
|---|---|---|---|
| `id` | ✅ | string | **Ver seção 2.1 — a parte mais importante do formato.** |
| `name` | ✅ | string | Nome exibido no app. |
| `minPlayers` / `maxPlayers` | ✅ | int | `minPlayers ≤ maxPlayers`. Jogo só-solo ou só-2 usa o mesmo valor nos dois (ex.: Star Realms `2`/`2`). |
| `avgDurationMinutes` | ✅ | int | Minutos. Se a caixa/Ludopedia informa uma faixa ("30-60 min"), use uma estimativa única (ex.: o meio da faixa). |
| `coverImageUrl` | opcional | string ou `null` | URL pública da capa. No Ludopedia, é a URL da imagem da página do jogo. |
| `category` | opcional | string ou `null` | Texto livre, ex.: `"Jogo de Cartas / Civilização"`. Aparece no filtro de categoria da tela Jogos. |
| `weight` | opcional | double ou `null` | Complexidade, escala 1.0 (leve) a 5.0 (pesado), **estilo BGG**. Se o Ludopedia não listar, deixe `null` — não invente um número. |
| `source` | opcional, padrão `"CURATED"` | string | Um dos valores de `GameSource`: `CURATED`, `USER_CREATED`, `LUDOPEDIA_IMPORT`, `BGG_IMPORT`. Pra jogos de um arquivo de seed, sempre `CURATED`. |

### 2.1 `id` — a chave que decide adicionar vs. atualizar

- Use um **slug estável e legível**, não um UUID: `"seed-nome-do-jogo"`. Vira a chave primária real no banco (o app não gera outro ID por baixo).
- **Reimportar o mesmo arquivo é seguro**: se o `id` já existe no app, os dados são **atualizados**; se não existe, um jogo **novo** é criado. É assim que dá pra ir editando este arquivo aos poucos (corrigir um peso, trocar uma capa) e reimportar sem duplicar nada.
- **Nunca reutilize um `id` pra dois jogos diferentes** — o segundo vai sobrescrever o primeiro.
- Convenção usada até agora: `seed-` + nome em minúsculas, sem acento, espaços viram `-` (ex.: `seed-heat-pedal-to-the-metal`).

### 2.2 O que acontece ao importar um jogo novo

Todo jogo **inserido pela primeira vez** entra automaticamente na estante do usuário com status **"Quero"** (`LibraryStatus.WANT`) — sem isso ele ficaria invisível na tela Jogos (não existe aba "Todos", só Estante/Desejo/Jogado, todas filtradas por status). Jogos que já existiam (update) não têm o status mexido — a escolha do usuário é preservada.

---

## 3. Pontuação personalizada (`scoreSchemas[]`)

Cada entrada referencia um jogo pelo `gameId` (tem que bater com um `id` da lista `games`, do mesmo arquivo ou já existente no app). **Mesma regra de upsert do `id`**: já existe schema pra aquele `gameId`? atualiza. Não existe? cria.

```json
{
  "gameId": "seed-nome-do-jogo",
  "type": "SIMPLE",
  "winnerMode": "AUTOMATIC",
  "fields": [ /* seção 3.2 — só relevante se type = COMPOSITE */ ],
  "formula": { /* seção 3.3 — só relevante se winnerMode = AUTOMATIC */ }
}
```

### 3.1 `type` — Simples, Composta ou Ranking

| Valor | Quando usar | O que mais precisa preencher |
|---|---|---|
| `SIMPLE` | Jogo onde "quem fez mais pontos" já é a pontuação inteira — um número só por jogador. | Nada além de `type`/`winnerMode` — `fields` fica vazio (`[]`), `formula` fica de fora. Na prática, **é opcional até incluir esse jogo em `scoreSchemas`**: sem nenhuma entrada, o app já trata como Simples por padrão. |
| `COMPOSITE` | Pontuação tem mais de uma categoria/fonte de pontos que precisa ser somada, ou o vencedor não é "quem tem o maior número". | `fields` (seção 3.2) e, se `winnerMode = AUTOMATIC`, `formula` (seção 3.3). |
| `RANKING` | Não existe "pontuação" no sentido tradicional — o que importa é a posição de cada jogador (1º ao Nº), decidida arrastando na hora de lançar a partida. Vencedor é **sempre** quem fica em 1º. | Ver seção 3.1.1 — `fields` vazio (`[]`) ou com **um único** campo `NUMBER` de pontos opcional; `formula` sempre de fora; `winnerMode` é ignorado pelo app (use `"AUTOMATIC"` por convenção, mesmo valor usado em `SIMPLE`). |

### 3.1.1 Particularidades do `RANKING`

```json
{
  "gameId": "seed-nome-do-jogo",
  "type": "RANKING",
  "winnerMode": "AUTOMATIC",
  "fields": []
}
```

- **Sem pontos** (só a posição importa): `fields: []`, exatamente como no exemplo acima.
- **Com pontos opcionais** (digitados por jogador na hora de cada partida, não uma tabela fixa por posição): `fields` recebe **exatamente um** campo `NUMBER` com `key` igual a `"points"`:
  ```json
  "fields": [
    { "type": "NUMBER", "key": "points", "label": "Pontos", "default": 0, "allowNegative": false }
  ]
  ```
  `key` **precisa ser literalmente `"points"`** (é a constante `RANKING_POINTS_FIELD_KEY` no código) — qualquer outro valor não é reconhecido como o campo de pontos do Ranking.
- Isso é tudo que o formato de importação cobre. O **resultado** de cada partida (a posição de cada jogador, e os pontos se ativados) não faz parte deste arquivo — ele é gravado por sessão, não por schema, quando alguém realmente joga.

### `winnerMode` — como o vencedor é decidido (só importa se `type = COMPOSITE`)

| Valor | Comportamento no app | Quando usar |
|---|---|---|
| `MANUAL` | Ao final da partida, o app pergunta "Quem venceu?" e a pessoa escolhe. | Jogos onde o resultado depende de julgamento (melhor construção, votação) e não dá pra reduzir a uma fórmula. |
| `AUTOMATIC` | O app calcula o total de cada jogador pela `formula` (seção 3.3) e decide o vencedor sozinho — com um diálogo de empate se necessário. | Sempre que a pontuação final for (ou puder ser reduzida a) uma soma ponderada de valores. |
| `NONE` | Ninguém é marcado como vencedor — os campos continuam sendo preenchidos por jogador, só não geram ranking. | Jogos cooperativos/sem "quem ganhou" individual. *(Hoje o app só deixa de perguntar vencedor — ainda não tem uma etapa separada de "resultado da partida" vitória/derrota do grupo; isso é uma extensão futura, não modelada neste JSON.)* |

### 3.2 `fields[]` — pra `type: "COMPOSITE"` (pra `RANKING`, ver a exceção específica na seção 3.1.1)

Cada campo é preenchido **por jogador**, na tela de lançamento da partida. Cinco tipos:

```json
{ "type": "NUMBER", "key": "pontos_construcao", "label": "Pontos de Construção", "default": 0, "min": null, "max": null, "allowNegative": false }
{ "type": "BOOLEAN", "key": "objetivo_cumprido", "label": "Objetivo cumprido", "pointsIfChecked": 5 }
{ "type": "ENUM", "key": "faccao", "label": "Facção", "options": [{ "label": "Vermelha", "points": 0 }, { "label": "Azul", "points": 0 }] }
{ "type": "MULTI_SELECT", "key": "conquistas", "label": "Conquistas", "options": [{ "label": "Primeira ação", "points": 2 }, { "label": "Combo triplo", "points": 5 }] }
{ "type": "TEXT", "key": "observacoes", "label": "Observações" }
```

| Tipo | Campos próprios | Pontua? | Uso típico |
|---|---|---|---|
| `NUMBER` | `default` (valor inicial, padrão 0), `min`/`max` (opcionais, sem limite se `null`), `allowNegative` | Sim — o número digitado entra direto na fórmula | Contadores, moedas, pontos de vitória |
| `BOOLEAN` | `pointsIfChecked` (pontos se marcado; **`null`/omitido = só anotação, não pontua**) | Só se `pointsIfChecked` estiver definido | Objetivo cumprido, condição binária |
| `ENUM` | `options: [{label, points}]` — escolha **única** | Pontos da opção escolhida | Facção, resultado, categoria exclusiva |
| `MULTI_SELECT` | `options: [{label, points}]` — **várias** escolhas possíveis | Soma dos pontos de todas as opções marcadas | Conquistas, bônus acumuláveis |
| `TEXT` | — | Nunca pontua | Observações, anotações livres |

`key` é o identificador interno do campo — usado pra referenciar o campo na `formula` (seção 3.3). Convenção: minúsculo, sem acento, `snake_case` (ex.: `estruturas_civis`). Precisa ser único dentro do mesmo schema.

### 3.3 `formula` — só pra `winnerMode: "AUTOMATIC"`

```json
{
  "terms": [
    { "fieldKey": "pontos_construcao", "weight": 1.0 },
    { "fieldKey": "penalidades", "weight": -1.0 },
    { "fieldKey": "bonus_final", "weight": 2.0 }
  ],
  "comparisonRule": "HIGHEST_WINS"
}
```

- **Total do jogador = soma de `(valor do campo × weight do termo)`**, pra todo termo listado. Campo que não aparece em `terms` simplesmente não entra na conta.
- `weight` pode ser negativo (cobre subtração — ex. `-1.0` pra penalidades) ou diferente de `1.0` (multiplicador). Na maioria dos casos fica `1.0` — o campo entra "como está".
- Campos `TEXT` nunca podem entrar em `terms` (não têm valor numérico). Campos `BOOLEAN` sem `pointsIfChecked` também não devem entrar (não pontuam).
- `comparisonRule`: `"HIGHEST_WINS"` (maior total vence) ou `"LOWEST_WINS"` (menor total vence — útil pra jogos tipo golfe, onde menos é melhor).

---

## 4. Jogos complexos — quando a fórmula linear não é suficiente

**A fórmula do app só sabe fazer soma ponderada** (`Σ valor × peso`) — nada de divisão, potência, tabelas de conversão não-lineares ou condicionais. Muitos jogos têm pelo menos uma categoria de pontuação que não é uma soma direta. Antes de modelar um jogo composto, pra cada categoria de pontuação pergunte:

> **"Dá pra escrever essa categoria como `valor_bruto × uma_constante`?"**

- **Se sim** → campo `NUMBER` cru (ex.: "moedas restantes", peso `1.0` ou `2.0`) ou `ENUM`/`MULTI_SELECT` com pontos por opção (o app já embute a tabela de conversão nas `options`, então isso continua "linear" do ponto de vista da fórmula).
- **Se não** (divisão inteira, exponenciação, bônus por combinação de várias outras categorias, tabela de conversão condicional) → **peça o subtotal já calculado** nessa categoria, num campo `NUMBER` só pra ela. A pessoa faz a conta na cabeça (ou olha a caderneta de pontuação oficial do jogo, se ele tiver uma) e digita o resultado — exatamente como fizemos pra "Tesouro" e "Estruturas Científicas" do 7 Wonders (ver `Jogos iniciais.json`): a regra oficial é `moedas ÷ 3` e `símbolos²+bônus`, nenhuma das duas é `valor × peso`, então cada uma virou um campo `NUMBER` que recebe o total já pronto, e a fórmula do schema só soma os 7 subtotais com peso `1.0` cada — literalmente reproduzindo a caderneta de pontuação impressa que vem na caixa do jogo.

Esse padrão ("um campo por categoria, subtotal pré-calculado, fórmula = soma simples de todos") funciona pra praticamente qualquer jogo com pontuação por categorias, mesmo quando alguma categoria interna é complicada — é a saída padrão sempre que a conta de uma categoria específica não for linear.

**Quando nem isso resolve**: se o vencedor depende de julgamento humano (não dá pra reduzir a número nenhum, tipo "melhor construção" ou "resultado de uma votação"), use `winnerMode: "MANUAL"` em vez de tentar forçar uma fórmula — o app pergunta quem venceu ao final, sem cálculo nenhum.

**Documentando a decisão**: sempre que um campo pedir "subtotal já calculado" em vez de valor bruto, deixe isso registrado no rótulo (`label`) do campo — ex. `"Tesouro (Moedas ÷ 3)"` em vez de só `"Tesouro"` — e, se o schema tiver um bloco de anotações livre (como o `notes` no exemplo do 7 Wonders), explique a regra original e por que ela não coube na fórmula. Isso evita que alguém, meses depois, tente "consertar" o campo pra receber o valor bruto sem perceber que ia quebrar a soma.

---

## 5. Exemplo completo (7 Wonders — ver `Jogos iniciais.json`)

```json
{
  "id": "seed-7-wonders",
  "name": "7 Wonders",
  "minPlayers": 2,
  "maxPlayers": 7,
  "avgDurationMinutes": 30,
  "coverImageUrl": "https://storage.googleapis.com/ludopedia-capas/8_t.jpg",
  "category": "Jogo de Cartas / Civilização",
  "weight": 2.3,
  "source": "CURATED"
}
```

```json
{
  "gameId": "seed-7-wonders",
  "type": "COMPOSITE",
  "winnerMode": "AUTOMATIC",
  "fields": [
    { "type": "NUMBER", "key": "conflitos_militares", "label": "Conflitos Militares", "default": 0, "allowNegative": true },
    { "type": "NUMBER", "key": "tesouro", "label": "Tesouro (Moedas ÷ 3)", "default": 0, "allowNegative": false },
    { "type": "NUMBER", "key": "maravilha", "label": "Maravilha", "default": 0, "allowNegative": false },
    { "type": "NUMBER", "key": "estruturas_civis", "label": "Estruturas Civis", "default": 0, "allowNegative": false },
    { "type": "NUMBER", "key": "estruturas_cientificas", "label": "Estruturas Científicas", "default": 0, "allowNegative": false },
    { "type": "NUMBER", "key": "estruturas_comerciais", "label": "Estruturas Comerciais", "default": 0, "allowNegative": false },
    { "type": "NUMBER", "key": "guildas", "label": "Guildas", "default": 0, "allowNegative": false }
  ],
  "formula": {
    "terms": [
      { "fieldKey": "conflitos_militares", "weight": 1.0 },
      { "fieldKey": "tesouro", "weight": 1.0 },
      { "fieldKey": "maravilha", "weight": 1.0 },
      { "fieldKey": "estruturas_civis", "weight": 1.0 },
      { "fieldKey": "estruturas_cientificas", "weight": 1.0 },
      { "fieldKey": "estruturas_comerciais", "weight": 1.0 },
      { "fieldKey": "guildas", "weight": 1.0 }
    ],
    "comparisonRule": "HIGHEST_WINS"
  }
}
```

Note que `conflitos_militares` é o único campo com `allowNegative: true` (fichas de derrota descontam pontos) — os demais nunca ficam negativos nesse jogo específico.

---

## 6. Checklist antes de importar

- [ ] JSON válido (sem vírgula sobrando, aspas fechadas etc. — um validador online resolve em segundos)
- [ ] Todo `id` em `games` é único dentro do arquivo
- [ ] Todo `gameId` em `scoreSchemas` corresponde a um `id` existente (no mesmo arquivo ou já importado antes)
- [ ] `minPlayers ≤ maxPlayers`
- [ ] Todo `fieldKey` usado em `formula.terms` existe em `fields` do mesmo schema
- [ ] Nenhum campo `TEXT` (ou `BOOLEAN` sem `pointsIfChecked`) aparece em `formula.terms`
- [ ] Se algum campo pede "subtotal já calculado" em vez de valor bruto, isso está no `label` e/ou documentado em algum bloco de notas
- [ ] Schema `RANKING` tem `fields` vazio (`[]`) ou com exatamente um campo `NUMBER` de `key = "points"` — nunca mais de um campo, nunca outra `key`
