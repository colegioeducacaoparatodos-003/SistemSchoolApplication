package com.SistemSchool.modulo_Financeiro.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;


import com.SistemSchool.modulo_Financeiro.io.MovementStatus;
import com.SistemSchool.modulo_Financeiro.io.MovementType;
import com.SistemSchool.modulo_secrtaria.model.Pagamento;

import jakarta.persistence.*;

@Entity
@Table(name = "financial_movement")
public class FinancialMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long phMovement;

    @Column(nullable = false, unique = true)
    private String movementNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cash_box_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_movement_cash_box"))
    private CashBox cashBox;


    /**
     * Pagamento (modulo_secrtaria) relacionado — usado pelo fluxo de
     * propinas/matrículas da secretaria, sem Invoice/Receipt.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagamento_pk", foreignKey = @ForeignKey(name = "fk_movement_pagamento"))
    private Pagamento pagamento;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private MovementType type;

    @Enumerated(EnumType.STRING)
    private MovementStatus status;

    private String category;

    private String responsible;

    private String observation;

    private LocalDateTime movementDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.movementDate == null) {
            this.movementDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = MovementStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public FinancialMovement() {
    }

    public Long getPhMovement() {
        return phMovement;
    }

    public void setPhMovement(Long phMovement) {
        this.phMovement = phMovement;
    }

    public String getMovementNumber() {
        return movementNumber;
    }

    public void setMovementNumber(String movementNumber) {
        this.movementNumber = movementNumber;
    }

    public CashBox getCashBox() {
        return cashBox;
    }

    public void setCashBox(CashBox cashBox) {
        this.cashBox = cashBox;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public MovementStatus getStatus() {
        return status;
    }

    public void setStatus(MovementStatus status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof FinancialMovement))
            return false;
        FinancialMovement other = (FinancialMovement) obj;
        return Objects.equals(phMovement, other.phMovement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phMovement);
    }

    @Override
    public String toString() {
        return movementNumber;
    }
}