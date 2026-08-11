package com.SistemSchool.modulo_Financeiro.interfaces;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface CashBoxTableProjection {

    Long getPhCashBox();

    /**
     * Número do caixa
     *
     * Ex:
     * CX-2026-00001
     */
    String getCashBoxNumber();

    /**
     * Responsável pelo caixa
     */
    String getOperator();

    /**
     * Valores financeiros
     */
    BigDecimal getOpeningBalance();

    BigDecimal getTotalIncome();

    BigDecimal getTotalExpense();

    /**
     * Saldo atual
     *
     * Inicial + Entradas - Saídas
     */
    BigDecimal getCurrentBalance();

    /**
     * Estado do caixa
     *
     * OPEN
     * CLOSED
     */
    String getStatus();

    /**
     * Datas
     */
    LocalDate getOpeningDate();

    LocalDate getClosingDate();

    /**
     * Auditoria
     */
    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

}