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

/**
 * Fluxo de planos sem autenticacao: o app desktop e de usuario unico e nao tem servidor, logo nao
 * ha o que autenticar. Antes da migracao para SQLite este teste era o AuthPlanFlowTest e gastava a
 * maior parte do codigo registrando e logando usuario; os cenarios de registro, login, 401, 409 e
 * isolamento entre usuarios sairam junto com a tabela users.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlanFlowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void criarPlanoSemNenhumHeaderDeAutenticacao() throws Exception {
        MvcResult criado = mvc.perform(post("/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("assunto", "Java", "descricao", "Do zero"))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode plano = json.readTree(criado.getResponse().getContentAsString());

        assertThat(plano.get("status").asText()).isEqualTo("ACTIVE");
        assertThat(plano.get("assunto").asText()).isEqualTo("Java");
        assertThat(plano.get("nodes").size()).isEqualTo(4);
    }

    @Test
    void assuntoEmBrancoRetorna400() throws Exception {
        mvc.perform(post("/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("assunto", "  "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarBuscaEStatusDePlano() throws Exception {
        String planId = criarPlano("Spring").get("id").asText();

        MvcResult lista = mvc.perform(get("/plans"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json.readTree(lista.getResponse().getContentAsString()).size()).isEqualTo(1);

        mvc.perform(get("/plans/" + planId)).andExpect(status().isOk());

        mvc.perform(get("/plans/" + UUID.randomUUID())).andExpect(status().isNotFound());

        MvcResult atualizado = mvc.perform(patch("/plans/" + planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("status", "pausado"))))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json.readTree(atualizado.getResponse().getContentAsString()).get("status").asText())
                .isEqualTo("ARCHIVED");

        mvc.perform(patch("/plans/" + planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("status", "explodindo"))))
                .andExpect(status().isBadRequest());
    }

    private JsonNode criarPlano(String assunto) throws Exception {
        MvcResult r = mvc.perform(post("/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("assunto", assunto, "descricao", "Do zero"))))
                .andExpect(status().isCreated())
                .andReturn();
        return json.readTree(r.getResponse().getContentAsString());
    }
}
