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

@Entity
@Table(name = "plans")
public class PlanEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "assunto", nullable = false, length = 200)
    private String assunto;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected PlanEntity() {
    }

    public PlanEntity(UserEntity user, String assunto, String status) {
        this.user = user;
        this.assunto = assunto;
        this.status = status;
        this.criadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
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
}
