package com.SistemSchool.modulo_pedagogico.repository;

import com.SistemSchool.modulo_pedagogico.dto.EvaluationDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.EvaluationTableProjection;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.io.Trimester;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    // ─────────────────────────────────────────────
    // Lazy Loading para tabela (nativeQuery)
    // ─────────────────────────────────────────────

    @Query(value = """
        SELECT e.pk_evaluation    AS pkEvaluation,
               d.pk_discipline    AS disciplinePk,
               d.discipline_name  AS disciplineName,
               e.evaluation_name  AS evaluationName,
               e.evaluation_type  AS evaluationType,
               e.trimester        AS trimester,
               e.evaluation_date  AS evaluationDate,
               e.ano_lectivo      AS anoLectivo,
               e.obs              AS obs,
               e.created_at       AS createdAt,
               e.updated_at       AS updatedAt
        FROM evaluation e
        INNER JOIN discipline d ON d.pk_discipline = e.discipline_pk
        """, countQuery = "SELECT COUNT(*) FROM evaluation", nativeQuery = true)
    Page<EvaluationTableProjection> findAllForTable(Pageable pageable);

    // ─────────────────────────────────────────────
    // Lista completa com DTO (JPQL)
    // ─────────────────────────────────────────────

    @Query("""
        SELECT new com.SistemSchool.modulo_pedagogico.dto.EvaluationDTO(
            e.pkEvaluation,
            e.discipline.pkDiscipline,
            e.discipline.disciplineName,
            e.evaluationName,
            e.evaluationType,
            e.trimester,
            e.evaluationDate,
            e.anoLectivo,
            e.obs,
            e.createdAt,
            e.updatedAt
        ) FROM Evaluation e
        """)
    List<EvaluationDTO> findAllEvaluationsDTO();

    @Query("""
        SELECT e FROM Evaluation e
        JOIN FETCH e.discipline
        ORDER BY e.evaluationDate DESC
        """)
    List<Evaluation> findAllWithDiscipline();

    @Query("""
        SELECT e FROM Evaluation e
        JOIN FETCH e.discipline
        WHERE e.discipline.pkDiscipline = :disciplinePk
        AND e.trimester = :trimester
        AND e.anoLectivo = :anoLectivo
        ORDER BY e.evaluationType, e.evaluationDate
        """)
    List<Evaluation> findByDisciplineAndTrimesterAndYear(
        @Param("disciplinePk") Long disciplinePk,
        @Param("trimester") Trimester trimester,
        @Param("anoLectivo") String anoLectivo
    );

    // Usado pelo lançamento de notas
    Optional<Evaluation> findByDisciplineAndTrimesterAndEvaluationTypeAndEvaluationName(
            Discipline discipline,
            Trimester trimester,
            EvaluationType evaluationType,
            String evaluationName);

    // ─────────────────────────────────────────────
    // Queries utilitárias
    // ─────────────────────────────────────────────

    List<Evaluation> findByDiscipline_PkDiscipline(Long disciplinePk);

    List<Evaluation> findByTrimester(Trimester trimester);

    List<Evaluation> findByEvaluationType(EvaluationType evaluationType);

    List<Evaluation> findByAnoLectivo(String anoLectivo);
}