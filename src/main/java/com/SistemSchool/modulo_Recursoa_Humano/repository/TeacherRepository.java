package com.SistemSchool.modulo_Recursoa_Humano.repository;

import com.SistemSchool.modulo_Recursoa_Humano.dto.TeacherDTO;
import com.SistemSchool.modulo_Recursoa_Humano.interfaces.TeacherTableProjection;
import com.SistemSchool.modulo_Recursoa_Humano.io.ContractType;
import com.SistemSchool.modulo_Recursoa_Humano.io.QualificationLevel;
import com.SistemSchool.modulo_Recursoa_Humano.io.TeacherStatus;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

       // -------------------------------
       // Método para Lazy Loading com filtros (número, nome, estado)
       // -------------------------------

       @Query(value = """
                     SELECT t.pk_teacher      AS pkTeacher,
                            t.teacher_number  AS teacherNumber,
                            t.frist_name      AS fristName,
                            t.last_name       AS lastName,
                            t.qualification_livel AS qualificationLivel,
                            t.contract_type   AS contractType,
                            t.status          AS status,
                            t.photo_phath     AS photoPhath,
                            t.email           AS email,
                            t.phone           AS phone,
                            t.created_at      AS createdAt,
                            t.updated_at      AS updatedAt
                     FROM teacher t
                     WHERE (:teacherNumber IS NULL OR LOWER(t.teacher_number) LIKE LOWER(CONCAT('%', :teacherNumber, '%')))
                       AND (:name IS NULL OR LOWER(CONCAT(t.frist_name, ' ', t.last_name)) LIKE LOWER(CONCAT('%', :name, '%')))
                       AND (:status IS NULL OR t.status = :status)
                     """, countQuery = """
                     SELECT COUNT(*)
                     FROM teacher t
                     WHERE (:teacherNumber IS NULL OR LOWER(t.teacher_number) LIKE LOWER(CONCAT('%', :teacherNumber, '%')))
                       AND (:name IS NULL OR LOWER(CONCAT(t.frist_name, ' ', t.last_name)) LIKE LOWER(CONCAT('%', :name, '%')))
                       AND (:status IS NULL OR t.status = :status)
                     """, nativeQuery = true)
       Page<TeacherTableProjection> findAllForTable(Pageable pageable,
                     @Param("teacherNumber") String teacherNumber,
                     @Param("name") String name,
                     @Param("status") String status);

       @Query(value = """
                     SELECT COUNT(*)
                     FROM teacher t
                     WHERE (:teacherNumber IS NULL OR LOWER(t.teacher_number) LIKE LOWER(CONCAT('%', :teacherNumber, '%')))
                       AND (:name IS NULL OR LOWER(CONCAT(t.frist_name, ' ', t.last_name)) LIKE LOWER(CONCAT('%', :name, '%')))
                       AND (:status IS NULL OR t.status = :status)
                     """, nativeQuery = true)
       long countFiltered(@Param("teacherNumber") String teacherNumber,
                     @Param("name") String name,
                     @Param("status") String status);

       // -------------------------------
       // Estatísticas do cabeçalho
       // -------------------------------

       long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

       @Query("SELECT COALESCE(SUM(t.baseSalary), 0) FROM Teacher t")
       BigDecimal sumBaseSalary();

       // -------------------------------
       // Geração automática do teacherNumber no formato PROF-ANO-SEQUENCIA
       // Bloqueia a última linha correspondente ao prefixo do ano corrente
       // (FOR UPDATE) para evitar colisões em gravações concorrentes.
       // -------------------------------
       @Query(value = """
                     SELECT t.teacher_number
                     FROM teacher t
                     WHERE t.teacher_number LIKE CONCAT(:prefix, '%')
                     ORDER BY t.teacher_number DESC
                     LIMIT 1
                     FOR UPDATE
                     """, nativeQuery = true)
       String findLastTeacherNumberForUpdate(@Param("prefix") String prefix);

       // -------------------------------
       // Queries utilitárias
       // -------------------------------

       boolean existsByTeacherNumber(String teacherNumber);

       boolean existsByBiNumber(String biNumber);

       List<Teacher> findByQualificationLivel(QualificationLevel qualificationLivel);

       List<Teacher> findByContractType(ContractType contractType);

       List<Teacher> findByStatus(TeacherStatus status);

       long countByStatus(TeacherStatus status);
}
