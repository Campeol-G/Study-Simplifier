package com.studys.api;

import java.util.UUID;

import com.studys.plans.PlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * O fallback de 500 nao pode vazar detalhe interno, mas tambem nao pode virar um aspirador: um
 * handler de {@code Exception} captura tambem as excecoes do framework que ja carregam status de
 * cliente, e transformar erro de cliente em 500 seria pior do que o problema que ele resolve.
 *
 * <p>A classe e separada de {@link ErroApiFlowTest} porque o {@code @MockitoBean} vale para a classe
 * inteira: com {@code PlanService} mockado, os cenarios de plano real da outra classe deixariam de
 * testar o servico.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ErroApi500Test {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockitoBean
    private PlanService plans;

    @Test
    void excecaoInesperadaRetorna500PadronizadoSemVazarDetalhe() throws Exception {
        doThrow(new RuntimeException("boom: segredo de banco em jdbc:sqlite:/segredo"))
                .when(plans).buscar(any(UUID.class));

        MvcResult r = mvc.perform(get("/plans/" + UUID.randomUUID()))
                .andExpect(status().isInternalServerError())
                .andReturn();

        String corpo = r.getResponse().getContentAsString();
        assertThat(json.readTree(corpo).get("mensagem").asText()).isEqualTo("Erro interno");
        assertThat(corpo)
                .as("nem mensagem da exceção, nem nome de classe, nem stack trace podem chegar ao cliente")
                .doesNotContain("boom")
                .doesNotContain("segredo")
                .doesNotContain("jdbc:sqlite")
                .doesNotContain("RuntimeException")
                .doesNotContain("org.springframework")
                .doesNotContain("at com.studys");
    }
}
