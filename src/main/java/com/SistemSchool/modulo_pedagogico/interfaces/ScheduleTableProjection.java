package com.SistemSchool.modulo_pedagogico.interfaces;

import java.time.LocalDateTime;
import java.time.LocalTime;

public interface ScheduleTableProjection {

    Long getPkSchedule();

    Long getTeacherPk();

    String getTeacherName();

    Long getDisciplinePk();

    String getDisciplineName();

    Long getSchoolClassPk();

    String getSchoolClassName();

    String getWeekDay();

    LocalTime getStartTime();

    LocalTime getEndTime();

    String getClassroom();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}