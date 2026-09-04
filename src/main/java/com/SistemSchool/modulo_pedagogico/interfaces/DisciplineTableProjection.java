package com.SistemSchool.modulo_pedagogico.interfaces;

import java.time.LocalDateTime;

public interface DisciplineTableProjection {
    Long getPkDiscipline();

    String getDisciplineCode();

    String getDisciplineName();

    String getDescription();

    String getStatus();

    String getObs();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}