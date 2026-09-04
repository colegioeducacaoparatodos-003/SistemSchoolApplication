package com.SistemSchool.modulo_pedagogico.repository;

import com.SistemSchool.modulo_pedagogico.dto.TrimesterResultDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.TrimesterResultTableProjection;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.TrimesterResult;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrimesterResultRepository extends JpaRepository<TrimesterResult, Long> {

    // -------------------------------
    // Lazy Loading para tabela (nativeQuery)
    // -------------------------------

    @Query(value = """
        SELECT tr.pk_trimester_result AS pkTrimesterResult,
               en.ph_enrolment        AS enrolmentPk,
               s.full_name            AS studentFullName,
               s.sudent_number        AS studentNumber,
               sc.class_name          AS schoolclassnome,
               d.pk_discipline        AS disciplinePk,
               d.discipline_name      AS disciplineName,
               tr.trimester           AS trimester,
               tr.mac                 AS mac,
               tr.npt                 AS npt,
               tr.mt                  AS mt,
               tr.situation           AS situation,
               tr.obs                 AS obs,
               tr.created_at          AS createdAt,
               tr.updated_at          AS updatedAt
        FROM trimester_result tr
        INNER JOIN enrolment en ON en.ph_enrolment = tr.enrolment_pk
        INNER JOIN student s ON s.pk_student = en.student_pk
        INNER JOIN school_class sc ON sc.pk_school_class = en.school_class_pk
        INNER JOIN discipline d ON d.pk_discipline = tr.discipline_pk
        """, countQuery = "SELECT COUNT(*) FROM trimester_result", nativeQuery = true)
    Page<TrimesterResultTableProjection> findAllForTable(Pageable pageable);

    // -------------------------------
    // Lista completa com DTO (JPQL)
    // -------------------------------

    @Query("""
        SELECT new com.SistemSchool.modulo_pedagogico.dto.TrimesterResultDTO(
            tr.pkTrimesterResult,
            tr.enrolment.phEnrolment,
            tr.enrolment.student.fullName,
            tr.enrolment.student.sudentNumber,
            tr.enrolment.schoolClass.className,
            tr.discipline.pkDiscipline,
            tr.discipline.disciplineName,
            tr.trimester,
            tr.mac,
            tr.npt,
            tr.mt,
            tr.situation,
            tr.obs,
            tr.createdAt,
            tr.updatedAt
        ) FROM TrimesterResult tr
        """)
    List<TrimesterResultDTO> findAllResultsDTO();

    @Query("""
        SELECT tr FROM TrimesterResult tr
        JOIN FETCH tr.enrolment en
        JOIN FETCH en.student
        JOIN FETCH en.schoolClass
        JOIN FETCH tr.discipline
        ORDER BY tr.discipline.disciplineName, en.student.fullName
        """)
    List<TrimesterResult> findAllWithRelations();

    @Query("""
        SELECT tr FROM TrimesterResult tr
        JOIN FETCH tr.enrolment en
        JOIN FETCH en.student
        JOIN FETCH en.schoolClass
        JOIN FETCH tr.discipline
        WHERE en.schoolClass.pkSchoolClass = :schoolClassPk
        AND tr.trimester = :trimester
        ORDER BY tr.discipline.disciplineName, en.student.fullName
        """)
    List<TrimesterResult> findBySchoolClassAndTrimesterWithRelations(
        @Param("schoolClassPk") Long schoolClassPk,
        @Param("trimester") Trimester trimester
    );

    // -------------------------------
    // Queries utilitárias
    // -------------------------------

    List<TrimesterResult> findByEnrolment_PhEnrolment(Long enrolmentPk);

    List<TrimesterResult> findByDiscipline_PkDiscipline(Long disciplinePk);

    Optional<TrimesterResult> findByEnrolment_PhEnrolmentAndDiscipline_PkDisciplineAndTrimester(
        Long enrolmentPk, Long disciplinePk, Trimester trimester
    );

    @Query("""
        SELECT tr FROM TrimesterResult tr
        JOIN tr.enrolment en
        WHERE en.schoolClass.pkSchoolClass = :schoolClassPk
        AND tr.trimester = :trimester
        ORDER BY tr.discipline.disciplineName, en.student.fullName
        """)
    List<TrimesterResult> findBySchoolClassAndTrimester(
        @Param("schoolClassPk") Long schoolClassPk,
        @Param("trimester") Trimester trimester
    );
}