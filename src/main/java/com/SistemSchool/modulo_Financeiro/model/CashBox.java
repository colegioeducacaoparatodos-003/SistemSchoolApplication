package com.SistemSchool.modulo_Financeiro.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import com.SistemSchool.modulo_Financeiro.io.CashBoxStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "cash_box")
public class CashBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long phCashBox;

    /**
     * Número do caixa
     * Ex: CX-2026-001
     */
    @Column(nullable = false, unique = true)
    private String cashBoxNumber;

    /**
     * Data de abertura
     */
    private LocalDate openingDate;

    /**
     * Data de fechamento
     */
    private LocalDate closingDate;

    /**
     * Valor inicial do caixa
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal openingBalance;

    /**
     * Valor final no fechamento
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal closingBalance;

    /**
     * Total recebido
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal totalIncome;

    /**
     * Total de saída
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal totalExpense;

    /**
     * Funcionário responsável
     * Futuramente pode ligar com User/RH
     */
    private String operator;

    @Enumerated(EnumType.STRING)
    private CashBoxStatus status;

    private String observation;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();

        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {

            this.status = CashBoxStatus.OPEN;

        }

        if (this.openingDate == null) {

            this.openingDate = LocalDate.now();

        }

        if (this.openingBalance == null) {

            this.openingBalance = BigDecimal.ZERO;

        }

        if (this.totalIncome == null) {

            this.totalIncome = BigDecimal.ZERO;

        }

        if (this.totalExpense == null) {

            this.totalExpense = BigDecimal.ZERO;

        }

    }

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();

    }

    public CashBox() {

    }

    public Long getPhCashBox() {
        return phCashBox;
    }

    public void setPhCashBox(Long phCashBox) {
        this.phCashBox = phCashBox;
    }

    public String getCashBoxNumber() {
        return cashBoxNumber;
    }

    public void setCashBoxNumber(String cashBoxNumber) {
        this.cashBoxNumber = cashBoxNumber;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(LocalDate openingDate) {
        this.openingDate = openingDate;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public CashBoxStatus getStatus() {
        return status;
    }

    public void setStatus(CashBoxStatus status) {
        this.status = status;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (!(obj instanceof CashBox))
            return false;

        CashBox other = (CashBox) obj;

        return Objects.equals(phCashBox, other.phCashBox);

    }

    @Override
    public int hashCode() {

        return Objects.hash(phCashBox);

    }

    @Override
    public String toString() {

        return cashBoxNumber;

    }

}