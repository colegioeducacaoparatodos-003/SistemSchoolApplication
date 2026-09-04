package com.SistemSchool.modulo_pedagogico.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.SistemSchool.modulo_pedagogico.io.WeekDay;
import com.SistemSchool.modulo_pedagogico.model.Schedule;

public class ScheduleDTO {

    private Long pkSchedule;
    private Long disciplinePk;
    private String disciplineName;
    private Long schoolClassPk;
    private String schoolClassName;
    private String schoolClassCode;
    private Long teacherPk;
    private String teacherName;
    private WeekDay weekDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private String anoLectivo;
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ScheduleDTO() {}

    public ScheduleDTO(Long pkSchedule, Long disciplinePk, String disciplineName, Long schoolClassPk,
                       String schoolClassName, String schoolClassCode, Long teacherPk, String teacherName,
                       WeekDay weekDay, LocalTime startTime, LocalTime endTime, String anoLectivo, String obs,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkSchedule = pkSchedule;
        this.disciplinePk = disciplinePk;
        this.disciplineName = disciplineName;
        this.schoolClassPk = schoolClassPk;
        this.schoolClassName = schoolClassName;
        this.schoolClassCode = schoolClassCode;
        this.teacherPk = teacherPk;
        this.teacherName = teacherName;
        this.weekDay = weekDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.anoLectivo = anoLectivo;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ScheduleDTO fromEntity(Schedule s) {
        return new ScheduleDTO(
            s.getPkSchedule(),
            s.getDiscipline() != null ? s.getDiscipline().getPkDiscipline() : null,
            s.getDiscipline() != null ? s.getDiscipline().getDisciplineName() : null,
            s.getSchoolClass() != null ? s.getSchoolClass().getPkSchoolClass() : null,
            s.getSchoolClass() != null ? s.getSchoolClass().getClassName() : null,
            s.getSchoolClass() != null ? s.getSchoolClass().getClassCode() : null,
            s.getTeacher() != null ? s.getTeacher().getPkTeacher() : null,
            s.getTeacher() != null ? s.getTeacher().getDisplayName() : null,
            s.getWeekDay(), s.getStartTime(), s.getEndTime(),
            s.getAnoLectivo(), s.getObs(), s.getCreatedAt(), s.getUpdatedAt()
        );
    }

    // Getters e Setters
    public Long getPkSchedule() { return pkSchedule; }
    public void setPkSchedule(Long pkSchedule) { this.pkSchedule = pkSchedule; }

    public Long getDisciplinePk() { return disciplinePk; }
    public void setDisciplinePk(Long disciplinePk) { this.disciplinePk = disciplinePk; }

    public String getDisciplineName() { return disciplineName; }
    public void setDisciplineName(String disciplineName) { this.disciplineName = disciplineName; }

    public Long getSchoolClassPk() { return schoolClassPk; }
    public void setSchoolClassPk(Long schoolClassPk) { this.schoolClassPk = schoolClassPk; }

    public String getSchoolClassName() { return schoolClassName; }
    public void setSchoolClassName(String schoolClassName) { this.schoolClassName = schoolClassName; }

    public String getSchoolClassCode() { return schoolClassCode; }
    public void setSchoolClassCode(String schoolClassCode) { this.schoolClassCode = schoolClassCode; }

    public Long getTeacherPk() { return teacherPk; }
    public void setTeacherPk(Long teacherPk) { this.teacherPk = teacherPk; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public WeekDay getWeekDay() { return weekDay; }
    public void setWeekDay(WeekDay weekDay) { this.weekDay = weekDay; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getAnoLectivo() { return anoLectivo; }
    public void setAnoLectivo(String anoLectivo) { this.anoLectivo = anoLectivo; }

    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}