package com.SistemSchool.modulo_pedagogico.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.SistemSchool.modulo_pedagogico.io.WeekDay;
import com.SistemSchool.modulo_pedagogico.model.Schedule;

public class ScheduleDTO {

    private Long pkSchedule;

    // Dados "achatados" do Teacher
    private Long teacherPk;
    private String teacherName;

    // Dados "achatados" da Discipline
    private Long disciplinePk;
    private String disciplineName;

    // Dados "achatados" da SchoolClass
    private Long schoolClassPk;
    private String schoolClassName;

    private WeekDay weekDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private String classroom;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ScheduleDTO() {
    }

    public ScheduleDTO(Long pkSchedule, Long teacherPk, String teacherName,
            Long disciplinePk, String disciplineName,
            Long schoolClassPk, String schoolClassName,
            WeekDay weekDay, LocalTime startTime, LocalTime endTime, String classroom,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkSchedule = pkSchedule;
        this.teacherPk = teacherPk;
        this.teacherName = teacherName;
        this.disciplinePk = disciplinePk;
        this.disciplineName = disciplineName;
        this.schoolClassPk = schoolClassPk;
        this.schoolClassName = schoolClassName;
        this.weekDay = weekDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classroom = classroom;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─────────────────────────────────────────────────────────────
    // CONVERSÃO (schedule → DTO)
    // ─────────────────────────────────────────────────────────────

    public static ScheduleDTO fromEntity(Schedule schedule) {
        return new ScheduleDTO(
                schedule.getPkSchedule(),
                schedule.getTeacher() != null ? schedule.getTeacher().getPkTeacher() : null,
                schedule.getTeacher() != null ? schedule.getTeacher().getDisplayName() : null,
                schedule.getDiscipline() != null ? schedule.getDiscipline().getPkDiscipline() : null,
                schedule.getDiscipline() != null ? schedule.getDiscipline().getDisciplineName() : null,
                schedule.getSchoolClass() != null ? schedule.getSchoolClass().getPkSchoolClass() : null,
                schedule.getSchoolClass() != null ? schedule.getSchoolClass().getClassCode() : null,
                schedule.getWeekDay(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getClassroom(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt());
    }

    public Long getPkSchedule() {
        return this.pkSchedule;
    }

    public void setPkSchedule(Long pkSchedule) {
        this.pkSchedule = pkSchedule;
    }

    public Long getTeacherPk() {
        return this.teacherPk;
    }

    public void setTeacherPk(Long teacherPk) {
        this.teacherPk = teacherPk;
    }

    public String getTeacherName() {
        return this.teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Long getDisciplinePk() {
        return this.disciplinePk;
    }

    public void setDisciplinePk(Long disciplinePk) {
        this.disciplinePk = disciplinePk;
    }

    public String getDisciplineName() {
        return this.disciplineName;
    }

    public void setDisciplineName(String disciplineName) {
        this.disciplineName = disciplineName;
    }

    public Long getSchoolClassPk() {
        return this.schoolClassPk;
    }

    public void setSchoolClassPk(Long schoolClassPk) {
        this.schoolClassPk = schoolClassPk;
    }

    public String getSchoolClassName() {
        return this.schoolClassName;
    }

    public void setSchoolClassName(String schoolClassName) {
        this.schoolClassName = schoolClassName;
    }

    public WeekDay getWeekDay() {
        return this.weekDay;
    }

    public void setWeekDay(WeekDay weekDay) {
        this.weekDay = weekDay;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getClassroom() {
        return this.classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ScheduleDTO pkSchedule(Long pkSchedule) {
        setPkSchedule(pkSchedule);
        return this;
    }

    public ScheduleDTO teacherPk(Long teacherPk) {
        setTeacherPk(teacherPk);
        return this;
    }

    public ScheduleDTO teacherName(String teacherName) {
        setTeacherName(teacherName);
        return this;
    }

    public ScheduleDTO disciplinePk(Long disciplinePk) {
        setDisciplinePk(disciplinePk);
        return this;
    }

    public ScheduleDTO disciplineName(String disciplineName) {
        setDisciplineName(disciplineName);
        return this;
    }

    public ScheduleDTO schoolClassPk(Long schoolClassPk) {
        setSchoolClassPk(schoolClassPk);
        return this;
    }

    public ScheduleDTO schoolClassName(String schoolClassName) {
        setSchoolClassName(schoolClassName);
        return this;
    }

    public ScheduleDTO weekDay(WeekDay weekDay) {
        setWeekDay(weekDay);
        return this;
    }

    public ScheduleDTO startTime(LocalTime startTime) {
        setStartTime(startTime);
        return this;
    }

    public ScheduleDTO endTime(LocalTime endTime) {
        setEndTime(endTime);
        return this;
    }

    public ScheduleDTO classroom(String classroom) {
        setClassroom(classroom);
        return this;
    }

    public ScheduleDTO createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public ScheduleDTO updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }
}