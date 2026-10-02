package com.studys.db;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cria o diretorio do arquivo SQLite antes de abrir a primeira conexao.
 *
 * <p>O SQLite cria o arquivo, mas nao o diretorio: numa maquina nova, com {@code ~/.studys/}
 * inexistente, o primeiro start quebra ao abrir a conexao. Um {@code @PostConstruct} comum nao
 * resolve, porque Flyway e o DataSource inicializam antes dos beans comuns. Aqui a criacao acontece
 * dentro do proprio metodo de fabrica do {@code DataSource}, que e a primeira coisa a rodar na
 * cadeia de persistencia — antes do pool, antes do Flyway, antes do Hibernate.
 *
 * <p>Declarar o bean aqui tambem desliga o auto-config de DataSource do Spring Boot, que so_age
 * quando nao existe um {@code DataSource} na aplicacao. Por isso o bean e montado com
 * {@link DataSourceProperties}: as propriedades de {@code spring.datasource.*}, inclusive as do
 * Hikari, continuam valendo.
 */
@Configuration
public class SqliteDataSourceConfig {

    private static final String PREFIXO = "jdbc:sqlite:";

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(DataSourceProperties properties) {
        criarDiretorioDoBanco(properties.getUrl());
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * Extrai o caminho do arquivo da URL JDBC do SQLite e cria o diretorio que o contem. Nao faz
     * nada para outra URL, nem para banco em memoria (que nao tem arquivo).
     */
    static void criarDiretorioDoBanco(String url) {
        Path arquivo = arquivoDe(url);
        if (arquivo == null) {
            return;
        }
        Path diretorio = arquivo.getParent();
        if (diretorio == null) {
            return;
        }
        try {
            Files.createDirectories(diretorio);
        } catch (IOException e) {
            throw new UncheckedIOException("Nao consegui criar o diretorio do banco em " + diretorio, e);
        }
    }

    private static Path arquivoDe(String url) {
        if (url == null || !url.startsWith(PREFIXO)) {
            return null;
        }
        String caminho = url.substring(PREFIXO.length());
        int interrogacao = caminho.indexOf('?');
        if (interrogacao >= 0) {
            caminho = caminho.substring(0, interrogacao);
        }
        if (caminho.isEmpty() || caminho.equals(":memory:")) {
            return null;
        }
        // "file:memdb1?mode=memory" e "file:/abs/db.sqlite" sao URI, nao caminho de arquivo.
        // Sem este guarda, Path.of("file:memdb1") faria createDirectories criar um diretorio
        // espúrio no diretorio de trabalho do processo.
        if (caminho.startsWith("file:")) {
            return null;
        }
        try {
            return Path.of(caminho);
        } catch (InvalidPathException e) {
            return null;
        }
    }
}
