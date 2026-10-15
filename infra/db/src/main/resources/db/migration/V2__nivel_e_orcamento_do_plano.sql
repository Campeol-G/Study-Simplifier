-- V2: nivel por plano, o par que o contrato v2 passou a exigir.
--
-- Colunas novas de plans sao ANULAVEIS, e isso e deliberado: um plano criado antes desta V2
-- existe de verdade e nao tem nivel nem orcamento — nao ha valor para backfill porque qualquer
-- valor seria inventado. O contrato (plano.schema.json), diferentemente, exige nivel_inicial e
-- nivel_atual porque um arquivo importado tem de declarar os dois. Banco mais permissivo que
-- arquivo e o sentido correto: o arquivo e a fronteira com o exterior.
--
-- nivel_inicial e nivel_atual sao dois campos e nao um so porque sao coisas diferentes: o primeiro
-- e o registro imutavel de onde a pessoa partiu, o segundo e o que o produto mostra e sobre o que
-- sugere conteudo. Uma coluna unica perderia a origem assim que o usuario avanca. No importacao os
-- dois nascem iguais — a copia inicial e o unico ponto onde coincidem.
--
-- prazo_pedido_semanas e restricoes sao opcionais tambem no arquivo: sao o que a pessoa deu por
-- conta propria, e a ausencia deles significa "a pessoa nao disse", nao "sao zero".
--
-- Esta migration e nova de proposito. O Flyway guarda o checksum das migrations ja aplicadas, entao
-- editar a V1 quebraria a subida do app em qualquer maquina que ja rodou a baseline.

ALTER TABLE plans ADD COLUMN nivel_inicial VARCHAR(30);
ALTER TABLE plans ADD COLUMN nivel_atual VARCHAR(30);
ALTER TABLE plans ADD COLUMN horas_por_semana INTEGER;
ALTER TABLE plans ADD COLUMN duracao_semanas INTEGER;
ALTER TABLE plans ADD COLUMN prazo_pedido_semanas INTEGER;
ALTER TABLE plans ADD COLUMN restricoes TEXT;