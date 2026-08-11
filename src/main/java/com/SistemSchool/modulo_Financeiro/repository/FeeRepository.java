package com.SistemSchool.modulo_Financeiro.repository;

import com.SistemSchool.modulo_Financeiro.dto.FeeDTO;
import com.SistemSchool.modulo_Financeiro.interfaces.FeeTableProjection;
import com.SistemSchool.modulo_Financeiro.io.FeeStatus;
import com.SistemSchool.modulo_Financeiro.io.FeeType;
import com.SistemSchool.modulo_Financeiro.model.Fee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {

    // =====================================================
    // Lazy Loading para PrimeFaces DataTable
    // =====================================================
    @Query(value = """
            SELECT
                f.ph_fee AS phFee,
                f.fee_code AS feeCode,
                f.description AS description,
                f.fee_type AS feeType,
                sc.pk_school_class AS schoolClassPk,
                sc.class_code AS schoolClassName,
                f.school_year AS schoolYear,
                f.amount AS amount,
                f.start_date AS startDate,
                f.end_date AS endDate,
                f.status AS status,
                f.obs AS obs,
                f.created_at AS createdAt,
                f.updated_at AS updatedAt
            FROM fee f
            INNER JOIN school_class sc ON sc.pk_school_class = f.school_class_pk
            """, countQuery = "SELECT COUNT(*) FROM fee", nativeQuery = true)
    Page<FeeTableProjection> findAllForTable(Pageable pageable);

    @Query("""
            SELECT new com.SistemSchool.modulo_Financeiro.dto.FeeDTO(
                f.phFee, f.feeCode, f.description, f.feeType,
                f.schoolClass.pkSchoolClass, f.schoolClass.classCode,
                f.schoolYear, f.amount, f.startDate, f.endDate,
                f.status, f.obs, f.createdAt, f.updatedAt
            )
            FROM Fee f
            """)
    List<FeeDTO> findAllFeesDTO();

    // impede duas taxas do mesmo tipo, para a mesma turma, no mesmo ano letivo
    boolean existsBySchoolClass_PkSchoolClassAndFeeTypeAndSchoolYear(
            Long schoolClassPk, FeeType feeType, Integer schoolYear);

    List<Fee> findByFeeType(FeeType feeType);

    // =====================================================
    // Geração automática do feeCode
    // =====================================================
    @Query(value = """
            SELECT f.fee_code
            FROM fee f
            WHERE f.fee_code LIKE :prefix
            ORDER BY f.ph_fee DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> findLastFeeCodeByPrefix(@Param("prefix") String prefix);

    // =====================================================
    // Consultas utilitárias
    // =====================================================

    List<Fee> findByStatus(FeeStatus status);

    List<Fee> findBySchoolYear(Integer schoolYear);

    List<Fee> findByEndDate(LocalDateTime endDate);

    boolean existsByFeeCode(String feeCode);

    List<Fee> findBySchoolClass_PkSchoolClass(Long schoolClassPk);
}