package com.SistemSchool.modulo_Financeiro.interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FinancialMovementTableProjection {

    Long getPhMovement();

    /**
     * Número do movimento
     *
     * Ex:
     * MOV-2026-00001
     */
    String getMovementNumber();

    /**
     * Caixa onde ocorreu o movimento
     */
    Long getCashBoxPk();

    String getCashBoxNumber();

    /**
     * Pagamento relacionado (modulo_secrtaria.Pagamento)
     *
     * Ex:
     * Pagamento de propina
     */
    Long getPagamentoPk();

    String getPagamentoNumeroDocumento();

    /**
     * Descrição do movimento
     */
    String getDescription();

    /**
     * Valor movimentado
     */
    BigDecimal getAmount();

    /**
     * Tipo:
     *
     * INCOME - Entrada
     * EXPENSE - Saída
     */
    String getType();

    /**
     * Categoria:
     *
     * PROPINA
     * SALARIO
     * MATERIAL
     * OUTROS
     */
    String getCategory();

    /**
     * Estado:
     *
     * ACTIVE
     * INACTIVE
     * CANCELLED
     */
    String getStatus();

    /**
     * Responsável pelo lançamento
     */
    String getResponsible();

    /**
     * Observação
     */
    String getObservation();

    /**
     * Data do movimento
     */
    LocalDateTime getMovementDate();

    /**
     * Auditoria
     */
    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

}