package com.SistemSchool.modulo_pedagogico.model;

import java.beans.Transient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;
import com.SistemSchool.modulo_pedagogico.io.DisciplineStatus;
import com.SistemSchool.modulo_pedagogico.io.WeekDay;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.util.Objects;

@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkSchedule;

    /** Relacionamentos */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_schedule_teacher"))
    private Teacher teacher;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "discipline_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_schedule_discipline"))
    private Discipline discipline;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "school_class_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_disciplina_scholl_class"))
    private SchoolClass schoolClass;

    @Enumerated(EnumType.STRING)
    private WeekDay weekDay;

    private LocalTime startTime;

    private LocalTime endTime;

    private String classroom;

    // =======================================
    // Auditoria
    // =======================================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.weekDay == null) {
            this.weekDay = WeekDay.MONDAY;
        }

    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public Schedule() {
    }

    public Schedule(Long pkSchedule, Teacher teacher, Discipline discipline, 
                    SchoolClass schoolClass, WeekDay weekDay, LocalTime startTime, LocalTime endTime, 
                    String classroom, LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.pkSchedule = pkSchedule;
        this.teacher = teacher;
        this.discipline = discipline;
        this.schoolClass = schoolClass;
        this.weekDay = weekDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classroom = classroom;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPkSchedule() {
        return this.pkSchedule;
    }

    public void setPkSchedule(Long phSchedule) {
        this.pkSchedule = pkSchedule;
    }

    public Teacher getTeacher() {
        return this.teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Discipline getDiscipline() {
        return this.discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }

    public SchoolClass getSchoolClass() {
        return this.schoolClass;
    }

    public void setSchoolClass(SchoolClass schoolClass) {
        this.schoolClass = schoolClass;
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

    public Schedule pkSchedule(Long phSchedule) {
        setPkSchedule(pkSchedule);
        return this;
    }

    public Schedule teacher(Teacher teacher) {
        setTeacher(teacher);
        return this;
    }

    public Schedule discipline(Discipline discipline) {
        setDiscipline(discipline);
        return this;
    }

    public Schedule schoolClass(SchoolClass schoolClass) {
        setSchoolClass(schoolClass);
        return this;
    }

    public Schedule weekDay(WeekDay weekDay) {
        setWeekDay(weekDay);
        return this;
    }

    public Schedule startTime(LocalTime startTime) {
        setStartTime(startTime);
        return this;
    }

    public Schedule endTime(LocalTime endTime) {
        setEndTime(endTime);
        return this;
    }

    public Schedule classroom(String classroom) {
        setClassroom(classroom);
        return this;
    }

    public Schedule createdAt(LocalDateTime createdAt) {
        setCreatedAt(createdAt);
        return this;
    }

    public Schedule updatedAt(LocalDateTime updatedAt) {
        setUpdatedAt(updatedAt);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Schedule)) {
            return false;
        }
        Schedule schedule = (Schedule) o;
        return Objects.equals(pkSchedule, schedule.pkSchedule) && Objects.equals(teacher, schedule.teacher) && Objects.equals(discipline, schedule.discipline) && Objects.equals(schoolClass, schedule.schoolClass) && Objects.equals(weekDay, schedule.weekDay) && Objects.equals(startTime, schedule.startTime) && Objects.equals(endTime, schedule.endTime) && Objects.equals(classroom, schedule.classroom) && Objects.equals(createdAt, schedule.createdAt) && Objects.equals(updatedAt, schedule.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkSchedule, teacher, 
                            discipline, schoolClass, 
                            weekDay, startTime, endTime, 
                            classroom, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "{" +
            " pkSchedule='" + getPkSchedule() + "'" +
            ", teacher='" + getTeacher() + "'" +
            ", discipline='" + getDiscipline() + "'" +
            ", schoolClass='" + getSchoolClass() + "'" +
            ", weekDay='" + getWeekDay() + "'" +
            ", startTime='" + getStartTime() + "'" +
            ", endTime='" + getEndTime() + "'" +
            ", classroom='" + getClassroom() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }


}
