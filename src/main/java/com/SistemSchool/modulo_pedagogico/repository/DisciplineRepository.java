package com.SistemSchool.modulo_pedagogico.repository;

import com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.DisciplineTableProjection;
import com.SistemSchool.modulo_pedagogico.io.DisciplineStatus;
import com.SistemSchool.modulo_pedagogico.model.Discipline;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisciplineRepository extends JpaRepository<Discipline, Long> {

    // -------------------------------
    // Lazy Loading para tabela (nativeQuery)
    // -------------------------------

    @Query(value = """
        SELECT d.pk_discipline   AS pkDiscipline,
               d.discipline_code AS disciplineCode,
               d.discipline_name AS disciplineName,
               d.description     AS description,
               d.status          AS status,
               d.obs             AS obs,
               d.created_at      AS createdAt,
               d.updated_at      AS updatedAt
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
            d.description,
            d.status,
            d.obs,
            d.createdAt,
            d.updatedAt
        ) FROM Discipline d
        """)
    List<DisciplineDTO> findAllDisciplinesDTO();

    @Query("""
        SELECT d FROM Discipline d
        WHERE d.status = 'ATIVO'
        ORDER BY d.disciplineName
        """)
    List<Discipline> findAllActive();

    // -------------------------------
    // Geração automática de código (DISC-ANO-SEQUENCIA)
    // -------------------------------

    /**
     * Retorna o último código de disciplina gerado para o prefixo indicado
     * (ex.: "DISC-2026-"), em ordem decrescente, bloqueando a linha para
     * evitar códigos duplicados em gravações concorrentes.
     */
    @Query(value = """
        SELECT d.discipline_code
        FROM discipline d
        WHERE d.discipline_code LIKE CONCAT(:prefix, '%')
        ORDER BY d.discipline_code DESC
        LIMIT 1
        FOR UPDATE
        """, nativeQuery = true)
    Optional<String> findLastCodeByPrefixForUpdate(@Param("prefix") String prefix);

    

    // -------------------------------
    // Queries utilitárias
    // -------------------------------

    boolean existsByDisciplineCode(String disciplineCode);

    Optional<Discipline> findByDisciplineCode(String disciplineCode);

    long countByStatus(DisciplineStatus status);
}