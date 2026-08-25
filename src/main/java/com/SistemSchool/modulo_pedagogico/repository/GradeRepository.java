package com.SistemSchool.modulo_pedagogico.repository;

import com.SistemSchool.modulo_pedagogico.dto.GradeDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.GradeTableProjection;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.io.GradeStatus;
import com.SistemSchool.modulo_pedagogico.model.Grade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

       // -------------------------------
       // Lazy Loading para tabela (nativeQuery)
       // -------------------------------

       @Query(value = """
                     SELECT g.pk_grade                                    AS pkGrade,
                            ev.pk_evaluation                               AS evaluationPk,
                            CONCAT(ev.title, ' - ', d.discipline_name)      AS evaluationDescription,
                            en.ph_enrolment                                AS enrolmentPk,
                            en.enrolment_numer                             AS enrolmentNumber,
                            s.pk_student                                   AS studentPk,
                            s.full_name                                    AS studentFullName,
                            g.value                                        AS value,
                            g.status                                       AS status,
                            g.observation                                  AS observation,
                            g.created_at                                   AS createdAt,
                            g.updated_at                                   AS updatedAt
                     FROM grade g
                     INNER JOIN evaluation ev ON ev.pk_evaluation = g.evaluation_pk
                     INNER JOIN discipline d ON d.pk_discipline = ev.discipline_pk
                     INNER JOIN enrolment en ON en.ph_enrolment = g.enrolment_pk
                     INNER JOIN student s ON s.pk_student = g.student_pk
                     """, countQuery = "SELECT COUNT(*) FROM grade", nativeQuery = true)
       Page<GradeTableProjection> findAllForTable(Pageable pageable);

       // -------------------------------
       // Lista completa com DTO (JPQL)
       // -------------------------------

       @Query("""
                     SELECT new com.SistemSchool.modulo_pedagogico.dto.GradeDTO(
                            g.pkGrade,
                            g.evaluation.pkEvaluation,
                            CONCAT(g.evaluation.title, ' - ', g.evaluation.discipline.disciplineName),
                            g.enrolment.phEnrolment,
                            g.enrolment.enrolmentNumer,
                            g.student.pkStudent,
                            g.student.fullName,
                            g.value,
                            g.status,
                            g.observation,
                            g.createdAt,
                            g.updatedAt
                     )
                     FROM Grade g
                     """)
       List<GradeDTO> findAllGradesDTO();

       // -------------------------------
       // Dados para Mini-Pauta (todas as notas de uma disciplina + turma + trimestre)
       // -------------------------------

       @Query("""
                     SELECT g
                     FROM Grade g
                     WHERE g.evaluation.discipline.pkDiscipline = :disciplinePk
                       AND g.evaluation.trimester = :trimester
                       AND g.enrolment.schoolClass.pkSchoolClass = :schoolClassPk
                       AND g.value IS NOT NULL
                     """)
       List<Grade> findByDisciplineAndTrimesterAndClass(
                     @Param("disciplinePk") Long disciplinePk,
                     @Param("trimester") Integer trimester,
                     @Param("schoolClassPk") Long schoolClassPk);

       // -------------------------------
       // Dados para Boletim (todas as notas de um aluno + matrícula + trimestre)
       // -------------------------------

       @Query("""
                     SELECT g
                     FROM Grade g
                     WHERE g.student.pkStudent = :studentPk
                       AND g.enrolment.phEnrolment = :enrolmentPk
                       AND g.evaluation.trimester = :trimester
                       AND g.value IS NOT NULL
                     ORDER BY g.evaluation.discipline.disciplineName
                     """)
       List<Grade> findByStudentAndEnrolmentAndTrimester(
                     @Param("studentPk") Long studentPk,
                     @Param("enrolmentPk") Long enrolmentPk,
                     @Param("trimester") Integer trimester);

       // -------------------------------
       // Cálculo de médias (Boletim / Pauta)
       // -------------------------------

       @Query("""
                     SELECT SUM(g.value * g.evaluation.weight) / SUM(g.evaluation.weight)
                     FROM Grade g
                     WHERE g.student.pkStudent = :studentPk
                       AND g.evaluation.discipline.pkDiscipline = :disciplinePk
                       AND g.evaluation.trimester = :trimester
                       AND g.value IS NOT NULL
                     """)
       Double calcularMediaFinal(
                     @Param("studentPk") Long studentPk,
                     @Param("disciplinePk") Long disciplinePk,
                     @Param("trimester") Integer trimester);

       @Query("""
                     SELECT g.student.pkStudent, SUM(g.value * g.evaluation.weight) / SUM(g.evaluation.weight)
                     FROM Grade g
                     WHERE g.evaluation.discipline.pkDiscipline = :disciplinePk
                       AND g.evaluation.trimester = :trimester
                       AND g.enrolment.schoolClass.pkSchoolClass = :schoolClassPk
                       AND g.value IS NOT NULL
                     GROUP BY g.student.pkStudent
                     """)
       List<Object[]> calcularMediasDaTurma(
                     @Param("schoolClassPk") Long schoolClassPk,
                     @Param("disciplinePk") Long disciplinePk,
                     @Param("trimester") Integer trimester);

       // -------------------------------
       // Queries utilitárias
       // -------------------------------

       List<Grade> findByEvaluation_PkEvaluation(Long evaluationPk);

       List<Grade> findByEnrolment_PhEnrolment(Long enrolmentPk);

       List<Grade> findByStudent_PkStudent(Long studentPk);

       List<Grade> findByStatus(GradeStatus status);

       boolean existsByEvaluation_PkEvaluationAndStudent_PkStudent(Long evaluationPk, Long studentPk);
}
