package com.studys.db.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SessaoEstudoTest {

    private static final Instant T0 = Instant.parse("2026-03-01T12:00:00Z");

    private static SessaoEstudoEntity sessaoSobre(PlanNodeEntity no) {
        return new SessaoEstudoEntity(no, T0);
    }

    private static PlanNodeEntity no() {
        return new PlanNodeEntity(null, null, "Ownership", 0, "TODO");
    }

    @Test
    @DisplayName("creditar soma na sessao e no total do no")
    void creditarAcumulaNaSessaoENoNo() {
        PlanNodeEntity no = no();
        SessaoEstudoEntity sessao = sessaoSobre(no);

        sessao.creditar(30, T0.plusSeconds(30));
        sessao.creditar(45, T0.plusSeconds(75));

        assertThat(sessao.getSegundosAcumulados()).isEqualTo(75);
        assertThat(no.getSegundosEstudados())
                .as("o total do no e a soma das sessoes, entao tem de acompanhar")
                .isEqualTo(75);
        assertThat(sessao.getUltimaAtividadeEm())
                .as("a ultima atividade e o instante do ultimo batimento, nao o fim da sessao")
                .isEqualTo(T0.plusSeconds(75));
    }

    @Test
    @DisplayName("batimento com zero ou negativo nao credita nada: ninguem ganha tempo de gra")
    void creditarNaoNegativoNaoCredita() {
        PlanNodeEntity no = no();
        SessaoEstudoEntity sessao = sessaoSobre(no);

        assertThatThrownBy(() -> sessao.creditar(0, T0.plusSeconds(10)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sessao.creditar(-30, T0.plusSeconds(10)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(sessao.getSegundosAcumulados()).isZero();
        assertThat(no.getSegundosEstudados()).isZero();
    }

    @Test
    @DisplayName("progresso marca o instante e move a ultima atividade, para zerar a inatividade")
    void progressoMarcaInstanteEAtividade() {
        SessaoEstudoEntity sessao = sessaoSobre(no());
        Instant quando = T0.plusSeconds(120);

        assertThat(sessao.getUltimoProgressoEm())
                .as("nulo significa 'nenhum progresso ainda', que e diferente de zero")
                .isNull();

        sessao.registrarProgresso(quando);

        assertThat(sessao.getUltimoProgressoEm()).isEqualTo(quando);
        assertThat(sessao.getUltimaAtividadeEm()).isEqualTo(quando);
    }

    @Test
    @DisplayName("uma sessao em andamento nao tem motivo de fim; fechar grava o motivo")
    void motivoFimSoExisteDepoisDeFechar() {
        SessaoEstudoEntity sessao = sessaoSobre(no());
        assertThat(sessao.getMotivoFim()).as("sessao aberta ainda nao parou").isNull();

        sessao.fechar("INATIVIDADE", T0.plusSeconds(600));

        assertThat(sessao.getMotivoFim()).isEqualTo("INATIVIDADE");
        assertThat(sessao.getUltimaAtividadeEm()).isEqualTo(T0.plusSeconds(600));
        assertThat(sessao.getSegundosAcumulados())
                .as("fechar nao inventa tempo: o que foi creditado e o que conta")
                .isZero();
    }

    @Test
    @DisplayName("midia ativa e o override: o estado e guardado, nao aplicado aqui")
    void midiaAtivaFicaGravada() {
        SessaoEstudoEntity sessao = sessaoSobre(no());

        sessao.definirMidiaAtiva(true);
        assertThat(sessao.isMidiaAtiva()).isTrue();

        sessao.definirMidiaAtiva(false);
        assertThat(sessao.isMidiaAtiva()).isFalse();
    }
}
