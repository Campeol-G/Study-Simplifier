CREATE TABLE users (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE plans (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    assunto VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_plans_user_id ON plans (user_id);

CREATE TABLE plan_nodes (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL REFERENCES plans (id) ON DELETE CASCADE,
    parent_id UUID REFERENCES plan_nodes (id) ON DELETE SET NULL,
    titulo VARCHAR(200) NOT NULL,
    nivel_dominio INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_nodes_plan_id ON plan_nodes (plan_id);
CREATE INDEX idx_nodes_parent_id ON plan_nodes (parent_id);

CREATE TABLE content_cache (
    id UUID PRIMARY KEY,
    topico_slug VARCHAR(255) NOT NULL UNIQUE,
    tipo VARCHAR(30) NOT NULL,
    conteudo TEXT NOT NULL,
    fontes TEXT,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE attempts (
    id UUID PRIMARY KEY,
    plan_node_id UUID NOT NULL REFERENCES plan_nodes (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    pontuacao DOUBLE PRECISION,
    concluido_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_attempts_node ON attempts (plan_node_id);
CREATE INDEX idx_attempts_user ON attempts (user_id);
