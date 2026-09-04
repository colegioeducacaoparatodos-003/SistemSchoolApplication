package com.SistemSchool.modulo_pedagogico.repository;

import com.SistemSchool.modulo_pedagogico.dto.GradeDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.GradeTableProjection;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.Grade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    // -------------------------------
    // Lazy Loading para tabela (nativeQuery)
    // -------------------------------

    @Query(value = """
            SELECT g.pk_grade        AS pkGrade,
                   e.pk_evaluation   AS evaluationPk,
                   e.evaluation_name AS evaluationName,
                   e.evaluation_type AS evaluationType,
                   e.trimester       AS trimester,
                   d.discipline_name AS disciplineName,
                   en.ph_enrolment   AS enrolmentPk,
                   s.full_name       AS studentFullName,
                   s.sudent_number   AS studentNumber,
                   sc.class_name     AS schoolclassName,
                   g.score           AS score,
                   g.launch_date     AS launchDate,
                   g.obs             AS obs,
                   g.created_at      AS createdAt,
                   g.updated_at      AS updatedAt
            FROM grade g
            INNER JOIN evaluation e ON e.pk_evaluation = g.evaluation_pk
            INNER JOIN discipline d ON d.pk_discipline = e.discipline_pk
            INNER JOIN enrolment en ON en.ph_enrolment = g.enrolment_pk
            INNER JOIN student s ON s.pk_student = en.student_pk
            INNER JOIN school_class sc ON sc.pk_school_class = en.school_class_pk
            """, countQuery = "SELECT COUNT(*) FROM grade", nativeQuery = true)
    Page<GradeTableProjection> findAllForTable(Pageable pageable);

    // -------------------------------
    // Lista completa com DTO (JPQL)
    // -------------------------------

    @Query("""
            SELECT new com.SistemSchool.modulo_pedagogico.dto.GradeDTO(
                g.pkGrade,
                g.evaluation.pkEvaluation,
                g.evaluation.evaluationName,
                g.evaluation.evaluationType,
                g.evaluation.trimester,
                g.evaluation.discipline.disciplineName,
                g.enrolment.phEnrolment,
                g.enrolment.student.fullName,
                g.enrolment.student.sudentNumber,
                g.enrolment.schoolClass.className,
                g.score,
                g.launchDate,
                g.obs,
                g.createdAt,
                g.updatedAt
            ) FROM Grade g
            """)
    List<GradeDTO> findAllGradesDTO();

    @Query("""
            SELECT g FROM Grade g
            JOIN FETCH g.evaluation
            JOIN FETCH g.enrolment
            ORDER BY g.createdAt DESC
            """)
    List<Grade> findAllWithRelations();

    @Query("""
            SELECT g FROM Grade g
            JOIN FETCH g.evaluation ev
            WHERE g.enrolment.phEnrolment = :enrolmentPk
            AND ev.discipline.pkDiscipline = :disciplinePk
            AND ev.trimester = :trimester
            AND ev.evaluationType = :type
            """)
    List<Grade> findByEnrolmentDisciplineTrimesterType(
            @Param("enrolmentPk") Long enrolmentPk,
            @Param("disciplinePk") Long disciplinePk,
            @Param("trimester") Trimester trimester,
            @Param("type") EvaluationType type);

    @Query("""
            SELECT g FROM Grade g
            WHERE g.enrolment.phEnrolment = :enrolmentPk
            AND g.evaluation.pkEvaluation = :evaluationPk
            """)
    Optional<Grade> findByEnrolmentPkAndEvaluationPk(
            @Param("enrolmentPk") Long enrolmentPk,
            @Param("evaluationPk") Long evaluationPk);
    // -------------------------------
    // Queries utilitárias
    // -------------------------------

    List<Grade> findByEnrolment_PhEnrolment(Long enrolmentPk);

    List<Grade> findByEvaluation_PkEvaluation(Long evaluationPk);
}