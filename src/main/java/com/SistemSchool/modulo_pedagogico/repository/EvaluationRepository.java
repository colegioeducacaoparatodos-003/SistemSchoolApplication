package com.SistemSchool.modulo_pedagogico.repository;

import com.SistemSchool.modulo_pedagogico.dto.EvaluationDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.EvaluationTableProjection;
import com.SistemSchool.modulo_pedagogico.io.EvaluationStatus;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

       // -------------------------------
       // Lazy Loading para tabela (nativeQuery)
       // -------------------------------

       @Query(value = """
                     SELECT e.pk_evaluation      AS pkEvaluation,
                            d.pk_discipline       AS disciplinePk,
                            d.discipline_name     AS disciplineName,
                            s.pk_schedule          AS schedulePk,
                            s.week_day             AS scheduleWeekDay,
                            e.title                AS title,
                            e.type                 AS type,
                            e.weight               AS weight,
                            e.evaluation_date      AS evaluationDate,
                            e.status               AS status,
                            e.trimester            AS trimester,
                            e.created_at           AS createdAt,
                            e.updated_at           AS updatedAt
                     FROM evaluation e
                     INNER JOIN discipline d ON d.pk_discipline = e.discipline_pk
                     INNER JOIN schedule s ON s.pk_schedule = e.schedule_pk
                     """, countQuery = "SELECT COUNT(*) FROM evaluation", nativeQuery = true)
       Page<EvaluationTableProjection> findAllForTable(Pageable pageable);

       // -------------------------------
       // Lista completa com DTO (JPQL)
       // -------------------------------

       @Query("""
                     SELECT new com.SistemSchool.modulo_pedagogico.dto.EvaluationDTO(
                            e.pkEvaluation,
                            e.discipline.pkDiscipline,
                            e.discipline.disciplineName,
                            e.schedule.pkSchedule,
                            e.schedule.weekDay,
                            e.title,
                            e.type,
                            e.weight,
                            e.evaluationDate,
                            e.status,
                            e.trimester,
                            e.createdAt,
                            e.updatedAt
                     )
                     FROM Evaluation e
                     """)
       List<EvaluationDTO> findAllEvaluationsDTO();

       // -------------------------------
       // Queries utilitárias
       // -------------------------------

       List<Evaluation> findByDiscipline_PkDiscipline(Long disciplinePk);

       List<Evaluation> findBySchedule_PkSchedule(Long schedulePk);

       List<Evaluation> findByType(EvaluationType type);

       List<Evaluation> findByStatus(EvaluationStatus status);

       List<Evaluation> findByTrimester(Integer trimester);

       boolean existsByDiscipline_PkDisciplineAndTitleAndTrimester(Long disciplinePk, String title, Integer trimester);
}