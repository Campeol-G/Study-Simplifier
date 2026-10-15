-- V3: contador de tempo de estudo — o total no no e o balanco detalhado na sessao.
--
-- segundos_estudados em plan_nodes e NOT NULL DEFAULT 0, e 0 e honesto: significa "nenhuma sessao
-- contada ainda". Aceitar null obrigaria todo o produto a tratar null e zero como coisas diferentes
-- quando nao sao.
--
-- sessoes_estudo e o balanco detalhado do total: por que a sessao parou (motivo_fim) e quando houve
-- progresso pela ultima vez (ultimo_progresso_em). A regra de parada e dupla — sem input do sistema
-- E sem progresso no app — e por isso que a sessao precisa guardar as duas metades da evidencia.
--
-- midia_ativa e o override da regra: com video rodando a contagem nao pausa, porque quem assiste a
-- video nao produz input e nao muda status. A coluna e declarada BOOLEAN, e nao INTEGER, porque o
-- ddl-auto=validate compara o tipo declarado com o que o Hibernate espera para um campo boolean:
-- com INTEGER ele derruba a subida com "found [integer], but expecting [boolean]". O SQLite nao tem
-- booleano de verdade e aceita o nome; o que importa aqui e o nome, nao o armazenamento.
--
-- A regra que decide creditar ou pausar NAO esta aqui: esta tabela guarda a resposta, e quem calcula
-- a resposta ainda nao existe.

ALTER TABLE plan_nodes ADD COLUMN segundos_estudados INTEGER NOT NULL DEFAULT 0;

CREATE TABLE sessoes_estudo (
    id BLOB PRIMARY KEY,
    plan_node_id BLOB NOT NULL REFERENCES plan_nodes (id) ON DELETE CASCADE,
    iniciada_em TIMESTAMP NOT NULL,
    ultima_atividade_em TIMESTAMP NOT NULL,
    ultimo_progresso_em TIMESTAMP,
    segundos_acumulados INTEGER NOT NULL DEFAULT 0,
    midia_ativa BOOLEAN NOT NULL DEFAULT 0,
    motivo_fim VARCHAR(30)
);
CREATE INDEX idx_sessoes_node ON sessoes_estudo (plan_node_id);