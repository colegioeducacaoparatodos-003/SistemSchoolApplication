package com.SistemSchool.modulo_pedagogico.interfaces;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface EvaluationTableProjection {
    Long getPkEvaluation();
    Long getDisciplinePk();
    String getDisciplineName();
    String getEvaluationName();
    String getEvaluationType();
    String getTrimester();
    LocalDate getEvaluationDate();
    String getAnoLectivo();
    String getObs();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}