package com.SistemSchool.modulo_secrtaria.repository;

import com.SistemSchool.modulo_secrtaria.dto.PagamentoDTO;
import com.SistemSchool.modulo_secrtaria.interfaces.PagamentoTableProjection;
import com.SistemSchool.modulo_secrtaria.io.EstadoPagamento;
import com.SistemSchool.modulo_secrtaria.io.FormaPagamento;
import com.SistemSchool.modulo_secrtaria.io.MesReferencia;
import com.SistemSchool.modulo_secrtaria.model.Pagamento;

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
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    // =====================================================
    // Lazy Loading para PrimeFaces DataTable
    // =====================================================
    @Query(value = """
        SELECT
            p.pk_pagamento AS pkPagamento,
            p.numero_documento AS numeroDocumento,
            e.ph_enrolment AS enrolmentPk,
            e.enrolment_numer AS enrolmentNumero,
            s.full_name AS studentName,
            f.ph_fee AS feePk,
            cb.ph_cash_box AS cashBoxPk,
            cb.cash_box_number AS cashBoxNumber,
            p.valor AS valor,
            p.multa AS multa,
            p.total AS total,
            p.data_emissao AS dataEmissao,
            p.data_pagamento AS dataPagamento,
            p.forma_pagamento AS formaPagamento,
            p.estado AS estado,
            p.mes_referencia AS mesReferencia,
            p.referencia AS referencia,
            p.observacao AS observacao,
            p.created_at AS createdAt,
            p.updated_at AS updatedAt
        FROM pagamento p
        INNER JOIN enrolment e ON e.ph_enrolment = p.enrolment_pk
        INNER JOIN student s ON s.pk_student = e.student_pk
        INNER JOIN fee f ON f.ph_fee = p.fee_pk
        INNER JOIN cash_box cb ON cb.ph_cash_box = p.cash_box_pk
        """,
        countQuery = "SELECT COUNT(*) FROM pagamento p",
        nativeQuery = true)
    Page<PagamentoTableProjection> findAllForTable(Pageable pageable);

    // =====================================================
    // Lista completa usando DTO
    // =====================================================
    @Query("""
        SELECT new com.SistemSchool.modulo_secrtaria.dto.PagamentoDTO(
            p.pkPagamento, p.numeroDocumento,
            p.enrolment.phEnrolment, p.enrolment.enrolmentNumer, p.enrolment.student.fullName,
            p.fee.phFee, null,
            p.cashBox.phCashBox, p.cashBox.cashBoxNumber,
            p.valor, p.multa, p.total,
            p.dataEmissao, p.dataPagamento,
            p.formaPagamento, p.estado, p.mesReferencia,
            p.referencia, p.observacao,
            p.createdAt, p.updatedAt
        ) FROM Pagamento p
        """)
    List<PagamentoDTO> findAllPagamentosDTO();

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pagamento p " +
           "WHERE p.estado = :estado AND p.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal sumTotalByEstadoAndPeriodo(@Param("estado") EstadoPagamento estado,
                                          @Param("inicio") LocalDateTime inicio,
                                          @Param("fim") LocalDateTime fim);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pagamento p WHERE p.estado = :estado")
    BigDecimal sumTotalByEstado(@Param("estado") EstadoPagamento estado);

    // =====================================================
    // Filtros paginados
    // =====================================================
    @Query("""
        SELECT p FROM Pagamento p
        WHERE (:numeroDocumento IS NULL OR LOWER(p.numeroDocumento) LIKE LOWER(CONCAT('%', :numeroDocumento, '%')))
          AND (:studentName IS NULL OR LOWER(p.enrolment.student.fullName) LIKE LOWER(CONCAT('%', :studentName, '%')))
          AND (:formaPagamento IS NULL OR p.formaPagamento = :formaPagamento)
          AND (:estado IS NULL OR p.estado = :estado)
          AND (:dataInicio IS NULL OR p.dataPagamento >= :dataInicio)
          AND (:dataFim IS NULL OR p.dataPagamento <= :dataFim)
        ORDER BY p.createdAt DESC
        """)
    Page<Pagamento> findComFiltros(
            @Param("numeroDocumento") String numeroDocumento,
            @Param("studentName") String studentName,
            @Param("formaPagamento") FormaPagamento formaPagamento,
            @Param("estado") EstadoPagamento estado,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable);

    // =====================================================
    // Consultas utilitárias
    // =====================================================
    List<Pagamento> findByEnrolment_PhEnrolment(Long enrolmentPk);
    List<Pagamento> findByEnrolment_Student_PkStudent(Long studentPk);
    List<Pagamento> findByFee_PhFee(Long feePk);
    List<Pagamento> findByCashBox_PhCashBox(Long cashBoxPk);
    List<Pagamento> findByEstado(EstadoPagamento estado);
    List<Pagamento> findByFormaPagamento(FormaPagamento formaPagamento);
    List<Pagamento> findByDataPagamentoBetween(LocalDateTime start, LocalDateTime end);
    boolean existsByNumeroDocumento(String numeroDocumento);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pagamento p WHERE p.estado = com.SistemSchool.modulo_secrtaria.io.EstadoPagamento.PAGO")
    BigDecimal getTotalConfirmado();

    @Query(value = """
        SELECT COALESCE(MAX(CAST(SUBSTRING(p.numero_documento, 10) AS INTEGER)), 0)
        FROM pagamento p WHERE p.numero_documento LIKE CONCAT('PAG-', :year, '-%')
        """, nativeQuery = true)
    long findMaxSequenceForYear(@Param("year") int year);

    // =====================================================
    // Verificação de mensalidade já paga no mês
    // =====================================================
    @Query("""
        SELECT COUNT(p) > 0 FROM Pagamento p
        WHERE p.enrolment.phEnrolment = :enrolmentPk
          AND p.mesReferencia = :mesReferencia
          AND p.estado = com.SistemSchool.modulo_secrtaria.io.EstadoPagamento.PAGO
          AND (:excludeId IS NULL OR p.pkPagamento <> :excludeId)
        """)
    boolean existsPagamentoPagoByEnrolmentAndMes(@Param("enrolmentPk") Long enrolmentPk,
                                                  @Param("mesReferencia") MesReferencia mesReferencia,
                                                  @Param("excludeId") Long excludeId);
}