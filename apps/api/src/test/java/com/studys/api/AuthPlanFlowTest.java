package com.studys.api;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class AuthPlanFlowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private String email() {
        return "user-" + UUID.randomUUID() + "@teste.com";
    }

    private String registrar(String email) throws Exception {
        MvcResult r = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("nome", "Teste", "email", email, "senha", "segredo123"))))
                .andExpect(status().isCreated())
                .andReturn();
        return r.getResponse().getContentAsString();
    }

    private String logar(String email) throws Exception {
        MvcResult r = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "senha", "segredo123"))))
                .andExpect(status().isOk())
                .andReturn();
        return json.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void fluxoCompleto() throws Exception {
        String emailA = email();
        registrar(emailA);

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("nome", "Dup", "email", emailA, "senha", "segredo123"))))
                .andExpect(status().isConflict());

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", emailA, "senha", "errada123"))))
                .andExpect(status().isUnauthorized());

        String tokenA = logar(emailA);

        mvc.perform(get("/plans")).andExpect(status().isUnauthorized());

        MvcResult criado = mvc.perform(post("/plans")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("assunto", "Java", "descricao", "Do zero"))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode plano = json.readTree(criado.getResponse().getContentAsString());
        String planId = plano.get("id").asText();
        assertThat(plano.get("status").asText()).isEqualTo("ACTIVE");

        mvc.perform(post("/plans")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("assunto", "  "))))
                .andExpect(status().isBadRequest());

        MvcResult lista = mvc.perform(get("/plans").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json.readTree(lista.getResponse().getContentAsString()).size()).isEqualTo(1);

        mvc.perform(get("/plans/" + planId).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mvc.perform(get("/plans/" + UUID.randomUUID()).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());

        MvcResult atualizado = mvc.perform(patch("/plans/" + planId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("status", "pausado"))))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json.readTree(atualizado.getResponse().getContentAsString()).get("status").asText())
                .isEqualTo("ARCHIVED");

        mvc.perform(patch("/plans/" + planId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("status", "explodindo"))))
                .andExpect(status().isBadRequest());

        String emailB = email();
        registrar(emailB);
        String tokenB = logar(emailB);

        MvcResult listaB = mvc.perform(get("/plans").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(json.readTree(listaB.getResponse().getContentAsString()).size()).isEqualTo(0);

        mvc.perform(get("/plans/" + planId).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        mvc.perform(patch("/plans/" + planId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("status", "concluído"))))
                .andExpect(status().isNotFound());
    }
}
