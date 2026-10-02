package com.studys.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de contrato do plano normalizado.
 *
 * <p>Escopo, deliberadamente limitado: este projeto <strong>ainda nao tem</strong> logica de parsing da resposta do
 * LLM. {@code PlanEngineService.gerarArvoreEstatica} monta uma arvore fixa e nao existe cliente de IA no
 * repositorio, o que dispara o desvio registrado no Relatorio final da Fase 1. Por isso este teste
 * <strong>nao</strong> converte {@code resposta-llm-exemplo.txt}: ele valida {@code plano-esperado.json} contra a
 * estrutura que {@code plano.schema.json} exige, com asserts explicitos e sem depender de biblioteca de
 * JSON Schema. Quando o parser existir, o passo que roda o parsing e compara com o fixture entra aqui
 * sem mexer no resto.
 */
class ContratoPlanoTest {

    private static final Path CONTRATO = Path.of("..", "..", "contrato");

    private static final Set<String> CHAVES_PLANO =
            Set.of("schemaVersion", "origem", "criadoEm", "assunto", "nivel", "meta", "nos");

    private static final Set<String> CHAVES_NO =
            Set.of("id", "paiId", "tipo", "titulo", "ordem", "nivelDominio", "status", "slug");

    private static final Set<String> CHAVES_CONTEUDO = Set.of("tipo", "texto", "fontes");

    private static final Set<String> CHAVES_RECURSO = Set.of("tipo", "url", "titulo");

    private static final Set<String> TIPOS_NO = Set.of("MATERIA", "CAPITULO", "TOPICO", "AULA");

    private static final Set<String> STATUS_NO = Set.of("TODO", "IN_PROGRESS", "DONE", "SKIPPED");

    private static final Set<String> NIVEIS = Set.of("iniciante", "intermediario", "avancado");

    private static final Set<String> TIPOS_CONTEUDO = Set.of("THEORY", "QUESTIONS", "PROJECT");

    private static final Set<String> TIPOS_RECURSO = Set.of("VIDEO", "ARTIGO", "SITE");

    private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    private static final Pattern DATA_HORA = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})$");

    private static final ObjectMapper JSON = new ObjectMapper();

    private static JsonNode plano;

    private static JsonNode schema;

    private static JsonNode esquemaSaida;

    @BeforeAll
    static void carregarContrato() throws IOException {
        plano = lerPlano();
        schema = lerRaiz("plano.schema.json");
        esquemaSaida = lerRaiz("prompts", "plan-generation", "esquema-saida.json");
    }

    /**
     * O Maven roda o teste com o diretorio do modulo como CWD, entao o contrato fica a dois niveis acima.
     * Falha com mensagem explicita quando o caminho nao existe, em vez de estourar um NoSuchFileException
     * sem dizer qual arquivo faltou.
     */
    private static Path caminho(String... partes) {
        Path caminho = CONTRATO;
        for (String parte : partes) {
            caminho = caminho.resolve(parte);
        }
        if (!Files.isRegularFile(caminho)) {
            throw new IllegalStateException("Arquivo de contrato ausente: " + caminho.toAbsolutePath()
                    + " (rode o build a partir da raiz do modulo apps/api)");
        }
        return caminho;
    }

    private static JsonNode lerPlano() throws IOException {
        return JSON.readTree(Files.readString(caminho("fixtures", "plano-esperado.json")));
    }

    private static JsonNode lerRaiz(String... partes) throws IOException {
        return JSON.readTree(Files.readString(caminho(partes)));
    }

    // ------------------------------------------------------------------ estrutura do plano

    @Test
    @DisplayName("plano tem exatamente as chaves de topo do contrato, sem campo desconhecido")
    void chavesDeTopo() {
        assertThat(chaves(plano)).containsExactlyInAnyOrderElementsOf(CHAVES_PLANO);
    }

    @Test
    @DisplayName("schemaVersion e 1 e criadoEm e data-time")
    void versionamentoEData() {
        assertThat(plano.get("schemaVersion").isInt()).isTrue();
        assertThat(plano.get("schemaVersion").intValue()).isEqualTo(1);
        assertThat(DATA_HORA.matcher(plano.get("criadoEm").asText()).matches())
                .as("criadoEm fora de ISO-8601: %s", plano.get("criadoEm").asText())
                .isTrue();
    }

    @Test
    @DisplayName("origem, nivel e assunto valem um valor do contrato")
    void camposEscalares() {
        assertThat(Set.of("web-ai", "web-script", "desktop")).contains(plano.get("origem").asText());
        assertThat(NIVEIS).contains(plano.get("nivel").asText());
        assertThat(plano.get("assunto").asText()).isNotBlank();
    }

    @Test
    @DisplayName("meta tem so as duas chaves inteiras e ambas >= 1")
    void meta() {
        JsonNode meta = plano.get("meta");
        assertThat(chaves(meta)).containsExactlyInAnyOrder("horasPorSemana", "duracaoSemanas");
        for (String chave : List.of("horasPorSemana", "duracaoSemanas")) {
            assertThat(meta.get(chave).isIntegralNumber()).as("%s deveria ser inteiro", chave).isTrue();
            assertThat(meta.get(chave).intValue()).as("%s >= 1", chave).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    @DisplayName("nos tem ao menos um item e cada no tem so as chaves do contrato")
    void nosNaoVaziosEChavesConhecidas() {
        assertThat(plano.get("nos").isArray()).isTrue();
        assertThat(plano.get("nos").isEmpty()).isFalse();
        for (JsonNode no : plano.get("nos")) {
            assertThat(chaves(no)).as("chaves do no %s", no.path("id").asText())
                    .containsExactlyInAnyOrderElementsOf(CHAVES_NO);
        }
    }

    @Test
    @DisplayName("tipo, status, ordem e nivelDominio de cada no valem valores do contrato")
    void enumsDeNo() {
        for (JsonNode no : plano.get("nos")) {
            String id = no.path("id").asText();
            assertThat(TIPOS_NO).as("tipo de %s", id).contains(no.get("tipo").asText());
            assertThat(STATUS_NO).as("status de %s", id).contains(no.get("status").asText());
            assertThat(no.get("ordem").isIntegralNumber()).as("ordem de %s", id).isTrue();
            assertThat(no.get("ordem").intValue()).as("ordem de %s", id).isGreaterThanOrEqualTo(0);
            assertThat(no.get("nivelDominio").isIntegralNumber()).as("nivelDominio de %s", id).isTrue();
            assertThat(no.get("nivelDominio").intValue()).as("nivelDominio de %s", id)
                    .isGreaterThanOrEqualTo(0);
            assertThat(no.get("titulo").asText()).as("titulo de %s", id).isNotBlank();
        }
    }

    @Test
    @DisplayName("slug casa com o padrao minusculo-sem-acento do contrato")
    void slugs() {
        for (JsonNode no : plano.get("nos")) {
            String slug = no.get("slug").asText();
            assertThat(SLUG.matcher(slug).matches()).as("slug invalido: %s", slug).isTrue();
        }
    }

    // Nao ha @Test para checarConteudoERecursos(plano): nosNaoVaziosEChavesConhecidas fixa cada no
    // nos 8 campos obrigatorios, entao os ramos de conteudo e recursos sao inalcancaveis nesse
    // fixture. A cobertura real dos campos opcionais e ramoOpcionalComConteudoERecursos.
    private void checarConteudoERecursos(JsonNode raiz) {
        for (JsonNode no : raiz.get("nos")) {
            String id = no.path("id").asText();
            if (no.has("conteudo")) {
                JsonNode conteudo = no.get("conteudo");
                assertThat(chaves(conteudo)).as("chaves de conteudo de %s", id)
                            .containsExactlyInAnyOrderElementsOf(CHAVES_CONTEUDO);
                assertThat(TIPOS_CONTEUDO).as("tipo de conteudo de %s", id).contains(conteudo.get("tipo").asText());
                assertThat(conteudo.get("texto").asText()).as("texto de %s", id).isNotBlank();
                assertThat(conteudo.get("fontes").isArray()).as("fontes de %s", id).isTrue();
            }
            if (no.has("recursos")) {
                assertThat(no.get("recursos").isArray()).as("recursos de %s", id).isTrue();
                for (JsonNode recurso : no.get("recursos")) {
                    assertThat(chaves(recurso)).as("chaves do recurso de %s", id)
                            .containsExactlyInAnyOrderElementsOf(CHAVES_RECURSO);
                    assertThat(TIPOS_RECURSO).as("tipo do recurso de %s", id).contains(recurso.get("tipo").asText());
                    assertThat(recurso.get("url").asText()).as("url do recurso de %s", id).startsWith("http");
                }
            }
        }
    }

    @Test
    @DisplayName("todo no nasce com status TODO e nivelDominio 0, como o contrato promete para plano novo")
    void planoNovoNasceTodo() {
        // plano-esperado.json vem de resposta-llm-exemplo.txt, que nao tem campo "status" nem
        // "nivelDominio": os dois sao derivados (TODO e 0). Se um dia o fixture for editado para
        // DONE/1, o par resposta/fixture parou de ser coerente e este teste acusa.
        for (JsonNode no : plano.get("nos")) {
            String id = no.path("id").asText();
            assertThat(no.get("status").asText()).as("status de %s", id).isEqualTo("TODO");
            assertThat(no.get("nivelDominio").intValue()).as("nivelDominio de %s", id).isZero();
        }
    }

    // ------------------------------------------------------------------ invariantes de arvore

    @Test
    @DisplayName("todo id e unico")
    void idsUnicos() {
        Set<String> vistos = new HashSet<>();
        for (JsonNode no : plano.get("nos")) {
            assertThat(vistos.add(no.get("id").asText()))
                    .as("id repetido: %s", no.get("id").asText()).isTrue();
        }
    }

    @Test
    @DisplayName("existe exatamente um no raiz, com paiId nulo")
    void exatamenteUmaRaiz() {
        List<JsonNode> raizes = new ArrayList<>();
        for (JsonNode no : plano.get("nos")) {
            if (no.get("paiId").isNull()) {
                raizes.add(no);
            }
        }
        assertThat(raizes).hasSize(1);
        assertThat(raizes.get(0).get("tipo").asText()).isEqualTo("MATERIA");
    }

    @Test
    @DisplayName("todo paiId aponta para um id existente")
    void paisApontamParaNosExistentes() {
        Set<String> ids = idsDo(plano);
        for (JsonNode no : plano.get("nos")) {
            JsonNode pai = no.get("paiId");
            if (!pai.isNull()) {
                assertThat(ids).as("pai %s do no %s", pai.asText(), no.get("id").asText()).contains(pai.asText());
            }
        }
    }

    @Test
    @DisplayName("id da raiz e o proprio slug; id dos demais e paiId + / + slug")
    void idSegueARegraDeCaminho() {
        for (JsonNode no : plano.get("nos")) {
            String id = no.get("id").asText();
            String slug = no.get("slug").asText();
            if (no.get("paiId").isNull()) {
                assertThat(id).as("raiz %s", id).isEqualTo(slug);
            } else {
                assertThat(id).as("no %s", id).isEqualTo(no.get("paiId").asText() + "/" + slug);
            }
        }
    }

    @Test
    @DisplayName("a arvore nao tem ciclo: subindo por paiId sempre se chega a raiz")
    void semCiclos() {
        for (JsonNode no : plano.get("nos")) {
            Set<String> visitados = new LinkedHashSet<>();
            JsonNode atual = no;
            while (!atual.get("paiId").isNull()) {
                assertThat(visitados.add(atual.get("id").asText()))
                        .as("ciclo passando por %s", atual.get("id").asText()).isTrue();
                String paiId = atual.get("paiId").asText();
                JsonNode pai = porId(paiId);
                assertThat(pai).as("pai %s nao existe", paiId).isNotNull();
                atual = pai;
            }
        }
    }

    @Test
    @DisplayName("ordem nao se repete entre irmaos")
    void ordemUnicaEntreIrmaos() {
        Map<String, Set<Integer>> ordensPorPai = new HashMap<>();
        for (JsonNode no : plano.get("nos")) {
            String chave = no.get("paiId").isNull() ? "RAIZ" : no.get("paiId").asText();
            Set<Integer> ordens = ordensPorPai.computeIfAbsent(chave, k -> new HashSet<>());
            assertThat(ordens.add(no.get("ordem").intValue()))
                    .as("ordem %d repetida sob %s", no.get("ordem").intValue(), chave).isTrue();
        }
    }

    // ------------------------------------------------------------------ arranjo entre artefatos

    @Test
    @DisplayName("enums de nivel e tipo sao identicos no plano.schema.json e no esquema-saida.json")
    void enumsComunsSaoIdenticos() {
        assertThat(valores(esquemaSaida.path("properties").path("nivel").path("enum")))
                .as("enum nivel divergente entre plano.schema.json e plan-generation/esquema-saida.json")
                .isEqualTo(valores(enumDoPlano("nivel")));
        assertThat(valores(esquemaSaida.path("properties").path("nos").path("items")
                .path("properties").path("tipo").path("enum")))
                .as("enum tipo divergente entre plano.schema.json e plan-generation/esquema-saida.json")
                .isEqualTo(valores(enumDoNo("tipo")));
    }

    @Test
    @DisplayName("meta do esquema de saida declara os mesmos campos e limites de meta do plano.schema.json")
    void metaDoEsquemaDeSaidaBateComAPlano() {
        JsonNode camposPlano = schema.path("properties").path("meta").path("properties");
        JsonNode camposSaida = esquemaSaida.path("properties").path("meta").path("properties");
        assertThat(chaves(camposSaida)).isEqualTo(chaves(camposPlano));
        // propertyNames() de um ArrayNode devolve vazio: os dois lados seriam comparados como
        // conjuntos vazios e a assercao passaria sem conferir nada. Aqui a lista e lida de verdade.
        assertThat(valores(esquemaSaida.path("properties").path("meta").path("required")))
                .containsExactlyInAnyOrder("horasPorSemana", "duracaoSemanas");
        assertThat(valores(schema.path("properties").path("meta").path("required")))
                .containsExactlyInAnyOrder("horasPorSemana", "duracaoSemanas");
        for (String chave : List.of("horasPorSemana", "duracaoSemanas")) {
            assertThat(camposSaida.path(chave).path("type").asText())
                    .isEqualTo(camposPlano.path(chave).path("type").asText());
            assertThat(camposSaida.path(chave).path("minimum").intValue())
                    .isEqualTo(camposPlano.path(chave).path("minimum").intValue());
        }
    }

    @Test
    @DisplayName("o esquema de saida nao pede ao LLM os campos que o StudyS deriva")
    void esquemaDeSaidaNaoPedeDerivados() {
        String bruto = esquemaSaida.toString();
        for (String proibido : List.of("\"id\"", "\"slug\"", "\"ordem\"", "\"status\"", "\"nivelDominio\"")) {
            assertThat(bruto).as("esquema-saida.json pede %s, que o StudyS deriva ao percorrer a arvore", proibido)
                    .doesNotContain(proibido);
        }
    }

    // ------------------------------------------------------------------ ramo opcional

    @Test
    @DisplayName("um no com conteudo e recursos passa nas mesmas checagens estruturais")
    void ramoOpcionalComConteudoERecursos() {
        String bruto = """
                {
                  "schemaVersion": 1,
                  "origem": "desktop",
                  "criadoEm": "2026-10-01T12:00:00Z",
                  "assunto": "Java",
                  "nivel": "avancado",
                  "meta": { "horasPorSemana": 3, "duracaoSemanas": 4 },
                  "nos": [
                    { "id": "java", "paiId": null, "tipo": "MATERIA", "titulo": "Java", "ordem": 0,
                      "nivelDominio": 0, "status": "IN_PROGRESS", "slug": "java",
                      "conteudo": { "tipo": "THEORY", "texto": "texto", "fontes": [] },
                      "recursos": [ { "tipo": "VIDEO", "url": "https://example.test/v", "titulo": "Aula" } ] }
                  ]
                }
                """;
        JsonNode comOpcionais = JSON.readTree(bruto);
        assertThat(chaves(comOpcionais)).containsExactlyInAnyOrderElementsOf(CHAVES_PLANO);
        JsonNode no = comOpcionais.get("nos").get(0);
        // as duas chaves opcionais entram por cima das obrigatorias, e nada mais
        Set<String> chavesEsperadas = new LinkedHashSet<>(CHAVES_NO);
        chavesEsperadas.addAll(List.of("conteudo", "recursos"));
        assertThat(chaves(no)).containsExactlyInAnyOrderElementsOf(chavesEsperadas);
        assertThat(chaves(no.get("conteudo"))).containsExactlyInAnyOrderElementsOf(CHAVES_CONTEUDO);
        assertThat(chaves(no.get("recursos").get(0))).containsExactlyInAnyOrderElementsOf(CHAVES_RECURSO);
        checarConteudoERecursos(comOpcionais);
    }

    // ------------------------------------------------------------------ auxiliares

    private static Set<String> chaves(JsonNode no) {
        return new LinkedHashSet<>(no.propertyNames());
    }

    private static List<String> valores(JsonNode arrayDeEnums) {
        List<String> valores = new ArrayList<>();
        arrayDeEnums.forEach(valor -> valores.add(valor.asText()));
        return valores;
    }

    private static JsonNode enumDoPlano(String propriedade) {
        return schema.path("properties").path(propriedade).path("enum");
    }

    private static JsonNode enumDoNo(String propriedade) {
        return schema.path("properties").path("nos").path("items").path("properties").path(propriedade).path("enum");
    }

    private static Set<String> idsDo(JsonNode raiz) {
        Set<String> ids = new HashSet<>();
        for (JsonNode no : raiz.get("nos")) {
            ids.add(no.get("id").asText());
        }
        return ids;
    }

    private static JsonNode porId(String id) {
        for (JsonNode no : plano.get("nos")) {
            if (no.get("id").asText().equals(id)) {
                return no;
            }
        }
        return null;
    }
}
