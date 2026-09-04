package com.SistemSchool.modulo_Recursoa_Humano.interfaces;

import java.time.LocalDateTime;

public interface TeacherTableProjection {

    Long getPkTeacher();

    String getTeacherNumber();

    String getFristName();

    String getLastName();

    String getQualificationLivel();

    String getContractType();

    String getStatus();

    String getPhotoPhath();

    String getEmail();

    String getPhone();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

}