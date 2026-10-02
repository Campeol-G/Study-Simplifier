package com.studys.api;

import java.util.Map;
import java.util.UUID;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlanNodeFlowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void criarPlanoGeraArvoreEstatica() throws Exception {
        JsonNode plano = criarPlano("Java");
        String planId = plano.get("id").asText();
        JsonNode nodes = plano.get("nodes");

        assertThat(nodes.size()).isEqualTo(4);

        JsonNode raiz = nodes.get(0);
        assertThat(raiz.get("parentId").isNull()).isTrue();
        assertThat(raiz.get("tipo").asText()).isEqualTo("MATERIA");
        assertThat(raiz.get("status").asText()).isEqualTo("TODO");
        assertThat(raiz.get("ordem").asInt()).isZero();
        assertThat(raiz.get("nivelDominio").asInt()).isZero();
        assertThat(raiz.get("titulo").asText()).isEqualTo("Java");
        assertThat(raiz.get("slug").asText()).isEqualTo(planId + "-materia");

        for (int i = 1; i <= 3; i++) {
            JsonNode topico = nodes.get(i);
            assertThat(topico.get("parentId").asText()).isEqualTo(raiz.get("id").asText());
            assertThat(topico.get("tipo").asText()).isEqualTo("TOPICO");
            assertThat(topico.get("status").asText()).isEqualTo("TODO");
            assertThat(topico.get("ordem").asInt()).isEqualTo(i);
            assertThat(topico.get("nivelDominio").asInt()).isZero();
            assertThat(topico.get("slug").asText()).isEqualTo(planId + "-topico-" + i);
        }
    }

    @Test
    void listarEBuscarDevolvemOsNosReais() throws Exception {
        String planId = criarPlano("Spring").get("id").asText();

        MvcResult lista = mvc.perform(get("/plans"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode doLista = json.readTree(lista.getResponse().getContentAsString()).get(0);
        assertThat(doLista.get("nodes").size()).isEqualTo(4);

        MvcResult busca = mvc.perform(get("/plans/" + planId))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json.readTree(busca.getResponse().getContentAsString()).get("nodes").size()).isEqualTo(4);
    }

    @Test
    void atualizarStatusDoNoAceitaNomeDoEnum() throws Exception {
        JsonNode plano = criarPlano("Banco de dados");
        String planId = plano.get("id").asText();
        String nodeId = primeiroTopico(plano);

        MvcResult atualizado = mvc.perform(patch("/plans/" + planId + "/nodes/" + nodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("status", "DONE"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = json.readTree(atualizado.getResponse().getContentAsString());
        assertThat(node.get("id").asText()).isEqualTo(nodeId);
        assertThat(node.get("status").asText()).isEqualTo("DONE");

        MvcResult minusculo = mvc.perform(patch("/plans/" + planId + "/nodes/" + nodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("status", "done"))))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json.readTree(minusculo.getResponse().getContentAsString()).get("status").asText())
                .isEqualTo("DONE");

        MvcResult busca = mvc.perform(get("/plans/" + planId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode nodes = json.readTree(busca.getResponse().getContentAsString()).get("nodes");
        assertThat(nodes.get(1).get("status").asText()).isEqualTo("DONE");
        assertThat(nodes.get(2).get("status").asText()).isEqualTo("TODO");
    }

    @Test
    void statusInvalidoOuAusenteRetorna400() throws Exception {
        JsonNode plano = criarPlano("Go");
        String planId = plano.get("id").asText();
        String nodeId = primeiroTopico(plano);

        mvc.perform(patch("/plans/" + planId + "/nodes/" + nodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("status", "explodindo"))))
                .andExpect(status().isBadRequest());

        mvc.perform(patch("/plans/" + planId + "/nodes/" + nodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest());
    }

    /**
     * Sem tabela users nao existe o cenario "no de outro usuario": todo no pertence a quem esta
     * usando o app. O que sobra e o no de outro plano, que continua 404.
     */
    @Test
    void noDeOutroPlanoRetorna404() throws Exception {
        String planIdA = criarPlano("Rust").get("id").asText();
        String nodeIdA = primeiroTopico(buscarPlano(planIdA));
        String planIdB = criarPlano("Zig").get("id").asText();

        mvc.perform(patch("/plans/" + planIdB + "/nodes/" + nodeIdA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("status", "DONE"))))
                .andExpect(status().isNotFound());

        mvc.perform(patch("/plans/" + planIdA + "/nodes/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("status", "DONE"))))
                .andExpect(status().isNotFound());
    }

    private JsonNode buscarPlano(String planId) throws Exception {
        MvcResult r = mvc.perform(get("/plans/" + planId)).andExpect(status().isOk()).andReturn();
        return json.readTree(r.getResponse().getContentAsString());
    }

    private JsonNode criarPlano(String assunto) throws Exception {
        MvcResult r = mvc.perform(post("/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("assunto", assunto, "descricao", "Do zero"))))
                .andExpect(status().isCreated())
                .andReturn();
        return json.readTree(r.getResponse().getContentAsString());
    }

    private String primeiroTopico(JsonNode plano) {
        for (JsonNode node : plano.get("nodes")) {
            if (node.get("tipo").asText().equals("TOPICO")) {
                return node.get("id").asText();
            }
        }
        throw new IllegalStateException("Nenhum TOPICO na arvore");
    }
}
