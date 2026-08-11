package com.SistemSchool.modulo_pedagogico.repository;

import com.SistemSchool.modulo_pedagogico.dto.ScheduleDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.ScheduleTableProjection;
import com.SistemSchool.modulo_pedagogico.io.WeekDay;
import com.SistemSchool.modulo_pedagogico.model.Schedule;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

       // -------------------------------
       // Lazy Loading para tabela (nativeQuery)
       // -------------------------------

       @Query(value = """
                     SELECT
                         s.pk_schedule AS pkSchedule,

                         t.pk_teacher AS teacherPk,
                         CONCAT(t.frist_name, ' ', t.last_name) AS teacherName,

                         d.pk_discipline AS disciplinePk,
                         d.discipline_name AS disciplineName,

                         sc.pk_school_class AS schoolClassPk,
                         sc.class_name AS schoolClassName,

                         s.week_day AS weekDay,
                         s.start_time AS startTime,
                         s.end_time AS endTime,
                         s.classroom AS classroom,
                         s.created_at AS createdAt,
                         s.updated_at AS updatedAt

                     FROM schedule s

                     INNER JOIN teacher t
                         ON t.pk_teacher = s.teacher_pk

                     INNER JOIN discipline d
                         ON d.pk_discipline = s.discipline_pk

                     INNER JOIN school_class sc
                         ON sc.pk_school_class = s.school_class_pk
                     """, countQuery = """
                         SELECT COUNT(*)
                         FROM schedule
                     """, nativeQuery = true)
       Page<ScheduleTableProjection> findAllForTable(Pageable pageable);

       // -------------------------------
       // Lista completa com DTO (JPQL)
       // -------------------------------

       @Query("""
                     SELECT new com.SistemSchool.modulo_pedagogico.dto.ScheduleDTO(
                            s.pkSchedule,
                            s.teacher.pkTeacher,
                            concat(s.teacher.fristName, ' ', s.teacher.lastName),
                            s.discipline.pkDiscipline,
                            s.discipline.disciplineName,
                            s.schoolClass.pkSchoolClass,
                            s.schoolClass.className,
                            s.weekDay,
                            s.startTime,
                            s.endTime,
                            s.classroom,
                            s.createdAt,
                            s.updatedAt
                     )
                     FROM Schedule s
                     """)
       List<ScheduleDTO> findAllSchedulesDTO();

       // -------------------------------
       // Queries utilitárias
       // -------------------------------

       List<Schedule> findByTeacher_PkTeacher(Long teacherPk);

       List<Schedule> findByDiscipline_PkDiscipline(Long disciplinePk);

       List<Schedule> findBySchoolClass_PkSchoolClass(Long schoolClassPk);

       List<Schedule> findByWeekDay(WeekDay weekDay);

       // Evita conflito de horário para o mesmo professor no mesmo dia
       boolean existsByTeacher_PkTeacherAndWeekDayAndStartTimeAndEndTime(
                     Long teacherPk, WeekDay weekDay, LocalTime startTime, LocalTime endTime);
}