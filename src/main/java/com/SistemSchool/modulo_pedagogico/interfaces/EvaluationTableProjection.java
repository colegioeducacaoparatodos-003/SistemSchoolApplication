package com.SistemSchool.modulo_pedagogico.interfaces;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface EvaluationTableProjection {

    Long getPkEvaluation();

    Long getDisciplinePk();

    String getDisciplineName();

    Long getSchedulePk();

    String getScheduleWeekDay();

    String getTitle();

    String getType();

    Double getWeight();

    LocalDate getEvaluationDate();

    String getStatus();

    Integer getTrimester();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}