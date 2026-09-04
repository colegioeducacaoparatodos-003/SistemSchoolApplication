package com.SistemSchool.modulo_secrtaria.repository;

import com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO;
import com.SistemSchool.modulo_secrtaria.interfaces.EnrolmentTableProjection;
import com.SistemSchool.modulo_secrtaria.io.EnrolmentType;
import com.SistemSchool.modulo_secrtaria.io.ShiftType;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

@Repository
public interface EnrolmentRepository extends JpaRepository<Enrolment, Long> {

       // -------------------------------
       // Lazy Loading para tabela (nativeQuery)
       // -------------------------------

       @Query(value = """
                        SELECT e.ph_enrolment      AS phEnrolment,
                               e.enrolment_numer   AS enrolmentNumer,
                               e.shift             AS shift,
                               e.enrolment_type    AS enrolmentType,
                               s.pk_student        AS studentPk,
                               s.full_name         AS studentFullName,
                               s.sudent_number     AS studentNumber,
                               sc.pk_school_class  AS schoolclassPk,
                               sc.class_name       AS schoolclassnome,
                               sc.class_code       AS schoolclasscode,
                               e.enrolment_data    AS enrolmentData,
                               e.obs               AS obs,
                               e.created_at        AS createdAt,
                               e.updated_at        AS updatedAt
                        FROM enrolment e
                        INNER JOIN student s ON s.pk_student = e.student_pk
                        INNER JOIN school_class sc ON sc.pk_school_class = e.school_class_pk
                     """, countQuery = "SELECT COUNT(*) FROM enrolment", nativeQuery = true)
       Page<EnrolmentTableProjection> findAllForTable(Pageable pageable);

       // -------------------------------
       // Lista completa com DTO (JPQL)
       // -------------------------------

       @Query("""
                     SELECT new com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO(
                               e.phEnrolment,
                               e.enrolmentNumer,
                               e.shift,
                               e.enrolmentType,
                               e.student.pkStudent,
                               e.student.fullName,
                               e.student.sudentNumber,
                               e.schoolClass.pkSchoolClass,
                               e.schoolClass.className,
                               e.schoolClass.classCode,
                               e.enrolmentData,
                               e.obs,
                               e.createdAt,
                               e.updatedAt
                     ) FROM Enrolment e """)
       List<EnrolmentDTO> findAllEnrolmentsDTO();

       @Query("""
                         SELECT e
                         FROM Enrolment e
                         JOIN FETCH e.student
                         JOIN FETCH e.schoolClass
                         ORDER BY e.enrolmentNumer
                     """)
       List<Enrolment> findAllWithStudent();

       @Query(value = """
                     SELECT e.enrolment_numer
                     FROM enrolment e
                     WHERE e.enrolment_numer LIKE :prefix
                     ORDER BY e.ph_enrolment DESC
                     LIMIT 1
                     """, nativeQuery = true)
       Optional<String> findLastEnrolmentNumberByPrefix(@Param("prefix") String prefix);

       @Query("""
                     SELECT new com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO(
                             e.phEnrolment,
                             e.enrolmentNumer,
                             e.shift,
                             e.enrolmentType,
                             e.student.pkStudent,
                             e.student.fullName,
                             e.student.sudentNumber,
                             e.schoolClass.pkSchoolClass,
                             e.schoolClass.className,
                             e.schoolClass.classCode,
                             e.enrolmentData,
                             e.obs,
                             e.createdAt,
                             e.updatedAt
                     )
                     FROM Enrolment e
                     WHERE e.schoolClass.pkSchoolClass = :schoolClassPk
                     ORDER BY e.student.fullName
                     """)
       List<EnrolmentDTO> findAllEnrolmentsDTOBySchoolClass(@Param("schoolClassPk") Long schoolClassPk);

       @Query("""
                     SELECT e
                     FROM Enrolment e
                     JOIN FETCH e.student
                     JOIN FETCH e.schoolClass
                     WHERE e.schoolClass.pkSchoolClass = :schoolClassPk
                     ORDER BY e.student.fullName
                     """)
       List<Enrolment> findBySchoolClass_PkSchoolClassWithStudent(@Param("schoolClassPk") Long schoolClassPk);

       // NOVO: usado pelo BoletimController para listar as matrículas de um aluno
       // sem cair em LazyInitializationException. Não faz JOIN FETCH de e.student
       // porque o Student já é conhecido pelo chamador (evita fetch duplicado).
       @Query("""
                     SELECT e
                     FROM Enrolment e
                     JOIN FETCH e.schoolClass
                     WHERE e.student.pkStudent = :studentPk
                     ORDER BY e.enrolmentData DESC
                     """)
       List<Enrolment> findByStudent_PkStudentWithSchoolClass(@Param("studentPk") Long studentPk);

       // -------------------------------
       // Queries utilitárias
       // -------------------------------

       List<Enrolment> findByShift(ShiftType shift);

       List<Enrolment> findByEnrolmentType(EnrolmentType enrolmentType);

       List<Enrolment> findByStudent_PkStudent(Long studentPk);

       List<Enrolment> findBySchoolClass_PkSchoolClass(Long schoolClassPk);

       List<Enrolment> findBySchoolClass_ClassCode(String classCode);

       boolean existsByEnrolmentNumer(String enrolmentNumer);

       boolean existsByStudent_PkStudentAndSchoolClass_PkSchoolClassAndShift(Long studentPk, Long schoolClassPk,
                     ShiftType shift);

}