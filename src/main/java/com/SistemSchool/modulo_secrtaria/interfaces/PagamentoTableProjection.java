package com.SistemSchool.modulo_secrtaria.interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PagamentoTableProjection {

    Long getPkPagamento();

    String getNumeroDocumento();

    /**
     * Matrícula
     */
    Long getEnrolmentPk();

    String getEnrolmentNumero();

    String getStudentName();

    /**
     * Propina
     */
    Long getFeePk();

    /**
     * Caixa
     */
    Long getCashBoxPk();

    String getCashBoxNumber();

    BigDecimal getValor();

    BigDecimal getMulta();

    BigDecimal getTotal();

    LocalDateTime getDataEmissao();

    LocalDateTime getDataPagamento();

    String getFormaPagamento();

    String getEstado();

    String getReferencia();

    String getObservacao();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}