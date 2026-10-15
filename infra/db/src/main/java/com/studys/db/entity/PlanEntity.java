package com.studys.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "plans")
public class PlanEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "assunto", nullable = false, length = 200)
    private String assunto;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "descricao")
    private String descricao;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "nivel_inicial", length = 30)
    private String nivelInicial;

    @Column(name = "nivel_atual", length = 30)
    private String nivelAtual;

    @Column(name = "horas_por_semana")
    private Integer horasPorSemana;

    @Column(name = "duracao_semanas")
    private Integer duracaoSemanas;

    @Column(name = "prazo_pedido_semanas")
    private Integer prazoPedidoSemanas;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "restricoes")
    private String restricoes;

    protected PlanEntity() {
    }

    public PlanEntity(String assunto, String status) {
        this.assunto = assunto;
        this.status = status;
        this.criadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public String getNivelInicial() {
        return nivelInicial;
    }

    public void setNivelInicial(String nivelInicial) {
        this.nivelInicial = nivelInicial;
    }

    public String getNivelAtual() {
        return nivelAtual;
    }

    public void setNivelAtual(String nivelAtual) {
        this.nivelAtual = nivelAtual;
    }

    public Integer getHorasPorSemana() {
        return horasPorSemana;
    }

    public void setHorasPorSemana(Integer horasPorSemana) {
        this.horasPorSemana = horasPorSemana;
    }

    public Integer getDuracaoSemanas() {
        return duracaoSemanas;
    }

    public void setDuracaoSemanas(Integer duracaoSemanas) {
        this.duracaoSemanas = duracaoSemanas;
    }

    public Integer getPrazoPedidoSemanas() {
        return prazoPedidoSemanas;
    }

    public void setPrazoPedidoSemanas(Integer prazoPedidoSemanas) {
        this.prazoPedidoSemanas = prazoPedidoSemanas;
    }

    public String getRestricoes() {
        return restricoes;
    }

    public void setRestricoes(String restricoes) {
        this.restricoes = restricoes;
    }
}
