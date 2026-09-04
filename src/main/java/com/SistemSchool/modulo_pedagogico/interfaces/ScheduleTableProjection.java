package com.SistemSchool.modulo_pedagogico.interfaces;

import java.time.LocalDateTime;
import java.time.LocalTime;

public interface ScheduleTableProjection {
    Long getPkSchedule();
    Long getDisciplinePk();
    String getDisciplineName();
    Long getSchoolclassPk();
    String getSchoolclassnome();
    String getSchoolclasscode();
    Long getTeacherPk();
    String getTeacherName();
    String getWeekDay();
    LocalTime getStartTime();
    LocalTime getEndTime();
    String getAnoLectivo();
    String getObs();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}