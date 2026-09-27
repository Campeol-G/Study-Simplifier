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
@Table(name = "attempts")
public class AttemptEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_node_id", nullable = false)
    private PlanNodeEntity planNode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "pontuacao")
    private Double pontuacao;

    @Column(name = "concluido_em", nullable = false)
    private Instant concluidoEm;

    protected AttemptEntity() {
    }

    public AttemptEntity(PlanNodeEntity planNode, UserEntity user, Double pontuacao) {
        this.planNode = planNode;
        this.user = user;
        this.pontuacao = pontuacao;
        this.concluidoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public PlanNodeEntity getPlanNode() {
        return planNode;
    }

    public UserEntity getUser() {
        return user;
    }

    public Double getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(Double pontuacao) {
        this.pontuacao = pontuacao;
    }

    public Instant getConcluidoEm() {
        return concluidoEm;
    }
}
