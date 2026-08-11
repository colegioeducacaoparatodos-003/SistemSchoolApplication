package com.SistemSchool.modulo_Financeiro.interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FeeTableProjection {

    Long getPhFee();
    String getFeeCode();
    String getDescription();
    String getFeeType();               // <-- adicionado

    Long getSchoolClassPk();
    String getSchoolClassName();
    Integer getSchoolYear();
    BigDecimal getAmount();
    LocalDateTime getStartDate();
    LocalDateTime getEndDate();
    String getStatus();
    String getObs();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}