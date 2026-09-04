package com.SistemSchool.modulo_pedagogico.repository;

import com.SistemSchool.modulo_pedagogico.dto.ScheduleDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.ScheduleTableProjection;
import com.SistemSchool.modulo_pedagogico.model.Schedule;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // -------------------------------
    // Lazy Loading para tabela (nativeQuery)
    // -------------------------------

    @Query(value = """
        SELECT s.pk_schedule      AS pkSchedule,
               d.pk_discipline    AS disciplinePk,
               d.discipline_name  AS disciplineName,
               sc.pk_school_class AS schoolclassPk,
               sc.class_name      AS schoolclassnome,
               sc.class_code      AS schoolclasscode,
               t.pk_teacher       AS teacherPk,
               CONCAT(t.frist_name, ' ', t.last_name) AS teacherName,
               s.week_day         AS weekDay,
               s.start_time       AS startTime,
               s.end_time         AS endTime,
               s.ano_lectivo      AS anoLectivo,
               s.obs              AS obs,
               s.created_at       AS createdAt,
               s.updated_at       AS updatedAt
        FROM schedule s
        INNER JOIN discipline d ON d.pk_discipline = s.discipline_pk
        INNER JOIN school_class sc ON sc.pk_school_class = s.school_class_pk
        LEFT JOIN teacher t ON t.pk_teacher = s.teacher_pk
        """, countQuery = "SELECT COUNT(*) FROM schedule", nativeQuery = true)
    Page<ScheduleTableProjection> findAllForTable(Pageable pageable);

    // -------------------------------
    // Lista completa com DTO (JPQL)
    // -------------------------------

    @Query("""
        SELECT new com.SistemSchool.modulo_pedagogico.dto.ScheduleDTO(
            s.pkSchedule,
            s.discipline.pkDiscipline,
            s.discipline.disciplineName,
            s.schoolClass.pkSchoolClass,
            s.schoolClass.className,
            s.schoolClass.classCode,
            t.pkTeacher,
            CONCAT(t.fristName, ' ', t.lastName),
            s.weekDay,
            s.startTime,
            s.endTime,
            s.anoLectivo,
            s.obs,
            s.createdAt,
            s.updatedAt
        ) FROM Schedule s
        LEFT JOIN s.teacher t
        """)
    List<ScheduleDTO> findAllSchedulesDTO();

    @Query("""
        SELECT s FROM Schedule s
        JOIN FETCH s.discipline
        JOIN FETCH s.schoolClass
        LEFT JOIN FETCH s.teacher
        ORDER BY s.weekDay, s.startTime
        """)
    List<Schedule> findAllWithRelations();

    @Query("""
        SELECT s FROM Schedule s
        JOIN FETCH s.discipline
        JOIN FETCH s.schoolClass
        LEFT JOIN FETCH s.teacher
        WHERE s.schoolClass.pkSchoolClass = :schoolClassPk
        ORDER BY s.weekDay, s.startTime
        """)
    List<Schedule> findBySchoolClassPkWithRelations(@Param("schoolClassPk") Long schoolClassPk);

    // -------------------------------
    // Queries utilitárias
    // -------------------------------

    List<Schedule> findBySchoolClass_PkSchoolClass(Long schoolClassPk);

    List<Schedule> findByDiscipline_PkDiscipline(Long disciplinePk);

    List<Schedule> findByAnoLectivo(String anoLectivo);

    List<Schedule> findByTeacher_PkTeacher(Long teacherPk);
}