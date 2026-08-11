package com.SistemSchool.modulo_secrtaria.interfaces;

import java.time.LocalDateTime;

public interface SchoolClassTableProjection {

    Long getPkSchoolClass();

    String getClassCode();

    String getClassName();

    String getClasse();

    String getTurno();

    String getAnoLectivo();

    Integer getCapacidade();

    String getRoom();

    String getStatus();

    String getObs();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}