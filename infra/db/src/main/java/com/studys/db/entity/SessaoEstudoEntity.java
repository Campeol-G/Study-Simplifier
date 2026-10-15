package com.studys.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * Uma sessao de estudo sobre um no, com o detalhamento que a regra de parada exige.
 *
 * <p>O tempo total por no mora em {@code plan_nodes.segundos_estudados}; aqui fica o detailed
 * balanco: por que a sessao parou e quando houve progresso pela ultima vez. A regra de contagem e
 * dupla — sem input do sistema E sem progresso no app — e por isso que esta tabela precisa guardar as
 * duas metades da evidencia separadamente.
 *
 * <p>{@code ultimoProgressoEm} e anulavel de proposito: null significa "nenhum progresso ainda nesta
 * sessao", que e diferente de zero. {@code motivoFim} e anulavel porque a sessao em andamento nao
 * tem motivo de fim ainda.
 *
 * <p>{@code midiaAtiva} e o override: com video rodando a contagem nao pausa, porque quem assiste a
 * video nao produz input nem muda status, e sem isso a regra de inatividade cortaria exatamente o
 * conteudo que a pessoa esta usando para estudar.
 */
@Entity
@Table(name = "sessoes_estudo")
public class SessaoEstudoEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_node_id", nullable = false)
    private PlanNodeEntity no;

    @Column(name = "iniciada_em", nullable = false)
    private Instant iniciadaEm;

    @Column(name = "ultima_atividade_em", nullable = false)
    private Instant ultimaAtividadeEm;

    @Column(name = "ultimo_progresso_em")
    private Instant ultimoProgressoEm;

    @Column(name = "segundos_acumulados", nullable = false)
    private int segundosAcumulados;

    @Column(name = "midia_ativa", nullable = false)
    private boolean midiaAtiva;

    @Column(name = "motivo_fim", length = 30)
    private String motivoFim;

    protected SessaoEstudoEntity() {
    }

    public SessaoEstudoEntity(PlanNodeEntity no, Instant iniciadaEm) {
        this.no = no;
        this.iniciadaEm = iniciadaEm;
        this.ultimaAtividadeEm = iniciadaEm;
    }

    /**
     * Credita tempo por um batimento, que e o intervalo entre dois sinais de que ha contagem.
     *
     * <p>O instante chega como argumento em vez de {@code Instant.now()} dentro do metodo: a
     * decisao de creditar ja foi tomada antes — por quem sabe, e talvez nem seja o relogio do
     * servidor — e a sessao so registra. O {@code no} e creditado junto, porque o total por no e a
     * soma das sessoes e nao um contador guardado a parte.
     */
    public void creditar(int segundos, Instant quando) {
        if (segundos <= 0) {
            throw new IllegalArgumentException("segundos a creditar tem de ser positivo: " + segundos);
        }
        this.segundosAcumulados += segundos;
        this.ultimaAtividadeEm = quando;
        this.no.addSegundosEstudados(segundos);
    }

    /** Registra que o usuario avancou de fato, o que zera a contagem de inatividade por progresso. */
    public void registrarProgresso(Instant quando) {
        this.ultimoProgressoEm = quando;
        this.ultimaAtividadeEm = quando;
    }

    public void definirMidiaAtiva(boolean ativa) {
        this.midiaAtiva = ativa;
    }

    public void fechar(String motivoFim, Instant quando) {
        this.motivoFim = motivoFim;
        this.ultimaAtividadeEm = quando;
    }

    public UUID getId() {
        return id;
    }

    public PlanNodeEntity getNo() {
        return no;
    }

    public Instant getIniciadaEm() {
        return iniciadaEm;
    }

    public Instant getUltimaAtividadeEm() {
        return ultimaAtividadeEm;
    }

    public Instant getUltimoProgressoEm() {
        return ultimoProgressoEm;
    }

    public int getSegundosAcumulados() {
        return segundosAcumulados;
    }

    public boolean isMidiaAtiva() {
        return midiaAtiva;
    }

    public String getMotivoFim() {
        return motivoFim;
    }
}
