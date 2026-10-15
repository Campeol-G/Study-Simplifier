# contrato/

O contrato de dados que o StudyS desktop (Java + SQLite) e o StudyS web (TypeScript, estatico)
trocam. Os dois conversam por um arquivo JSON, nao por rede.

A partir da Fase 1 do `PROMPT-UNIFIED.md` esta pasta fica **congelada**: mudar qualquer arquivo aqui
exige migracao explicita nos dois lados e um `schemaVersion` novo.

```
plano.schema.json      o plano normalizado, em JSON Schema draft 2020-12
prompts/               prompts de sistema e esquemas de saida, como dados
fixtures/              resposta sintetica de LLM + o plano que ela deve produzir
```

## O que e o plano normalizado

Um plano e um conjunto de nos em arvore. Cada no tem um tipo (`MATERIA`, `CAPITULO`, `TOPICO`,
`AULA`), um status (`TODO`, `IN_PROGRESS`, `DONE`, `SKIPPED`), uma ordem entre os irmaos e, as vezes,
conteudo gerado e recursos externos. `conteudo` e `recursos` sao opcionais por no: o web gera o do
primeiro topico como amostra e o desktop gera o resto.

Os enums espelham o codigo Java, nao o contrario:

| No contrato | No codigo |
|---|---|
| `nos[].tipo` | `com.studys.schemas.plan.PlanNodeType` |
| `nos[].status` | `com.studys.schemas.plan.PlanNodeStatus` |
| `nos[].conteudo.tipo` | `com.studys.schemas.content.ContentType` |
| `nos[].recursos[].tipo` | `com.studys.schemas.content.ResourceType` |

O nivel e a unica coisa do contrato que ainda nao existia em Java: foi criado aqui e o Java passa a
bater contra ele.

## Regra de ids

Ids sao derivados de caminho, nao UUID:

- a raiz tem `id == slug`;
- os demais tem `id == paiId + "/" + slug`.

Exemplos: `java`, `java/fundamentos`, `java/fundamentos/variaveis`. O `slug` segue
`^[a-z0-9]+(-[a-z0-9]+)*$`: minusculas, sem acento, palavras separadas por hifen.

O motivo e idempotencia de importacao. Com UUID aleatorio, importar o mesmo plano duas vezes
duplicaria tudo. Com id de caminho, a segunda importacao encontra os nos existentes e atualiza. O
motor atual ja usa um padrao parecido (`<planId>-materia`, `<planId>-topico-1`); este formato e a
generalizacao dele.

## Versionamento

`schemaVersion` e obrigatorio e hoje vale `2` (`const`, nao `enum`: nao existe versao 3). Sem ele a
primeira mudanca de formato quebraria os planos do usuario sem caminho de migracao. Mudanca de
formato significa schema novo mais migracao explicita no consumidor; um plano com versao
desconhecida nao pode ser importado.

### v1 -> v2

A v2 foi feita enquanto ainda nao existia consumidor: nenhum arquivo de plano foi gerado e nenhum
codigo lia o formato. Por isso a migracao e um no-op — a versao antiga simplesmente nunca circulou.

| Mudanca | Antes | Depois |
|---|---|---|
| nivel | um campo `nivel` | `nivel_inicial` (imutavel) e `nivel_atual` (mutavel) |
| escala | 3 valores | 5: `iniciante`, `iniciante_basico`, `intermediario`, `avancado`, `especialista` |
| turno do onboarding | `pergunta` | `fala` |
| `meta` | `horasPorSemana`, `duracaoSemanas` | + `prazoPedidoSemanas` e `restricoes`, ambos opcionais |
| estilo dos prompts | sem instrucao | uma linha por prompt de onboarding |

Tres dessas precisam de counterparts no Java, e existem:

| No contrato | No codigo |
|---|---|
| `nivel_inicial` / `nivel_atual` | `PlanEntity.nivelInicial` / `PlanEntity.nivelAtual` |
| `meta.horasPorSemana` / `duracaoSemanas` | `PlanEntity.horasPorSemana` / `duracaoSemanas` |
| `meta.restricoes` | `PlanEntity.restricoes` |
| `nos[].segundosEstudados` (total, derivado) | `PlanNodeEntity.segundosEstudados` |
| (sessoes, detalhes do contador) | `SessaoEstudoEntity` |

As colunas novas de `plans` sao anulaveis no banco porque um plano criado antes da V2 existe de
verdade e nao tem nivel nem orcamento: nao ha valor para backfill porque qualquer um seria
inventado. O contrato, diferentemente, exige os dois niveis, porque um arquivo importado tem de
declarar os dois. Banco mais permissivo que arquivo e o sentido correto — o arquivo e a fronteira
com o exterior.

Quem produz `nivel_inicial` e `nivel_atual`: o gerador de plano ainda emite um campo so, `nivel`,
que e o nivel de partida. Na importacao ele vira os dois, com `nivel_atual` igual a `nivel_inicial` —
a copia inicial e o unico ponto onde os dois coincidem, e e por isso que nao ha campo de nivel no
`plan-generation/esquema-saida.json` para os dois. Divergir depois e o produto mudando `nivel_atual`,
que e a unica direcao que faz sentido.

`pergunta` virou `fala` porque o turno de uma conversa de professor nao e so uma pergunta: e
reconhecer o que a pessoa acabou de dizer e depois perguntar, num texto so. O campo `pergunta`
empurrava a IA para a pergunta seca. `nivelamento` mantem `perguntas` na saida porque ali e uma
lista de sondagens planejadas de uma vez, nao um turno de conversa — mas o `historico` dele usa
`fala`, igual ao de `descoberta`.

## Invariantes que JSON Schema nao expressa

JSON Schema descreve um no. Estas regras involveem a arvore inteira, e sao asserts do
`ContratoPlanoTest` (em `apps/api/src/test/`):

1. ids unicos;
2. exatamente um no raiz, com `paiId == null`;
3. todo `paiId` aponta para um `id` existente;
4. `id` obedece a regra de caminho;
5. sem ciclos;
6. `ordem` sem repeticao entre irmaos.

O schema usa `additionalProperties: false` em todos os objetos: o plano normalizado e produzido por
codigo nosso, entao campo desconhecido e drift e tem de ser pego. A tolerancia a lixo fica no parsing
da resposta do LLM, nunca no plano.

## Prompts

Todo `.txt` em `prompts/` e **prompt de sistema**. Nao existe `user.txt`: os dados do usuario vao no
prompt de usuario, montado pelo codigo como JSON. Por isso cada `.txt` declara no proprio texto o
formato de entrada que vai receber.

Convencoes, valendo para os sete:

- saida estruturada obrigatoria: JSON estrito, nada fora do JSON (sem cerca de markdown, sem
  comentario, sem prosa antes ou depois). Resposta em prosa e o modo de falha mais comum de LLM;
- cada etapa tem um `*.esquema.json` (ou `esquema-saida.json`) ao lado, auto-contido e sem `$ref`,
  porque API de saida estruturada nao resolve referencias;
- sem persona generica ("voce e um assistente..."): ela nao melhora a saida e gasta token. O prompt
  descreve tarefa, entrada, saida e restricoes;
- 2 a 3 exemplos few-shot por prompt, em dominios diferentes entre si (evita ancoragem), com pelo
  menos um caso-limite. Todo exemplo de saida e validado contra o esquema da etapa;
- campos e enums em comum com `plano.schema.json` sao **identicos**, e o `ContratoPlanoTest`
  compara os dois arquivos para garantir isso.

`plan-generation/system.txt` nao pede `id`, `slug`, `ordem`, `status` nem `nivelDominio` ao LLM: o
StudyS deriva os cinco ao percorrer a arvore. Menos campo pedido, menos erro. O esquema de saida usa
aninhamento em vez de `paiId` justamente porque aninhamento expressa a hierarquia com uma chave a
menos.

## Fixtures

`fixtures/resposta-llm-exemplo.txt` e o texto bruto que um LLM devolveria ao prompt de
`plan-generation`. `fixtures/plano-esperado.json` e o plano normalizado que essa resposta deve
produzir.

**Proveniencia sintetica.** A resposta foi escrita a mao. Este projeto ainda **nao faz chamada a IA**
(nem a Fase 1 nem nenhuma anterior): nao existe cliente de provedor, nem logica de parsing no
repositorio — `PlanEngineService.gerarArvoreEstatica` monta uma arvore fixa. O par existe para fixar
o formato antes de existir o codigo que o consome. **Quando houver chamada real a IA, substitua os
dois arquivos por uma captura verdadeira**, mantendo as falhas tipicas abaixo, e so entao o par passa
a ser um detector de drift entre as implementacoes TypeScript e Java.

### Falhas que o fixture exercita

Cinco falhas tipicas de LLM, todas deliberadas:

| Falha | Onde |
|---|---|
| preambulo em prosa antes do JSON | `"Claro! Aqui esta a hierarquia..."` |
| cerca de markdown | ```` ```json ```` |
| pos-script em prosa depois do JSON | `"Espero que ajude!"` |
| virgula sobrando | depois de `"filhos": []` |
| campo a mais | `"exemplo": "def dobro(x): ..."` |
| valor fora do enum | `"tipo": "LEITURA"`, que nao existe em `PlanNodeType` |

O arquivo e `.txt` e nao `.json` de proposito: virgula sobrando e cerca de markdown tornam o arquivo
JSON invalido, e um `.json` invalido quebra editor, linter e validador.

**Nenhuma dessas falhas e tolerada hoje**, porque nao existe parser. O fixture define a tolerancia
**alvo** do parser que a proxima fase vai escrever. A regra de normalizacao proposta, a ser
documentada junto com o parser: `tipo` fora do enum cai para o tipo do nivel em que o no esta
(MATERIA/CAPITULO/TOPICO/AULA por profundidade), campo desconhecido e descartado, `status` e
`nivelDominio` sao sempre `TODO` e `0`.
