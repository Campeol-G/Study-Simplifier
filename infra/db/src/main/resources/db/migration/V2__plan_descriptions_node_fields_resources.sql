ALTER TABLE plans ADD COLUMN descricao TEXT;

ALTER TABLE plan_nodes ADD COLUMN slug VARCHAR(255);
ALTER TABLE plan_nodes ADD COLUMN ordem INTEGER NOT NULL DEFAULT 0;
ALTER TABLE plan_nodes ADD COLUMN tipo VARCHAR(30);
CREATE INDEX idx_nodes_slug ON plan_nodes (slug);

CREATE TABLE resources (
    id UUID PRIMARY KEY,
    plan_node_id UUID NOT NULL REFERENCES plan_nodes (id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    thumbnail_url VARCHAR(1024),
    ordem INTEGER NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_resources_plan_node_id ON resources (plan_node_id);
