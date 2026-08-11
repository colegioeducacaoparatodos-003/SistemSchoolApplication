package com.SistemSchool.modulo_pedagogico.interfaces;

import java.time.LocalDateTime;

public interface DisciplineTableProjection {

    Long getPkDiscipline();

    String getDisciplineCode();

    String getDisciplineName();

    Integer getWorkload();

    String getStatus();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}