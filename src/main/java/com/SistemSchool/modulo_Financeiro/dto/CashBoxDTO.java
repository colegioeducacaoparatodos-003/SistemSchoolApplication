package com.SistemSchool.modulo_Financeiro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.SistemSchool.modulo_Financeiro.io.CashBoxStatus;
import com.SistemSchool.modulo_Financeiro.model.CashBox;

public class CashBoxDTO {

    private Long phCashBox;

    private String cashBoxNumber;

    private LocalDate openingDate;

    private LocalDate closingDate;

    private BigDecimal openingBalance;

    private BigDecimal closingBalance;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    /**
     * Saldo atual
     *
     * Entrada - Saída + Inicial
     */
    private BigDecimal currentBalance;

    /**
     * Responsável pelo caixa
     */
    private String operator;

    private CashBoxStatus status;

    private String observation;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public CashBoxDTO() {

    }

    /**
     * Construtor reduzido usado pela query de listagem
     * (CashBoxRepository#findAllCashBoxesDTO). Não calcula totalIncome /
     * totalExpense / currentBalance a partir dos movimentos financeiros
     * (isso é feito na query nativa de lazy loading da tabela); aqui eles
     * ficam com valores padrão, já que essa listagem é usada apenas para
     * estatísticas e para localizar um caixa por id (editar/excluir/fechar).
     */
    public CashBoxDTO(
            Long phCashBox,
            String cashBoxNumber,
            BigDecimal openingBalance,
            BigDecimal closingBalance,
            String operator,
            CashBoxStatus status,
            LocalDate openingDate,
            LocalDate closingDate,
            String observation,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.phCashBox = phCashBox;
        this.cashBoxNumber = cashBoxNumber;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.operator = operator;
        this.status = status;
        this.openingDate = openingDate;
        this.closingDate = closingDate;
        this.observation = observation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

        this.totalIncome = BigDecimal.ZERO;
        this.totalExpense = BigDecimal.ZERO;
        this.currentBalance = openingBalance;
    }

    public CashBoxDTO(
            Long phCashBox,
            String cashBoxNumber,
            LocalDate openingDate,
            LocalDate closingDate,
            BigDecimal openingBalance,
            BigDecimal closingBalance,
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal currentBalance,
            String operator,
            CashBoxStatus status,
            String observation,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.phCashBox = phCashBox;

        this.cashBoxNumber = cashBoxNumber;

        this.openingDate = openingDate;

        this.closingDate = closingDate;

        this.openingBalance = openingBalance;

        this.closingBalance = closingBalance;

        this.totalIncome = totalIncome;

        this.totalExpense = totalExpense;

        this.currentBalance = currentBalance;

        this.operator = operator;

        this.status = status;

        this.observation = observation;

        this.createdAt = createdAt;

        this.updatedAt = updatedAt;

    }

    // ==================================================
    // ENTITY -> DTO
    // ==================================================

    public static CashBoxDTO fromEntity(CashBox cashBox) {

        BigDecimal balance = BigDecimal.ZERO;

        if (cashBox.getOpeningBalance() != null) {

            balance = balance.add(
                    cashBox.getOpeningBalance());

        }

        if (cashBox.getTotalIncome() != null) {

            balance = balance.add(
                    cashBox.getTotalIncome());

        }

        if (cashBox.getTotalExpense() != null) {

            balance = balance.subtract(
                    cashBox.getTotalExpense());

        }

        return new CashBoxDTO(

                cashBox.getPhCashBox(),

                cashBox.getCashBoxNumber(),

                cashBox.getOpeningDate(),

                cashBox.getClosingDate(),

                cashBox.getOpeningBalance(),

                cashBox.getClosingBalance(),

                cashBox.getTotalIncome(),

                cashBox.getTotalExpense(),

                balance,

                cashBox.getOperator(),

                cashBox.getStatus(),

                cashBox.getObservation(),

                cashBox.getCreatedAt(),

                cashBox.getUpdatedAt()

        );

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

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}