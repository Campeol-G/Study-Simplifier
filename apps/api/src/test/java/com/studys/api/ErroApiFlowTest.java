package com.studys.api;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Formato de erro da API para erro de cliente: o corpo e sempre {@code {"mensagem": ...}}, nunca o
 * formato padronizado do Spring Boot.
 *
 * <p>O caso de 500 com excecao inesperada mora em {@link ErroApi500Test}, que precisa de mock.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ErroApiFlowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void jsonMalformadoRetorna400Padronizado() throws Exception {
        MvcResult r = mvc.perform(post("/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assunto\": "))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertCorpoPadronizado(r, "malformed JSON");
    }

    @Test
    void uuidInvalidoNoPathRetorna400Padronizado() throws Exception {
        MvcResult r = mvc.perform(get("/plans/nao-e-uuid"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertCorpoPadronizado(r, "UUID inválido no path");
    }

    @Test
    void rotaInexistenteRetorna404Padronizado() throws Exception {
        MvcResult r = mvc.perform(get("/rota-que-nao-existe"))
                .andExpect(status().isNotFound())
                .andReturn();

        assertCorpoPadronizado(r, "rota inexistente");
    }

    @Test
    void metodoNaoSuportadoRetorna405Padronizado() throws Exception {
        MvcResult r = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/plans/" + UUID.randomUUID()))
                .andExpect(status().isMethodNotAllowed())
                .andReturn();

        assertCorpoPadronizado(r, "método não suportado");
    }

    @Test
    void assuntoEmBrancoRetorna400ComMensagemUtil() throws Exception {
        MvcResult r = mvc.perform(post("/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("assunto", "  "))))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(json.readTree(r.getResponse().getContentAsString()).get("mensagem").asText())
                .isEqualTo("Assunto é obrigatório");
    }

    @Test
    void planoInexistenteRetorna404Padronizado() throws Exception {
        MvcResult r = mvc.perform(get("/plans/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andReturn();

        assertCorpoPadronizado(r, "plano inexistente");
    }

    private void assertCorpoPadronizado(MvcResult r, String contexto) throws Exception {
        String corpo = r.getResponse().getContentAsString();
        JsonNode node = json.readTree(corpo);
        assertThat(node.isObject()).as("corpo de %s deveria ser um objeto: %s", contexto, corpo).isTrue();
        assertThat(node.propertyNames()).as("chaves de %s", contexto).containsExactly("mensagem");
        assertThat(node.get("mensagem").asText()).as("mensagem de %s", contexto).isNotBlank();
    }
}
