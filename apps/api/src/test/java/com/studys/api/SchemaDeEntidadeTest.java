package com.studys.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O modelo JPA e a migration precisam concordar sobre o mesmo schema.
 *
 * <p>{@code ddl-auto=validate} confere coluna, tipo e nulabilidade — mas <strong>não</strong> confere
 * restrição de unicidade. Por isso uma migration correta e uma entidade com {@code unique = true}
 * convivem em silêncio, e o cache de conteúdo volta a aceitar um tipo por tópico assim que alguém
 * gerar DDL a partir das entidades. Estes testes montam o schema a partir das entidades
 * ({@code ddl-auto=create}) e checam a restrição que importa.
 */
class SchemaDeEntidadeTest {

    @Test
    @DisplayName("content_cache aceita vários tipos para o mesmo topico_slug")
    void contentCacheAceitaVariosTiposPorTopico() throws IOException {
        Path diretorio = Path.of(System.getProperty("java.io.tmpdir"), "studys-schema-cache");
        apagar(diretorio);
        Path arquivo = diretorio.resolve("studys.db");

        try (ConfigurableApplicationContext contexto = contextoComDdlGeradoPeloHibernate(diretorio)) {
            assertThat(contexto.isRunning()).isTrue();
        }

        String sql = "INSERT INTO content_cache (id, topico_slug, tipo, conteudo, atualizado_em)"
                + " VALUES (x'%s', 'java/fundamentos', '%s', 'texto', '2026-10-01T12:00:00Z')";
        try (var conexao = DriverManager.getConnection("jdbc:sqlite:" + arquivo);
                var consulta = conexao.createStatement()) {
            consulta.execute(sql.formatted("A1", "THEORY"));
            consulta.execute(sql.formatted("A2", "QUESTIONS"));
            consulta.execute(sql.formatted("A3", "PROJECT"));

            var total = consulta.executeQuery("SELECT COUNT(*) FROM content_cache WHERE topico_slug ="
                    + " 'java/fundamentos'");
            assertThat(total.next()).isTrue();
            assertThat(total.getInt(1))
                    .as("teoria, questões e projeto do mesmo tópico têm de coexistir")
                    .isEqualTo(3);
        } catch (SQLException e) {
            throw new AssertionError("o cache de conteúdo recusou um segundo tipo para o mesmo tópico: "
                    + e.getMessage(), e);
        } finally {
            apagar(diretorio);
        }
    }

    private static ConfigurableApplicationContext contextoComDdlGeradoPeloHibernate(Path diretorio) {
        return new SpringApplicationBuilder(StudySApplication.class)
                .web(WebApplicationType.NONE)
                .run(
                        "--spring.datasource.url=jdbc:sqlite:" + diretorio.resolve("studys.db"),
                        "--spring.datasource.driver-class-name=org.sqlite.JDBC",
                        "--spring.jpa.properties.hibernate.dialect=org.hibernate.community.dialect.SQLiteDialect",
                        "--spring.flyway.enabled=false",
                        "--spring.jpa.hibernate.ddl-auto=create");
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
}
