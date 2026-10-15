package com.studys.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova de que a aplicacao cria o diretorio do banco antes de abrir a conexao.
 *
 * <p>SQLite nao cria diretorio. Se {@code ~/.studys/} nao existir, o primeiro start numa maquina
 * nova quebra, e {@code @PostConstruct} comum pode rodar tarde demais, porque Flyway e o DataSource
 * inicializam antes dele. Este teste sobe um segundo contexto real, apontado para um diretorio
 * temporario apagado logo antes, que e exatamente a situacao de uma maquina nova. Nao mocka nada.
 */
class BancoSqliteBootstrapTest {

    private static final String UUID_QUALQUER = "3f1c2b8e-7d4a-4c6f-9e10-5b2d8a4f1c33";

    private static final String HEX_ID = "11111111111111111111111111111111";

    private static final String HEX_PLAN_AUSENTE = "22222222222222222222222222222222";

    @Test
    @DisplayName("o contexto sobe com o diretorio do banco inexistente e cria diretorio e arquivo")
    void contextoCriaDiretorioEArquivo() throws IOException {
        Path diretorio = Path.of(System.getProperty("java.io.tmpdir"), "studys-bootstrap-" + UUID_QUALQUER);
        Path arquivo = diretorio.resolve("studys.db");
        apagar(diretorio);

        assertThat(diretorio).doesNotExist();
        assertThat(arquivo).doesNotExist();

        try (ConfigurableApplicationContext contexto = novoContexto(diretorio)) {
            assertThat(contexto.isRunning()).isTrue();
        }

        assertThat(diretorio).as("o diretorio do banco tem de ser criado na subida").isDirectory();
        assertThat(arquivo).as("o arquivo do banco tem de ser criado na subida").isRegularFile();
        assertThat(Files.size(arquivo)).isPositive();
    }

    @Test
    @DisplayName("o arquivo criado e um banco SQLite de verdade, com as tabelas do StudyS e sem users")
    void bancoTemEsquemaEsperado() throws IOException {
        Path diretorio = Path.of(System.getProperty("java.io.tmpdir"), "studys-esquema-" + UUID_QUALQUER);
        Path arquivo = diretorio.resolve("studys.db");
        apagar(diretorio);

        try (ConfigurableApplicationContext contexto = novoContexto(diretorio)) {
            assertThat(contexto.isRunning()).isTrue();

            assertThat(magicNumber(arquivo)).isEqualTo("SQLite format 3");

            List<String> tabelas = tabelas(urlDe(diretorio));
            assertThat(tabelas)
                    .contains("plans", "plan_nodes", "attempts", "content_cache", "resources", "sessoes_estudo")
                    .as("a tabela users tem de ter sumido junto com a autenticacao")
                    .doesNotContain("users");

            // foreign_keys e um pragma por conexao: so da para ler o que o app ve se a leitura
            // usar a mesma URL, com o mesmo ?foreign_keys=on
            assertThat(PRAGMA(urlDe(diretorio), "foreign_keys")).as("PRAGMA foreign_keys").isEqualTo(1);
        }
    }

    @Test
    @DisplayName("o plano guarda nivel de partida e nivel atual, mais o orcamento do contrato v2")
    void colunasDoPlanoV2() throws IOException {
        Path diretorio = Path.of(System.getProperty("java.io.tmpdir"), "studys-v2-plano-" + UUID_QUALQUER);
        apagar(diretorio);

        try (ConfigurableApplicationContext contexto = novoContexto(diretorio)) {
            assertThat(contexto.isRunning()).isTrue();
        }

        String url = urlDe(diretorio);
        assertThat(colunas(url, "plans"))
                .as("nivel_inicial e o registro imutavel do ponto de origem; nivel_atual e o que o produto mostra")
                .contains("nivel_inicial", "nivel_atual")
                .as("orcamento que dimensionou o plano")
                .contains("horas_por_semana", "duracao_semanas", "prazo_pedido_semanas", "restricoes");
    }

    @Test
    @DisplayName("sessoes_estudo guarda o que foi contado, por que parou e quando houve progresso")
    void tabelaDeSessoes() throws IOException {
        Path diretorio = Path.of(System.getProperty("java.io.tmpdir"), "studys-v3-sessoes-" + UUID_QUALQUER);
        apagar(diretorio);

        try (ConfigurableApplicationContext contexto = novoContexto(diretorio)) {
            assertThat(contexto.isRunning()).isTrue();
        }

        String url = urlDe(diretorio);
        assertThat(colunas(url, "sessoes_estudo"))
                .as("segundos_acumulados e o tempo creditado; motivo_fim diz por que a sessao parou")
                .contains("id", "plan_node_id", "iniciada_em", "ultima_atividade_em", "ultimo_progresso_em",
                        "segundos_acumulados", "motivo_fim")
                .as("midia_ativa e o override: video rodando nunca pausa a contagem")
                .contains("midia_ativa");

        assertThat(colunas(url, "plan_nodes"))
                .as("o total por no e a soma das sessoes; comeca em zero, sem backfill inventado")
                .contains("segundos_estudados");
    }

    @Test
    @DisplayName("com foreign_keys ligado, inserir no com pai inexistente e recusado pelo banco")
    void chaveEstrangeiraAtiva() throws IOException {
        Path diretorio = Path.of(System.getProperty("java.io.tmpdir"), "studys-fk-" + UUID_QUALQUER);
        apagar(diretorio);

        try (ConfigurableApplicationContext contexto = novoContexto(diretorio)) {
            assertThat(contexto.isRunning()).isTrue();
        }

        try (var conexao = DriverManager.getConnection(urlDe(diretorio));
                var consulta = conexao.createStatement()) {
            conexao.setAutoCommit(false);
            assertThat(tentativaRecusada(consulta))
                    .as("plan_nodes apontando para um plano inexistente tem de ser recusado")
                    .isTrue();
            conexao.rollback();
        } catch (SQLException e) {
            throw new AssertionError("nao consegui falar com o banco SQLite: " + e.getMessage(), e);
        }
    }

    /**
     * Sobe a aplicacao de verdade contra um diretorio novo. {@code WebApplicationType.NONE} evita
     * ocupar porta: aqui o que importa e a cadeia Flyway/DataSource/Hibernate, que nao depende de web.
     *
     * <p>As sobrescritas vao como argumento de linha de comando, e nao por
     * {@code SpringApplicationBuilder.properties(...)}: aquele registra {@code defaultProperties},
     * que tem a precedencia mais baixa de todas e perde para o {@code application.properties} de
     * {@code src/test/resources}. Com ele, este teste rodava contra o banco do tmpdir, que ja
     * existia, e a criacao de diretorio nunca era exercitada.
     */
    private static ConfigurableApplicationContext novoContexto(Path diretorio) {
        return new SpringApplicationBuilder(StudySApplication.class)
                .web(WebApplicationType.NONE)
                .run(
                        "--spring.datasource.url=jdbc:sqlite:" + diretorio.resolve("studys.db")
                                + "?foreign_keys=on&journal_mode=WAL&busy_timeout=5000",
                        "--spring.datasource.driver-class-name=org.sqlite.JDBC",
                        "--spring.datasource.hikari.maximum-pool-size=1",
                        "--spring.jpa.properties.hibernate.dialect=org.hibernate.community.dialect.SQLiteDialect",
                        "--spring.jpa.hibernate.ddl-auto=validate");
    }

    @Test
    @DisplayName("o pool tem 1 conexao, como em producao: SQLite tem escritor unico")
    void poolDeUmaConexao() throws IOException {
        // apagar antes: um banco deixado de uma rodada anterior carrega o Flyway com as migrations
        // ja aplicadas, e ai a falha e de checksum em vez de falha de codigo — confuso de ler e
        // esconderia erro novo de migration.
        Path diretorio = Path.of(System.getProperty("java.io.tmpdir"), "studys-pool-" + UUID_QUALQUER);
        apagar(diretorio);

        try (ConfigurableApplicationContext contexto = novoContexto(diretorio)) {
            var dataSource = (com.zaxxer.hikari.HikariDataSource) contexto.getBean(javax.sql.DataSource.class);
            assertThat(dataSource.getMaximumPoolSize())
                    .as("SQLite tem escritor unico: mais de uma conexao concorrencia da SQLITE_BUSY")
                    .isEqualTo(1);
        }
    }

    private static String urlDe(Path diretorio) {
        return "jdbc:sqlite:" + diretorio.resolve("studys.db") + "?foreign_keys=on&journal_mode=WAL&busy_timeout=5000";
    }

    private static List<String> tabelas(String url) {
        try (var conexao = DriverManager.getConnection(url);
                var consulta = conexao.createStatement();
                var resultado = consulta.executeQuery(
                        "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")) {
            List<String> nomes = new ArrayList<>();
            while (resultado.next()) {
                nomes.add(resultado.getString("name"));
            }
            return nomes;
        } catch (SQLException e) {
            throw new AssertionError("nao consegui ler o esquema de " + url + ": " + e.getMessage(), e);
        }
    }

    private static List<String> colunas(String url, String tabela) {
        try (var conexao = DriverManager.getConnection(url);
                var consulta = conexao.createStatement();
                var resultado = consulta.executeQuery("PRAGMA table_info(" + tabela + ")")) {
            List<String> nomes = new ArrayList<>();
            while (resultado.next()) {
                nomes.add(resultado.getString("name"));
            }
            return nomes;
        } catch (SQLException e) {
            throw new AssertionError(
                    "nao consegui ler as colunas de " + tabela + " em " + url + ": " + e.getMessage(), e);
        }
    }

    private static int PRAGMA(String url, String pragma) {
        try (var conexao = DriverManager.getConnection(url);
                var consulta = conexao.createStatement();
                var resultado = consulta.executeQuery("PRAGMA " + pragma)) {
            return resultado.next() ? resultado.getInt(1) : -1;
        } catch (SQLException e) {
            throw new AssertionError("nao consegui ler o PRAGMA " + pragma + ": " + e.getMessage(), e);
        }
    }

    private static boolean tentativaRecusada(java.sql.Statement consulta) {
        try {
            // os ids sao BLOB de 16 bytes: o mesmo formato que o @UuidGenerator do Hibernate 7 grava,
            // senao a recusa viria por tipo e nao por chave estrangeira ausente
            consulta.execute("INSERT INTO plan_nodes (id, plan_id, titulo, nivel_dominio, status, ordem, tipo,"
                    + " criado_em) VALUES (x'" + HEX_ID + "', x'" + HEX_PLAN_AUSENTE + "', 'orfa', 0,"
                    + " 'TODO', 0, 'TOPICO', '2026-10-01T12:00:00Z')");
            return false;
        } catch (SQLException esperada) {
            return true;
        }
    }

    private static void apagar(Path caminho) throws IOException {
        if (!Files.exists(caminho)) {
            return;
        }
        try (var fluxo = Files.walk(caminho)) {
            fluxo.sorted(Comparator.reverseOrder()).forEach(alvo -> {
                try {
                    Files.deleteIfExists(alvo);
                } catch (IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            });
        }
    }

    private static String magicNumber(Path arquivo) throws IOException {
        byte[] inicio = new byte[15];
        try (var entrada = Files.newInputStream(arquivo)) {
            if (entrada.read(inicio) != inicio.length) {
                throw new AssertionError("arquivo menor que o cabecalho do SQLite: " + arquivo);
            }
        }
        return new String(inicio, StandardCharsets.US_ASCII);
    }
}
