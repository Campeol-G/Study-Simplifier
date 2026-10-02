# StudyS

O StudyS é um simplificador de estudos. Você aponta um assunto e as horas por semana que tem, e
recebe um plano navegável: uma sequência de nós que vai do geral ao detalhe, com conteúdo e recursos
em cada passo, e registro das tentativas para saber onde está faltando reforço.

O estudo é tratado como **grafo**, não como lista. Cada nó depende do anterior, então marcar um como
feito não é só uma flag: é o que desbloqueia o próximo.

## Estado atual

O que **funciona**: criar plano, listar, buscar, mudar o status do plano e o status de cada nó, com
persistência em SQLite, migrations versionadas e 39 testes de integração passando.

O que **ainda não existe**, apesar de estar declarado no código:

- **Onboarding com IA.** `services/onboarding-chat` é um módulo vazio. Não há conversa de
  descoberta, nem nivelamento, nem chamada a provedor de IA em lugar nenhum do repositório.
- **Geração de conteúdo.** `services/content-generation` é um módulo vazio. A tabela `content_cache`
  existe e aceita vários tipos por tópico, mas nada escreve nela.
- **Readaptação do plano.** A árvore gerada hoje é estática: uma MATERIA e três TOPICOs, com
  `PlanEngineService.gerarArvoreEstatica`. Não reage a desempenho, a tentativas nem a tempo decorrido.
- **Leitura e escrita de tentativas.** A tabela `attempts` existe e nada a preenche.
- **Integração com a web.** `apps/web` é um protótipo visual com dados mockados; não chama a API.
  O contrato em `contrato/` existe para que as duas conversem por arquivo JSON, mas essa conversa
  ainda não foi construída.

## Pré-requisitos

- **Java 27.**
- Nada mais. O Maven vem pelo wrapper (`./mvnw`), que baixa a própria versão.

## Como rodar

```bash
# testes
./mvnw clean test

# aplicação (http://localhost:8080)
./mvnw -DskipTests install        # uma vez: resolve os módulos de biblioteca
./mvnw -pl apps/api spring-boot:run
```

O `install` é necessário porque `spring-boot:run` roda só em `apps/api`, e os outros seis módulos
são bibliotecas que precisam estar resolvidas para ela compilar.

Endpoints (todos sem autenticação):

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/plans` | cria um plano e a árvore de nós |
| `GET` | `/plans` | lista os planos |
| `GET` | `/plans/{id}` | busca um plano com seus nós |
| `PATCH` | `/plans/{id}` | muda o status (`em andamento`, `pausado`, `concluído`) |
| `PATCH` | `/plans/{planId}/nodes/{nodeId}` | muda o status do nó (`TODO`, `IN_PROGRESS`, `DONE`, `SKIPPED`) |

Todo erro responde `{"mensagem": "..."}` no mesmo formato. Erro de servidor nunca vaza detalhe
interno: responde `{"mensagem": "Erro interno"}` e a exceção completa vai para o log.

## Onde ficam os dados

Tudo em **um arquivo**: `~/.studys/studys.db`. O diretório e o arquivo são criados no primeiro start,
se não existirem.

É seguro apagar com o app fechado, mas você **perde tudo**. Sem servidor e sem nuvem, não há backup
automático: copie o arquivo se quiser guardar.

Ao lado dele aparecem `studys.db-wal` e `studys.db-shm`, arquivos temporários do modo WAL. Não são
cópias: pode ignorá-los.

## Estrutura de diretórios

Sete módulos Maven e duas pastas fora do build. `apps/` são executáveis, `services/` são regras de
domínio, `shared/` é o que tudo compartilha e `infra/` é a base.

| Módulo | O que faz |
|---|---|
| `shared/schemas` | contratos Java compartilhados (plano, nó, tentativa, conteúdo, chat) |
| `infra/db` | entidades JPA, repositórios e a migration do SQLite; cria o diretório do banco |
| `services/plans` | CRUD de planos, sem web e sem HTTP |
| `services/plan-engine` | gera a árvore de nós e muda o status de um nó |
| `services/content-generation` | declarado, **vazio**: geração de conteúdo a partir do cache |
| `services/onboarding-chat` | declarado, **vazio**: conversa de descoberta e nivelamento |
| `apps/api` | app Spring Boot executável: controllers, tratamento de erro, testes |
| `contrato/` | contrato de dados entre desktop e web: schema do plano, prompts, fixtures |
| `apps/web` | front React/Vite, protótipo, **fora** do Maven |

`contrato/` é a fonte única do formato de plano e dos prompts de IA, compartilhada entre o desktop
(Java) e a web (TypeScript). Está **congelado** a partir do commit que o criou: mudar qualquer
arquivo ali exige migração explícita nos dois lados e um `schemaVersion` novo. Ver
[`contrato/LEIA-ME.md`](contrato/LEIA-ME.md).

## Decisões de arquitetura

- **SQLite, e não um banco com servidor.** O StudyS roda na máquina da pessoa. Um PostgreSQL exigiria
  Docker ou um serviço instalado antes do primeiro start, o que é exatamente a infraestrutura que um
  app local não deve ter. O preço é um escritor por vez.
- **Sem autenticação.** É um app de usuário único, sem servidor: não existe ameaça de autenticação
  a enfrentar, só complexidade em toda requisição. Tabela `users`, JWT e bcrypt saíram do projeto.
- **Custo zero de infraestrutura.** Sem nuvem, sem container, sem conta. O arquivo do banco é a
  única coisa que existe fora do código.
- **Uma árvore, não uma lista.** O `parent_id` em `plan_nodes` é o que permite expressar dependência
  e, mais adiante, readaptação.
- **IDs de caminho no contrato.** No arquivo JSON trocado entre desktop e web, o id de um nó é
  `paiId + "/" + slug`, não um UUID: importar o mesmo plano duas vezes atualiza em vez de duplicar.
  Dentro do banco o id continua UUID, porque é chave primária e não precisa ser legível.
