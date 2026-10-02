-- Baseline unica do StudyS para SQLite.
--
-- Substitui as tres migrations anteriores, que eram do banco com servidor e incluíam a tabela users.
-- O banco e recriado do zero: nao ha dado de usuario existente para migrar, e o app desktop e de
-- usuario unico, sem conta.
--
-- UUIDs: gravados como BLOB de 16 bytes. A primeira versao desta baseline usava TEXT hifenizado,
-- que era mais legivel no sqlite3 CLI, mas ddl-auto=validate derrubou a aplicacao com "wrong
-- column type ... found [text], but expecting [blob]": o @UuidGenerator do Hibernate 7 mapeia UUID
-- para binario de 16 bytes. O DDL mudou para BLOB em vez de relaxar o validate. Para ler no shell:
--   SELECT hex(id), assunto FROM plans;
--
-- Timestamps: coluna TIMESTAMP. O SQLite nao tem tipo de data/hora proprio e ignora o nome, mas o
-- ddl-auto=validate compara o tipo declarado com o que o Hibernate espera para Instant, entao o
-- nome precisa ser TIMESTAMP. O driver grava e le ISO-8601 UTC.
--
-- Texto longo (descricao, conteudo, fontes): coluna TEXT, mapeada com @JdbcTypeCode(LONGVARCHAR).
-- Atencao: apagar o columnDefinition = "TEXT" sem repor o mapeamento faria o Hibernate assumir
-- varchar(255) e truncaria o conteudo gerado — foi o que aconteceu na primeira versao desta
-- baseline. Ver o Relatorio final da Fase 2.

CREATE TABLE plans (
    id BLOB PRIMARY KEY,
    assunto VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL,
    descricao TEXT,
    criado_em TIMESTAMP NOT NULL
);

CREATE TABLE plan_nodes (
    id BLOB PRIMARY KEY,
    plan_id BLOB NOT NULL REFERENCES plans (id) ON DELETE CASCADE,
    parent_id BLOB REFERENCES plan_nodes (id) ON DELETE SET NULL,
    titulo VARCHAR(200) NOT NULL,
    nivel_dominio INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    slug VARCHAR(255),
    ordem INTEGER NOT NULL DEFAULT 0,
    tipo VARCHAR(30) NOT NULL,
    criado_em TIMESTAMP NOT NULL
);
CREATE INDEX idx_nodes_plan_id ON plan_nodes (plan_id);
CREATE INDEX idx_nodes_parent_id ON plan_nodes (parent_id);
CREATE INDEX idx_nodes_slug ON plan_nodes (slug);

CREATE TABLE resources (
    id BLOB PRIMARY KEY,
    plan_node_id BLOB NOT NULL REFERENCES plan_nodes (id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    thumbnail_url VARCHAR(1024),
    ordem INTEGER NOT NULL DEFAULT 0,
    criado_em TIMESTAMP NOT NULL
);
CREATE INDEX idx_resources_plan_node_id ON resources (plan_node_id);

CREATE TABLE content_cache (
    id BLOB PRIMARY KEY,
    topico_slug VARCHAR(255) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    conteudo TEXT NOT NULL,
    fontes TEXT,
    atualizado_em TIMESTAMP NOT NULL,
    -- UNIQUE(topico_slug) sozinha proibia guardar teoria, questoes e projeto do mesmo topico, e o
    -- repositorio consulta por (topico_slug, tipo). Ver achado D3 da AUDITORIA.txt.
    UNIQUE (topico_slug, tipo)
);

CREATE TABLE attempts (
    id BLOB PRIMARY KEY,
    plan_node_id BLOB NOT NULL REFERENCES plan_nodes (id) ON DELETE CASCADE,
    -- pontuacao anulavel de proposito: null significa "nao pontuado", que e semanticamente
    -- diferente de zero. O wrapper Double do Java preserva essa distincao.
    pontuacao DOUBLE,
    concluido_em TIMESTAMP NOT NULL
);
CREATE INDEX idx_attempts_node ON attempts (plan_node_id);
