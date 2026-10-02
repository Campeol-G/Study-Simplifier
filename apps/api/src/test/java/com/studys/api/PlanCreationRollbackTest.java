package com.studys.api;

import java.util.Map;
import java.util.UUID;

import com.studys.db.repository.PlanRepository;
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

/**
 * Rollback transacional de "plano + arvore": o teste mais valioso da suite, e por isso o unico que
 * nao e {@code @Transactional}. Se estivesse transacional, o rollback do proprio teste mascararia
 * exatamente o que ele quer provar.
 *
 * <p>A limpeza no {@code finally} tambem e manual, pela mesma razao.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PlanCreationRollbackTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private PlanRepository plans;

    @MockitoBean
    private PlanEngineService engine;

    @Test
    void falhaNaEngineNaoDeixaPlanoSemArvore() throws Exception {
        long antes = plans.count();

        doThrow(new IllegalStateException("engine fora do ar")).when(engine).gerarArvoreEstatica(any(UUID.class));

        try {
            assertThatThrownBy(() -> mvc.perform(post("/plans")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(Map.of("assunto", "Rollback", "descricao", "sem arvore")))))
                    .hasRootCauseInstanceOf(IllegalStateException.class);

            assertThat(plans.count()).as("o plano nao pode sobrar sem a arvore").isEqualTo(antes);
        } finally {
            plans.deleteAll();
        }
    }
}
