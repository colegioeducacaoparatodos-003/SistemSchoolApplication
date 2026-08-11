package com.SistemSchool.modulo_Recursoa_Humano.repository;

import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_Recursoa_Humano.dto.TeacherDTO;
import com.SistemSchool.modulo_Recursoa_Humano.interfaces.TeacherTableProjection;
import com.SistemSchool.modulo_Recursoa_Humano.io.ContractType;
import com.SistemSchool.modulo_Recursoa_Humano.io.TeacherStatus;
import com.SistemSchool.modulo_Recursoa_Humano.model.Teacher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

        // -------------------------------
        // Lazy Loading para tabela (nativeQuery)
        // -------------------------------

        @Query(value = """
                        SELECT t.pk_teacher         AS pkTeacher,
                               t.teacher_number     AS teacherNumber,
                               t.frist_name         AS fristName,
                               t.last_name          AS lastName,
                               t.gender             AS gender,
                               t.qualification_livel AS qualificationLivel,
                               t.contract_type      AS contractType,
                               t.status             AS status,
                               t.photo_phath        AS photoPhath,
                               t.bi_number          AS biNumber,
                               t.bi_expiry_date     AS biExpiryDate,
                               t.address_street     AS addressStreet,
                               t.address_provice    AS addressProvice,
                               t.base_salary        AS baseSalary,
                               t.email              AS email,
                               t.phone              AS phone,
                               t.mobile_phone       AS mobilePhone,
                               t.obs                AS obs,
                               t.created_at         AS createdAt,
                               t.updated_at         AS updatedAt
                        FROM teacher t
                        """, countQuery = "SELECT COUNT(*) FROM teacher", nativeQuery = true)
        Page<TeacherTableProjection> findAllForTable(Pageable pageable);

        // -------------------------------
        // Lista completa com DTO (JPQL)
        // -------------------------------

        @Query("""
                        SELECT new com.SistemSchool.modulo_Recursoa_Humano.dto.TeacherDTO(
                               t.pkTeacher,
                               t.teacherNumber,
                               t.fristName,
                               t.lastName,
                               t.gender,
                               t.qualificationLivel,
                               t.contractType,
                               t.status,
                               t.photoPhath,
                               t.biNumber,
                               t.biExpiryDate,
                               t.addressStreet,
                               t.addressProvice,
                               t.baseSalary,
                               t.email,
                               t.phone,
                               t.mobilePhone,
                               t.obs,
                               t.createdAt,
                               t.updatedAt
                        )
                        FROM Teacher t
                        """)
        List<TeacherDTO> findAllTeachersDTO();

        // -------------------------------
        // Queries utilitárias
        // -------------------------------

        List<Teacher> findByStatus(TeacherStatus status);

        List<Teacher> findByGender(Gender gender);

        List<Teacher> findByContractType(ContractType contractType);

        long countByStatus(TeacherStatus status);

        List<Teacher> findByAddressProvice(String provincia);

        boolean existsByTeacherNumber(String teacherNumber);

        boolean existsByBiNumber(String biNumber);

        boolean existsByEmail(String email);
}