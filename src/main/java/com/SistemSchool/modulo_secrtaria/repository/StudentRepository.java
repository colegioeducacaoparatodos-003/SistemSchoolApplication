package com.SistemSchool.modulo_secrtaria.repository;

import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.modulo_secrtaria.interfaces.StudentTableProjection;
import com.SistemSchool.modulo_secrtaria.io.StudentStatus;
import com.SistemSchool.modulo_secrtaria.model.Student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_secrtaria.io.StudentStatus;
import com.SistemSchool.modulo_secrtaria.model.Student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // -------------------------------
    // Lazy Loading para tabela (nativeQuery)
    // -------------------------------

    @Query(value = """
            SELECT
                s.pk_student AS pkStudent,
                s.sudent_number AS sudentNumber,
                s.frist_name AS fristName,
                s.last_name AS lastName,
                s.full_name AS fullName,
                s.gender AS gender,
                s.bi_number AS biNumber,
                s.nasc_date AS nascDate,
                s.email AS email,
                s.phone_1 AS phone_1,
                s.upload_photo AS uploadPhoto,
                s.status AS status,
                s.created_at AS createdAt,
                s.updated_at AS updatedAt
            FROM student s
            """, countQuery = """
                SELECT COUNT(*)
                FROM student
            """, nativeQuery = true)
    Page<StudentTableProjection> findAllForTable(Pageable pageable);

    // -------------------------------
    // Lista completa com DTO (JPQL)
    // -------------------------------

    @Query("""
            SELECT new com.SistemSchool.modulo_secrtaria.dto.StudentDTO(
                   s.pkStudent,
                   s.sudentNumber,
                   s.fristName,
                   s.lastName,
                   s.fullName,
                   s.gender,
                   s.biNumber,
                   s.nascDate,
                   s.biExpiryData,
                   s.addressStreet,
                   s.addressProvice,
                   s.nameFather,
                   s.nameMather,
                   s.email,
                   s.phone_1,
                   s.phone_2,
                   s.uploadPhoto,
                   s.status,
                   s.obs,
                   s.createdAt,
                   s.updatedAt
            )
            FROM Student s
            """)
    List<StudentDTO> findAllStudentsDTO();

    // -------------------------------
    // Queries utilitárias
    // -------------------------------

    List<Student> findByStatus(StudentStatus status);

    List<Student> findByFullNameContainingIgnoreCase(String fullName);

    Optional<Student> findBySudentNumber(String sudentNumber);

    Optional<Student> findByBiNumber(String biNumber);

    boolean existsBySudentNumber(String sudentNumber);

    boolean existsByBiNumber(String biNumber);

    boolean existsBySudentNumberAndPkStudentNot(String sudentNumber, Long pkStudent);

    boolean existsByBiNumberAndPkStudentNot(String biNumber, Long pkStudent);

    /**
     * Conta quantos alunos já têm um número de aluno começado pelo prefixo
     * indicado (ex: "ALU-2026-"), usado para calcular a próxima sequência
     * anual em {@code StudentService#gerarNumeroAluno()}.
     */
    long countBySudentNumberStartingWith(String prefixo);

    // -------------------------------
    // Contagens para os cartões de estatística
    // -------------------------------

    long countByStatus(StudentStatus status);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.createdAt >= :inicio")
    long countCreatedAfter(@Param("inicio") LocalDateTime inicio);

    // ── adiciona estes métodos à tua interface StudentRepository ──

    @Query("SELECT s FROM Student s WHERE " +
            "(:searchText IS NULL OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            " LOWER(s.sudentNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            " LOWER(s.biNumber) LIKE LOWER(CONCAT('%', :searchText, '%'))) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:gender IS NULL OR s.gender = :gender) AND " +
            "(:birthDateFrom IS NULL OR s.nascDate >= :birthDateFrom) AND " +
            "(:birthDateTo IS NULL OR s.nascDate <= :birthDateTo) AND " +
            "(:studentName IS NULL OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :studentName, '%')))")
    Page<Student> findLazyWithFilters(
            @Param("searchText") String searchText,
            @Param("status") StudentStatus status,
            @Param("gender") Gender gender,
            @Param("birthDateFrom") LocalDate birthDateFrom,
            @Param("birthDateTo") LocalDate birthDateTo,
            @Param("studentName") String studentName,
            Pageable pageable);

    @Query("SELECT COUNT(s) FROM Student s WHERE " +
            "(:searchText IS NULL OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            " LOWER(s.sudentNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            " LOWER(s.biNumber) LIKE LOWER(CONCAT('%', :searchText, '%'))) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:gender IS NULL OR s.gender = :gender) AND " +
            "(:birthDateFrom IS NULL OR s.nascDate >= :birthDateFrom) AND " +
            "(:birthDateTo IS NULL OR s.nascDate <= :birthDateTo) AND " +
            "(:studentName IS NULL OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :studentName, '%')))")
    long countWithFilters(
            @Param("searchText") String searchText,
            @Param("status") StudentStatus status,
            @Param("gender") Gender gender,
            @Param("birthDateFrom") LocalDate birthDateFrom,
            @Param("birthDateTo") LocalDate birthDateTo,
            @Param("studentName") String studentName);

            
}