package com.SistemSchool.modulo_pedagogico.repository;

import com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.DisciplineTableProjection;
import com.SistemSchool.modulo_pedagogico.io.DisciplineStatus;
import com.SistemSchool.modulo_pedagogico.model.Discipline;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisciplineRepository extends JpaRepository<Discipline, Long> {

        // -------------------------------
        // Lazy Loading para tabela (nativeQuery)
        // -------------------------------

        @Query(value = """
                        SELECT d.pk_discipline    AS pkDiscipline,
                               d.discipline_code  AS disciplineCode,
                               d.discipline_name  AS disciplineName,
                               d.workload         AS workload,
                               d.status           AS status,
                               d.created_at       AS createdAt,
                               d.updated_at       AS updatedAt
                        FROM discipline d
                        """, countQuery = "SELECT COUNT(*) FROM discipline", nativeQuery = true)
        Page<DisciplineTableProjection> findAllForTable(Pageable pageable);

        // -------------------------------
        // Lista completa com DTO (JPQL)
        // -------------------------------

        @Query("""
                        SELECT new com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO(
                               d.pkDiscipline,
                               d.disciplineCode,
                               d.disciplineName,
                               d.workload,
                               d.status,
                               d.createdAt,
                               d.updatedAt
                        )
                        FROM Discipline d
                        """)
        List<DisciplineDTO> findAllDisciplinesDTO();

        // -------------------------------
        // Queries utilitárias
        // -------------------------------

        boolean existsByDisciplineCode(String disciplineCode);

        List<Discipline> findByStatus(DisciplineStatus status);

        List<Discipline> findByDisciplineNameContainingIgnoreCase(String disciplineName);

        long countByStatus(DisciplineStatus status);
}