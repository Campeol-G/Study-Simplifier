# Camada de IA — `infra/llm` e a conexão com o provedor

- **Data:** 2026-10-15
- **Escopo:** o primeiro contato do StudyS com uma IA
- **Estado:** aprovado no desenho, aguardando plano de implementação

---

## 1. Resumo

Hoje o StudyS **não tem nenhuma chamada a IA**. Não existe cliente HTTP no repositório, não existe
parser de resposta de modelo, e `services/onboarding-chat` e `services/content-generation` são módulos
declarados e vazios. O que existe do lado da IA é o **contrato**: `contrato/prompts/` com sete prompts de
sistema e seus esquemas de saída, e `contrato/fixtures/` com um par de arquivos que descreve exatamente o
que o parser vai ter que enfrentar.

Esta fase entrega o **caminho completo entre os dois**: um cliente de provedor reutilizável, um parser
que sobrevive ao que os modelos realmente devolvem, e a política que transforma erro de provedor em
mensagem em português.

Três decisões sustentam o resto do documento:

1. **O cliente é nosso, em camadas, e não depende do StudyS.** Um provedor novo custa duas funções.
2. **A biblioteca pronta foi avaliada e recusada**, com medição — não compalpite.
3. **A chave e a configuração ficam em dois arquivos separados**, config sem segredo e segredo isolado.

---

## 2. O que existe hoje

| existe | onde |
|---|---|
| CRUD de planos com árvore de nós | `services/plans`, `services/plan-engine` |
| status de plano e de nó, com rollback transacional | `apps/api` |
| persistência SQLite em `~/.studys/studys.db`, migrations V1–V3 | `infra/db` |
| formato de erro padronizado `{"mensagem": "..."}` | `GlobalExceptionHandler` |
| contrato congelado: schema do plano, 7 prompts, 7 esquemas, fixtures | `contrato/` |
| 51 testes verdes (46 em `apps/api`, 5 em `infra/db`) | — |

| não existe | onde deveria estar |
|---|---|
| qualquer cliente HTTP | `infra/llm` (módulo inteiro, ainda não criado) |
| parser da resposta do modelo | `infra/llm` |
| onboarding de descoberta e nivelamento | `services/onboarding-chat` (vazio) |
| geração de conteúdo | `services/content-generation` (vazio) |
| onde a chave do provedor é guardada | `~/.studys/` |

**Restrição herdada:** `PlanEngineService.gerarArvoreEstatica` monta 1 MATERIA + 3 TOPICO fixos. É
placeholder, não produto. Esta fase **tira ele do caminho de produção** e o deixa como gerador usado só
por teste.

---

## 3. Objetivo

> Um provedor de IA configurado responde a uma chamada, a resposta vira um objeto validado contra o
> esquema do contrato, e o que não pode ser convertido vira uma mensagem em português que diz o que a
> pessoa precisa fazer.

Segundo o produto: o onboarding conduz a conversa de descoberta e nivelamento de ponta a ponta, e a
criação de plano passa a usar a geração por IA no lugar da árvore estática.

---

## 4. Fora do escopo

Cada item tem sua decisão registrada no `NOTAS.md`, com o porquê completo.

| fora | por quê | onde está a decisão |
|---|---|---|
| **Streaming** | não há interface para renderizar token a token (o shell Tauri é P5 e não começou). Além disso o problema difícil do streaming — stream que morre a 80% e precisa desfazer texto já mostrado — só se resolve com tela. | I11 |
| **Log estruturado** | a decisão desta fase é **não** mostrar erro na tela quando algo falha; o log é o registro. Fazer log de verdade é fase própria, e depende da interface para saber o que é contexto e o que é detalhe técnico. | I12 |
| **Tabela de desvios** (campos que o modelo inventou) | a ideia é boa e está desenhada, mas sem chamada real em produção não há desvio acumulando. Tabela antes de uso cria migration sem dado para consultar. | I13 |
| **Shell / interface** | não existe. O fluxo guído de "escolher provedor → colar chave" é montado como estado e endpoint; quem desenha é o shell. | P5 |
| **Conteúdo** (teoria, questões, projeto) | `services/content-generation` é da fase seguinte. | — |
| **Quarto provedor** | D10: não adicionar sem necessidade real. O formato OpenAI-compatível já cobre Ollama, LM Studio, vLLM, Groq e OpenRouter por configuração de URL. | D10 |

---

## 5. Decisões e o que foi descartado

### 5.1 Cliente em camadas, não biblioteca pronta

**A pergunta que decide** não é "qual biblioteca chamar a IA". É **onde a chamada é quebrada em pedaços**,
porque isso decide o que se escreve uma vez e o que se repete por provedor.

Três candidatos foram comparados:

| | **A — camadas + `HttpClient` do JDK** | **A′ — camadas + SDKs oficiais** | **C — Spring AI 2.0.1** |
|---|---|---|---|
| dependências novas | **+1** (o `networknt`) | **+22** (3 diretas: SDK Anthropic, SDK OpenAI, e o runtime Kotlin que elas puxam) | **+85** |
| acoplamento | nenhum | 2 fornecedores | Spring |
| degraus da D9 cobertos | 4 nossos | 4 nossos | ~1,5 nosso |
| filtros cobertos | 5 nossos | 5 nossos | ~1,5 nosso |
| `Jackson` no classpath | só o `tools.jackson` 3.1.5 | 3.1.5 + 2.19.4 | 3.1.5 + 2.21.5 |
| streaming depois | troca só dentro do adaptador | já vem | já vem |
| `raciocinio` | campo nosso | **veio pronto** | não exposto na abstração |
| esforço inicial | o maior | médio | o menor |

**Medição de `+85` (mesmo `pom.xml`, mudando só o Spring AI):**

```
baseline (deps de apps/api sem os módulos do StudyS) : 106 jars
com spring-ai-starter-model-openai + ...-anthropic   : 191 jars
custo real                                            : +85 jars
```

De onde vem: **21 netty**, 24 `org.springframework` (Spring AI + WebFlux), **5 Kotlin**, **5 Jackson 2**
(`com.fasterxml`, convivendo com o `tools.jackson` do Boot), 3 OkHttp/Okio, 3 victools, 2 Swagger,
2 reactor-netty.

A opção A′ custa **22 jars** (medidos com as duas SDKs e nada mais): 5 Kotlin e 5 Jackson 2 entre eles.
A opção A custa **1**: o `networknt`, que é o validador do degrau 2 da D9 e que o Spring AI traria de
qualquer forma.

**O que a medição eliminou do argumento contra o Spring AI:** a compatibilidade com Spring Boot 4.1.1
**funciona**. O contexto sobe e os dois beans aparecem (`anthropicChatModel`, `openAiChatModel`).

**O que sobrou como argumento:** a D9 e os cinco filtros são nossos nas três opções, e o Spring AI cobre
um e meio dos cinco. Some-se o problema do contrato: `BeanOutputConverter` **gera o JSON Schema a partir
de um `record` Java**, e o `contrato/` está congelado e exige que a fonte da verdade seja o arquivo.
Usar o gerador significa escolher entre deixar de validar contra o contrato ou ignorar o gerador.

### 5.2 Por que duas funções por provedor

O `AdaptadorProvedor` tem duas funções, e a linha é desenhada onde "igual ao outro provedor" acaba:

```java
interface AdaptadorProvedor {
    RequisicaoHttp montarRequisicao(Requisicao req, ConfiguracaoProvedor cfg, String credencial);
    Resposta lerResposta(RespostaHttp http);
}
```

**Dentro delas** está o que de fato difere: a URL (`{base}/chat/completions` contra `{base}/v1/messages`),
o cabeçalho de auth (`Authorization: Bearer` contra `x-api-key` + `anthropic-version`), onde mora o prompt
de sistema (dentro de `messages[0]` contra um campo `system` no topo), e onde volta o texto
(`choices[0].message.content` contra `content[0].text`).

**Fora delas** fica tudo o que é igual: retry, timeout, erro em português, aviso de dado sensível,
extração de JSON e validação de esquema. Escrever essas coisas uma vez por provedor é o que a Approach B
— descartada — faria de errado.

A listagem de modelos fica fora da interface principal porque nem todo provedor a tem:

```java
interface ProvedorComCatalogo extends AdaptadorProvedor {
    List<ModeloDisponivel> listarModelos(ConfiguracaoProvedor cfg, String credencial);
}
```

Separar é o que impede o bug silencioso: lista vazia significa ao mesmo tempo "não tem modelos" (problema)
e "não sabe listar" (normal). Com a interface separada, **o tipo diz à tela o que fazer**.

### 5.3 Sem streaming agora

A primeira versão deste desenho previa `obter()` como atalho sobre um `Consumer<Evento>`. **Isso estava no
lugar errado.** O `Transporte` devolveria a resposta HTTP inteira, e é nele que a resposta vira blocos
quando houver SSE. Para o retrabalho ficar confinado, a fresta teria de estar **no `Transporte` e no
`AdaptadorProvedor` — as duas, ou nenhuma**. Fazer só metade é indireção que não protege nada.

Decidido: **sem streaming**. `obter()` devolve `Resposta`.

### 5.4 Falhar alto, sem degradar

Se a IA não gera o plano, o app **diz que não conseguiu** e a pessoa tenta de novo. Não existe queda para
um modelo menor nem para a árvore estática.

O motivo de a alternativa ser pior do que parece: `gerarArvoreEstatica` produz 1 MATERIA + 3 TOPICO
fixos. Uma pessoa que recebe aquilo recebe um plano falso que **parece** ter funcionado, e não tem como
saber que é placeholder.

---

## 6. Arquitetura

### 6.1 Módulos

```
shared/schemas ──────┐
                     ├──► services/onboarding-chat ──► apps/api
infra/db ────────────┘              │
                                    ▼
                              infra/llm

shared/schemas ──────┐
infra/db ────────────┼──► services/plan-engine ──────► apps/api
                     │
                     └──► infra/llm
```

**`infra/llm` é novo.** Depende de Jackson 3 (já no projeto) e de `com.networknt:json-schema-validator`
(linha 3.x, a que fala com Jackson 3). Nada mais.

**Não depende de `shared/schemas`, `infra/db`, nem de Spring.** Isso não é promessa: é verificado por
build (§12.4).

### 6.2 Os tipos neutros

`shared/schemas/chat/` tem `ChatRequest` com `conversationId` e `ChatResponse` com `onboardingConcluido`,
`nivel`, `sugestoes` e `estagio`. São **domínio do StudyS**, e um `UUID` de conversa não significa nada
para um modelo. Se o cliente falasse esses tipos, ele arrastaria o pacote do domínio e a extração para
outro projeto viria reescrevendo todas as assinaturas.

Então o cliente define os seus:

```java
record Mensagem(Papel papel, String texto)            // SISTEMA | USUARIO | ASSISTENTE
record Requisicao(String modelo, int maxTokens, Double temperatura,
                  JsonNode esquema, List<Mensagem> mensagens)
record Resposta(String texto, String raciocinio, Tokens uso)
```

A tradução para o domínio vive no serviço — umas vinte linhas por serviço, e é ali que mora o conhecimento
do StudyS.

### 6.3 Carregar prompts

P1 estava aberta ("como os prompts chegam ao Java"). **Decidido: opção C — ler do disco em runtime, com
caminho configurável.** O contrato é dado, não código, e editar prompt não deve exigir rebuild; fechar
o arquivo dentro do jar troca um problema que não existe por um que passaria a existir.

`infra/llm` expõe `carregarPrompt("plan-generation/system.txt")` a partir de uma raiz configurável. Ele
**não sabe** que existe uma pasta `contrato/` nem o layout dela — quem sabe é o serviço, que passa o
caminho.

---

## 7. Registro de provedores

### 7.1 Dois arquivos, config separada do segredo

**`~/.studys/provedores.json`** — configuração, sem segredo, editável à mão:

```json
{
  "ativo": "anthropic",
  "provedores": {
    "anthropic":  { "nome": "Anthropic",      "tipo": "anthropic",
                    "urlBase": "https://api.anthropic.com", "modeloPadrao": "claude-opus-5" },
    "meu-ollama": { "nome": "Ollama (local)", "tipo": "openai-compativel",
                    "urlBase": "http://localhost:11434/v1", "modeloPadrao": "qwen3" }
  }
}
```

**`~/.studys/chaves.json`** — só segredos, permissão `600`:

```json
{ "anthropic": "sk-ant-...", "meu-ollama": null }
```

A chave é **anulável de propósito**: Ollama não exige chave, e alguns servidores locais exigem uma string
burra. Forçar chave seria mentir sobre o estado normal.

**Por que dois arquivos.** A configuração é editável e versionável; a chave não entra nela. Misturar as
duas é como a chave acaba num print de configuração ou num anexo de bug.

**Por que `id` e `nome` separados.** O `id` é estável e usado em código; o `nome` é o rótulo que aparece
para a pessoa. Um id estável impede que trocar o rótulo quebre referências.

### 7.2 Quem é dono do quê

| em `infra/llm` (reutilizável) | no StudyS (`apps/api`) |
|---|---|
| `ConfiguracaoProvedor` — id, nome, tipo, urlBase, modeloPadrao | lê `~/.studys/provedores.json` |
| `FonteDeCredencial` — `chaveDe(id)` | lê `~/.studys/chaves.json` |
| `Catalogo` — `listar()`, `ativo()`, `configuracaoDe(id)` | a implementação em arquivo |

`FonteDeCredencial` como interface é o que torna a extração trivial: quem usar a biblioteca em outro
projeto implementa a mesma interface lendo variável de ambiente, keychain do SO, ou o que for.

### 7.3 Endpoints

| | |
|---|---|
| `GET /provedores` | lista; devolve `nome`, `tipo`, `urlBase`, `modeloPadrao` e `possuiChave: bool` — **nunca a chave** |
| `POST /provedores` | cadastra; valida chamando `/v1/models` antes de salvar |
| `PATCH /provedores/{id}` | edita; `{"ativo": true}` troca o ativo |
| `DELETE /provedores/{id}` | remove (não pode remover o que está ativo) |
| `GET /provedores/{id}/modelos` | lista os modelos; devolve `livre: true` quando a lista falhar ou vier vazia |

**Estado inicial:** sem provedor nenhum, o app **sobe** (D8) e `GET /provedores` devolve lista vazia.
Qualquer tentativa de usar IA responde `{"mensagem": "Nenhum provedor configurado"}`. Não é erro de
configuração — é o primeiro start.

### 7.4 Listar modelos, e a degradação silenciosa

| | endpoint | o que volta |
|---|---|---|
| OpenAI | `GET /v1/models` (`Authorization: Bearer`) | lista de `id` |
| Anthropic | `GET /v1/models` (`x-api-key` + `anthropic-version`) | lista paginada **e as capacidades de cada modelo** |
| Local | `/v1/models` herdado do padrão OpenAI (Ollama tem, em `/v1/models` e no `/api/tags` nativo) | modelos instalados |

O retorno do Anthropic inclui `structured_outputs.supported`, `thinking.supported`, `effort` e
`max_input_tokens` por modelo. Dá para **avisar antes** que um modelo não faz saída estruturada, em vez de
descobrir quando o parse falha.

**A degradação é silenciosa e é deliberada.** Nem todo servidor OpenAI-compatível implementa
`/v1/models`. Quando a lista falha ou vem vazia, a resposta carrega `livre: true` — que **não é mensagem
para o usuário**, é o sinal estrutural de "a tela desenha um campo de texto em vez de uma lista". O
detalhe técnico (URL, status, corpo) vai para o log.

---

## 8. O caminho de uma chamada

```
POST /chat {"conteudo": "quero aprender Rust"}
   │
   │  ┌── SERVIÇO ────────────────────────────────────────────────────┐
   1  │  carrega o prompt do disco        contrato/prompts/...      │
   2  │  monta o prompt de usuário        {"assuntoBruto": ...,   │
   │  │                                    "historico": [...]}     │
   │ ┌───────────────────────────────────────────────────────────────┐ │
   │  │  CLIENTE (infra/llm) — igual para os 3 provedores            │ │
   3  │  varre dado sensível  → avisos, NÃO bloqueia                 │ │
   4  │  ┌── tentativa 1..3 com backoff ────────────────────────────┐ │ │
   │  │  │  montarRequisicao(req, config, credencial)               │ │ │
   │  │  │  transporte.enviar(http)                                 │ │ │
   │  │  └──────────────────────────────────────────────────────────┘ │ │
   5  │  lerResposta(http) → Resposta(texto, raciocinio)              │ │
   │  │  erro do provedor → português                                │ │
   6  │  D9: extrair JSON do texto → validar → fallback               │ │
   │ ┌───────────────────────────────────────────────────────────────┐ │
   │  │  o que só o StudyS sabe                                     │ │
   7  │  invariantes de árvore, derivação de id/slug/ordem           │ │
   8  │  monta o objeto de domínio, persiste, devolve                │ │
```

### 8.1 Retry

| situação | retry? |
|---|---|
| 429, 500, 502, 503, 504 | sim |
| timeout, conexão caiu | sim |
| 400, 401, 403, 404 | **não** — chave inválida não conserta sozinha; repetir é insultar o provedor |

3 tentativas, espera de 1 s → 4 s → 16 s, com dispersão de ±20% (senão dois usos simultâneos batem no
retry juntos).

**Teto de 4 chamadas por ação da pessoa:** 1 normal + 2 retries + 1 fallback. Passou disso, falha alto com
mensagem clara. Sem teto, o pior caso é 6 chamadas e a pessoa esperando sem saber por quê.

### 8.2 Timeout — por chamada, não global

Um turno de conversa responde em segundos. Um plano com 40 nós leva minutos.

| chamada | timeout |
|---|---|
| turno de conversa | 60 s |
| geração de plano | 180 s |
| geração de conteúdo | 300 s |

Quem passa o valor é o serviço, na hora da chamada.

**A premissa a remover:** mais tempo **não** significa resposta melhor — modelo generativo não melhora
com tempo. O que justifica o timeout generoso é o **custo de truncar**: se a resposta for cortada no meio
do JSON, o resultado é inválido e a chamada é perdida. A espera custa uma pessoa olhando um indicador; o
corte custa uma chamada.

Consequência que precisa ser registrada: quando a resposta é cortada por falta de `max_tokens`, o
`stop_reason` do provedor diz isso. É um motivo **corrigível** ("o modelo não coube"), e não deve ser
apresentado como falha genérica.

### 8.3 Erro em português

Cada erro vira a mensagem do padrão do projeto, com o motivo real:

| veio | vira |
|---|---|
| 401 / 403 | "Chave inválida ou sem permissão. Confira `~/.studys/chaves.json`." |
| 404 | "Provedor não encontrado. Confira a URL base." |
| 429 | "Limite de uso atingido. Tente de novo em instantes." |
| 400 · contexto estourado | "O modelo não cabe com todo o histórico. Reduza o histórico ou troque de modelo." |
| 5xx depois dos retries | "O provedor não respondeu. Tente de novo." |
| timeout | "O provedor demorou demais." |

O 400 de contexto é o único que exige **traduzir o corpo do erro**, porque o status sozinho não diz o que
aconteceu.

### 8.4 Dado sensível

Varre e-mail, telefone, CPF, cartão e CEP na mensagem. **Não bloqueia, não redige.** Devolve uma lista de
avisos que vai na resposta da API e no log.

A consequência é aceita de propósito: se a pessoa usa provedor em nuvem e colou o e-mail dela, a
informação **sai da máquina**. O app diz isso; a decisão é dela.

### 8.5 Guarda-corpo da resposta

O que fazer quando a resposta sai do esperado. Quatro casos, e é honesto dizer que **um deles é só
cobertura parcial**:

| caso | como é tratado |
|---|---|
| **recusa do modelo** ("não posso ajudar com isso") | reprova na validação do degrau 2 e cai no fallback. **Cobertura parcial**: funciona, mas a pessoa lê "a resposta não bate com o esquema" quando o motivo real é recusa. Falta reconhecer a recusa e dizer isso. |
| **resposta enorme** | teto de tamanho antes de parsear. JSON de megabytes trava o parse e é sempre defeito, nunca conteúdo legítimo. |
| **o modelo ecoa dado sensível de volta** | a mesma varredura do §8.4, agora na resposta. É a única forma de o dado sair da máquina sem a pessoa ter digitado nada sensível. |
| **conteúdo fora do assunto** | a validação de esquema já pega: campo que não existe é perdoado e descartado, valor que não bate é rejeitado. |

**O que fica para depois:** reconhecer a recusa e dar a ela mensagem própria. Depende de lista de
mensagens de recusa que varia por provedor e por modelo, e essa lista é um tipo de conhecimento que hoje
mora fora do repositório. Registrado em I12, junto com o log — é o mesmo tipo de coisa: dar nome ao que
aconteceu em vez de dizer "deu ruim".

---

## 9. A D9 em detalhe

A ordem é: **saída estruturada → parse estrito → validação de invariantes → fallback textual.**

### 9.1 O que é de cada camada

| degrau | camada | por quê |
|---|---|---|
| 1 — pedir saída estruturada | `infra/llm`, dentro de `montarRequisicao` | o erro nasce menor |
| 2 — extrair e validar contra o `.esquema.json` | `infra/llm`, com `networknt` | valida **localmente**, sem confiar que o provedor obedeceu |
| 3 — invariantes que JSON Schema não expressa | **serviço** | JSON Schema descreve um nó; estas regras são sobre a **árvore inteira** |
| 4 — segunda chamada pedindo só o JSON | `infra/llm` | modelo estruturado ainda falha, e a alternativa é a pessoa ver erro |

O degrau 3 é o mais específico do StudyS e nenhuma biblioteca o faz: uma raiz só, todo `paiId` aponta
para alguém que existe, sem ciclo, `id` obedece à regra de caminho, `ordem` sem repetição entre irmãos.

### 9.2 A regra de perdão

O `plano.schema.json` tem `additionalProperties: false`, que reprova campo desconhecido; e o `LEIA-ME.md`
diz que campo desconhecido é descartado. As duas coisas só valerem se a ordem for:

1. extrai o JSON do texto;
2. **perdoa** — se *todos* os erros são "propriedade desconhecida", remove exatamente aqueles caminhos e
   revalida;
3. valida estritamente;
4. se ainda falhou → segunda chamada.

Ou seja: **campo a mais é perdoado; qualquer outra coisa precisa ser consertada.** Cabe numa linha e é
testável.

### 9.3 O que o extrator tolera, e o que não tolera

| falha | quem trata |
|---|---|
| preâmbulo em prosa antes do JSON | **extrator** — achar o primeiro `{` e o último `}` é confiável |
| cerca de ``` ```json ``` | **extrator** |
| pos-script em prosa depois | **extrator** |
| campo a mais | passo "perdoar" |
| vírgula sobrando | **fallback** (§9.4) |
| `"tipo": "LEITURA"` (fora do enum) | **serviço**, normaliza pela profundidade |

**A vírgula sobrando não é remendada, e é uma decisão.** O Jackson 3 tem
`JsonReadFeature.ALLOW_TRAILING_COMMA` e ligar seria uma flag. Não ligar é deliberado: remendar JSON com
regex quebra quando a vírgula está dentro de uma string, e um parser tolerante demais acaba aceitando
lixo. Remendar é tratar **prosa**; remendar **erro de sintaxe** é outra coisa. O fixture exercita esse
caminho pelo fallback, e isso é mais honesto do que fingir que o extrator conserta.

### 9.4 O fallback

Se a validação falha, uma segunda chamada pedindo "só o JSON, sem cercas, sem prosa", a partir do texto
cru. Ela conta para o teto de 4 chamadas e, se também falhar, o erro final é explícito — com os dois
textos crus no log, que é o caso em que o log ganha o seu valor.

### 9.5 O custo de um valor fora do enum

O cliente valida estrito e vai ao fallback **antes** do serviço ver o valor. Então um `"tipo": "LEITURA"`
custa uma chamada a mais. Aceito, porque a alternativa é enfiar conhecimento de domínio no cliente e
perder o reuso. É raro: o prompt pede os valores certos e o esquema os lista.

---

## 10. `raciocinio` — o campo que entra sem ser usado

`Resposta` carrega `raciocinio`. Ele é preenchido quando o provedor manda, e **ninguém liga o pensamento
agora**.

São três mecanismos diferentes, absorvidos por uma string:

| provedor | como chega | observação |
|---|---|---|
| Anthropic | bloco `thinking`, com `budget_tokens` | **incompatível com `temperature`/`top_p`/`top_k`** quando ligado |
| OpenAI (oficial) | `reasoning_effort` | devolve só **resumo** — o raciocínio cru não é exposto |
| OpenAI-compatível | `reasoning_content` | Ollama usa `thinking` |

**Por que não ligar agora:** o raciocínio chega **antes** da resposta, e sem streaming ele apareceria junto
com ela — o que é um painel "por que ele perguntou isso?", não o professor pensando. Ligar também obriga
duas políticas de sampling no Anthropic e custa token e latência em toda chamada.

---

## 11. Testes: três camadas

| | rede | chave | token | roda no `./mvnw test` |
|---|---|---|---|---|
| **1. Requisição gravada** | não | não | não | **sim** |
| **2. Resposta gravada** | não | não | não | **sim** |
| **3. Rede real** | sim | sim | sim | não, só com `STUDYS_TEST_LLM=1` |

**Camada 1** — para cada provedor, `montarRequisicao` é chamada com uma `Requisicao` constante escrita no
teste, e URL, cabeçalhos e corpo são comparados com um arquivo dourado versionado. É a mais barata do
projeto: não precisa de chave nem de rede, e roda em qualquer máquina. Pega o erro mais caro de
diagnosticar — "mudamos o pedido e ninguém soube".

> **Exigência:** `montarRequisicao` tem que ser **determinístico**. Sem relógio, sem aleatoriedade, sem
> nada que mude entre execuções. É o que torna o arquivo dourado comparável.

**Camada 2** — um corpo real gravado entra em `lerResposta` e a `Resposta` é conferida. É onde mora a
**detecção de drift**: quando o provedor mudar o nome de um campo, o teste falha sem rede e sem gastar
token. Sem essa camada, a única forma de descobrir seria gastar uma chamada e ler o resultado no olho.

O `contrato/fixtures/resposta-llm-exemplo.txt` é o caso particular dessa camada: ele nasce sintético e o
`LEIA-ME.md` manda substituí-lo por captura verdadeira quando houver chamada real. **É a camada 2 que torna
essa troca segura.**

**Camada 3** — só com `-Dstudys.llm.live=true` ou `STUDYS_TEST_LLM=1`; sem isso o teste é pulado com
mensagem dizendo por quê. Existe para uma coisa só: pegar o que 1 e 2 não pegam, que é "os dois lados ainda
encaixam depois que a API mudou". Não é sentir-padrão de suíte porque custa dinheiro.

### 11.1 O transporte falso

```java
interface Transporte { RespostaHttp enviar(RequisicaoHttp requisicao); }
```

Uma implementação real (JDK `java.net.http`) e uma falsa, em teste. A fresta fica nesse nível —
o mais baixo que os três provedores compartilham — porque **um único falso serve aos três**. Se a fresta
estivesse no nível do provedor, seriam três falsos, e um teste com o falso do Anthropic não garantiria nada
sobre o mesmo código rodando no Ollama.

### 11.2 A matriz que o falso tem que cobrir

| o falso devolve | o teste prova |
|---|---|
| JSON limpo | o caminho feliz |
| preâmbulo + cerca + pos-script | o extrator funciona |
| vírgula sobrando | o fallback é acionado |
| campo a mais | o passo "perdoar" funciona |
| `"tipo": "LEITURA"` | o serviço normaliza pela profundidade |
| 429, depois 200 | o retry acontece e a segunda é a boa |
| 500 sempre | o teto de 4 chamadas é respeitado |
| 401 | o retry **não** acontece |

O último par é o mais importante: são eles que garantem que o teto não vire desperdício e que chave
inválida não dê para 3 chamadas inúteis.

---

## 12. Fatiamento

Seis entregas, cada uma com spec → plano → commits próprios. A ordem é por **dependência**, não por
tamanho.

| # | entrega | pronto quando |
|---|---|---|
| 1 | `infra/llm`: tipos, `Transporte`, `TransporteFalso` | os tipos existem e o falso responde sem rede |
| 2 | **Anthropic**: adaptador + arquivo dourado | o arquivo dourado bate, sem chave e sem rede |
| 3 | **OpenAI-compatível**: adaptador + `listarModelos` | idem, mais a listagem |
| 4 | **Políticas**: retry, teto, erro em pt-BR, dado sensível, D9 | os 8 comportamentos do falso verdes |
| 5 | **Registro**: os 2 arquivos, os 5 endpoints | cadastra, lista, ativa e remove sem chave |
| 6 | **Serviços**: onboarding de ponta a ponta; plano pela IA | `gerarArvoreEstatica` sai da produção |

**Por que esta ordem.** 1 é a mais barata e, se errada, todas herdam. 2 e 3 são independentes entre si e
cada um é completo sozinho. 4 vem depois porque retry e erro precisam existir para os testes do falso
terem o que exercitar. 5 é a mais visível para quem usa, mas cadastrar um provedor que não sabe chamar é
cadastro sem valor. 6 fecha, porque é a primeira vez que o StudyS inteiro faz uma chamada de verdade.

### 12.1 O que cada uma não faz

| entrega | não cabe |
|---|---|
| 1 | nenhum provedor, nenhuma política, nenhuma chamada |
| 2 e 3 | nenhum retry, nenhuma política de erro, nenhum serviço |
| 4 | nenhum serviço chamando, nenhum registro de provedor |
| 5 | nenhuma chamada de IA, nenhum onboarding |
| 6 | streaming, tabela de desvios, log estruturado |

### 12.2 A entrega 4 é a única que muda de forma

As outras mudam por acréscimo. A etapa 4 é onde mora a política, e a política vai ser **ajustada** quando
a IA errar de um jeito que ninguém previu. Por isso:

- uma classe por política, com um nome cada, e nenhuma conhecendo as outras;
- comportamento por **enum**, não por `if` espalhado;
- todas testadas pela matriz do falso, que vira o contrato escrito da política;
- erro em português é **dado, não gerado** — uma tabela de mapeamento que alguém consegue ler e editar
  sem compilar. Depois de um tempo, o erro que importa é o que ninguém previu.

### 12.3 O que atravessa todas

`contrato/` continua **congelado**. Nenhuma entrega toca em `contrato/`.

### 12.4 A regra que garante o reuso

Nenhum arquivo em `infra/llm/src/main` pode conter a string `com.studys`. Um `grep` no build falha se
aparecer.

**Por que é verificação e não promessa:** decisões de arquitetura apodrecem em silêncio. Daqui a meses
alguém importa um tipo do StudyS por conveniência — compila, passa nos testes, e a biblioteca deixa de ser
independente sem que ninguém perceba. Com o `grep`, isso vira erro de build na hora. O padrão já é do
projeto: o ledger do SDD usa a mesma técnica para garantir que a remoção de autenticação não volte.

---

## 13. Riscos e limites conhecidos

| risco | estado |
|---|---|
| Valor fora do enum gasta uma chamada a mais (§9.5) | aceito, com o motivo |
| `GET /v1/models` ausente em servidor local não-padrão | mitigado por texto livre |
| Prompt local ignorando esquema | mitigado pela validação local do degrau 2 |
| Provedor que cobra por chamada e falha na 4ª | mitigado pelo teto de 4 |
| thinking do Anthropic conflita com `temperature` | evitado: não ligamos thinking |
| `raciocinio` nunca preenchido por um provedor | a string fica vazia; não é erro |
| Migração de streaming futuro toca o `Transporte` | aceito; ver I11 para o porquê de não antecipar |

---

## 14. Evidências medidas

Medições feitas com o `pom.xml` real, não de memória:

| afirmação | como foi verificada |
|---|---|
| Spring AI 2.0.1 compila e sobe no Boot 4.1.1 | contexto iniciado; beans `anthropicChatModel` e `openAiChatModel` presentes |
| custo de +85 jars | dois `pom.xml` idênticos, mudando só o Spring AI |
| Boot 4.1.1 usa Jackson 3 | `tools.jackson.core:jackson-databind:3.1.5` |
| Spring AI traz Jackson 2 junto | `com.fasterxml.jackson.core:jackson-databind:2.21.5` no mesmo classpath |
| Spring AI já traz `networknt` | `com.networknt:json-schema-validator:3.0.1` |
| `BeanOutputConverter` deriva schema de classe | inspecionado o `getJsonSchema()` gerado de um `record` |
| `ResponseTextCleaner` não é bean por padrão | contexto iniciado e a lista voltou vazia; as classes existem |
| Não há advisor de retry | lista de `chat/client/advisor/` inspecionada |
| `AssistantMessage` não expõe raciocínio | `javap` na classe pública |
| SDKs oficiais têm o material | 143 classes com "think" no Anthropic, 346 com "reasoning" na OpenAI |
| `ALLOW_TRAILING_COMMA` existe no Jackson 3 | `javap tools.jackson.core.json.JsonReadFeature` |
| Anthropic devolve capacidades por modelo | `GET /v1/models`, com `structured_outputs` e `thinking` |

---

## 15. Referências

`NOTAS.md` não é versionado — ele vive na máquina de quem desenvolve e não chega num clone novo. As
entradas abaixo são a memória por trás das decisões desta spec:

| entrada | o que sustenta |
|---|---|
| D8 · D13 | chave em `~/.studys/`, dois arquivos, app sobe sem chave, dado sensível só avisa |
| D9 | a ordem de quatro degraus desta spec |
| D10 | os dois formatos de provedor, e por que não há um terceiro |
| D12 | migration é sempre arquivo novo |
| P9 | o fatiamento em seis entregas e o que cada uma não faz |
| I11 | por que streaming não entra agora, e o que ele custaria |
| I12 | por que a falha degrada calada e o log é o registro |
| I13 | por que a tabela de desvios espera uso real |
| I14 | o campo de texto livre precisa validar e sugerir |
| I15 | avisar antes que o modelo não faz saída estruturada |
| I16 | reconhecer recusa do modelo, e dar nome a ela |

- `contrato/LEIA-ME.md` — invariantes de árvore, regra de ids, o que o fixture exercita
- `README.md` — o que funciona e o que ainda não existe