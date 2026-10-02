package com.studys.api;

import java.util.Map;
import java.util.UUID;

import com.studys.db.repository.PlanRepository;
import com.studys.db.repository.UserRepository;
import com.studys.planengine.PlanEngineService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlanCreationRollbackTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private PlanRepository plans;

    @Autowired
    private UserRepository users;

    @MockitoBean
    private PlanEngineService engine;

    @Test
    void falhaNaEngineNaoDeixaPlanoSemArvore() throws Exception {
        String email = "rollback-" + UUID.randomUUID() + "@teste.com";
        MvcResult registro = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("nome", "Teste", "email", email, "senha", "segredo123"))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode user = json.readTree(registro.getResponse().getContentAsString());
        UUID userId = UUID.fromString(user.get("id").asText());

        MvcResult login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "senha", "segredo123"))))
                .andExpect(status().isOk())
                .andReturn();
        String token = json.readTree(login.getResponse().getContentAsString()).get("token").asText();

        doThrow(new IllegalStateException("engine fora do ar")).when(engine).gerarArvoreEstatica(any(UUID.class));

        try {
            assertThatThrownBy(() -> mvc.perform(post("/plans")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(Map.of("assunto", "Rollback", "descricao", "sem arvore")))))
                    .hasRootCauseInstanceOf(IllegalStateException.class);

            assertThat(plans.findByUserId(userId)).isEmpty();
        } finally {
            users.deleteById(userId);
        }
    }
}
