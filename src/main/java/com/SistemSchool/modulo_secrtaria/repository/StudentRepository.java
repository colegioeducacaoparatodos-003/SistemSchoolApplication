package com.SistemSchool.modulo_secrtaria.repository;

import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_secrtaria.interfaces.StudentTableProjection;
import com.SistemSchool.modulo_secrtaria.io.StudentStatus;
import com.SistemSchool.modulo_secrtaria.model.Student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

       // -------------------------------
       // Lazy Loading para tabela (nativeQuery)
       // -------------------------------

       @Query(value = """
                     SELECT s.pk_student       AS pkStudent,
                            s.sudent_number    AS sudentNumber,
                            s.frist_name       AS fristName,
                            s.last_name        AS lastName,
                            s.full_name        AS fullName,
                            s.gender           AS gender,
                            s.bi_number        AS biNumber,
                            s.nasc_date        AS nascDate,
                            s.bi_expiry_data   AS biExpiryData,
                            s.address_street   AS addressStreet,
                            s.address_provice  AS addressProvice,
                            s.name_father      AS nameFather,
                            s.name_mather      AS nameMather,
                            s.email            AS email,
                            s.phone_1          AS phone_1,
                            s.phone_2          AS phone_2,
                            s.upload_photo     AS uploadPhoto,
                            s.status           AS status,
                            s.obs              AS obs,
                            s.created_at       AS createdAt,
                            s.updated_at       AS updatedAt
                     FROM student s
                     """, countQuery = "SELECT COUNT(*) FROM student", nativeQuery = true)
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

       @Query(value = """
                     SELECT s.sudent_number
                     FROM student s
                     WHERE s.sudent_number LIKE :prefix
                     ORDER BY s.pk_student DESC
                     LIMIT 1
                     """, nativeQuery = true)
       Optional<String> findLastStudentNumberByPrefix(@Param("prefix") String prefix);
       // -------------------------------
       // Queries utilitárias
       // -------------------------------

       List<Student> findByStatus(StudentStatus status);

       List<Student> findByGender(Gender gender);

       List<Student> findByAddressProvice(String provincia);

       boolean existsBySudentNumber(String sudentNumber);

       boolean existsByBiNumber(String biNumber);

       boolean existsByEmail(String email);

       long countByStatus(StudentStatus status); 
}