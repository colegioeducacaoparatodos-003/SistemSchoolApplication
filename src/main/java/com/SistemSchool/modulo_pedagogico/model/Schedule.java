package com.SistemSchool.modulo_pedagogico.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import com.SistemSchool.modulo_pedagogico.io.WeekDay;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;

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
import jakarta.persistence.Table;
import java.beans.Transient;

@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pkSchedule;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "discipline_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_schedule_discipline"))
    private Discipline discipline;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "school_class_pk", nullable = false, foreignKey = @ForeignKey(name = "fk_schedule_school_class"))
    private SchoolClass schoolClass;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_pk", nullable = true, foreignKey = @ForeignKey(name = "fk_schedule_teacher"))
    private Teacher teacher;

    @Enumerated(EnumType.STRING)
    private WeekDay weekDay;

    private LocalTime startTime;
    private LocalTime endTime;
    private String anoLectivo;

    // AUDITORIA
    private String obs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Transient
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Schedule() {}

    public Schedule(Long pkSchedule, Discipline discipline, SchoolClass schoolClass, Teacher teacher,
                    WeekDay weekDay, LocalTime startTime, LocalTime endTime, String anoLectivo,
                    String obs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pkSchedule = pkSchedule;
        this.discipline = discipline;
        this.schoolClass = schoolClass;
        this.teacher = teacher;
        this.weekDay = weekDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.anoLectivo = anoLectivo;
        this.obs = obs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPkSchedule() { return pkSchedule; }
    public void setPkSchedule(Long pkSchedule) { this.pkSchedule = pkSchedule; }

    public Discipline getDiscipline() { return discipline; }
    public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

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

    public Schedule pkSchedule(Long pkSchedule) { setPkSchedule(pkSchedule); return this; }
    public Schedule discipline(Discipline discipline) { setDiscipline(discipline); return this; }
    public Schedule schoolClass(SchoolClass schoolClass) { setSchoolClass(schoolClass); return this; }
    public Schedule teacher(Teacher teacher) { setTeacher(teacher); return this; }
    public Schedule weekDay(WeekDay weekDay) { setWeekDay(weekDay); return this; }
    public Schedule startTime(LocalTime startTime) { setStartTime(startTime); return this; }
    public Schedule endTime(LocalTime endTime) { setEndTime(endTime); return this; }
    public Schedule anoLectivo(String anoLectivo) { setAnoLectivo(anoLectivo); return this; }
    public Schedule obs(String obs) { setObs(obs); return this; }
    public Schedule createdAt(LocalDateTime createdAt) { setCreatedAt(createdAt); return this; }
    public Schedule updatedAt(LocalDateTime updatedAt) { setUpdatedAt(updatedAt); return this; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Schedule)) return false;
        Schedule schedule = (Schedule) o;
        return Objects.equals(pkSchedule, schedule.pkSchedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkSchedule);
    }
}