package com.SistemSchool.modulo_Financeiro.repository;

import com.SistemSchool.modulo_Financeiro.dto.CashBoxDTO;
import com.SistemSchool.modulo_Financeiro.interfaces.CashBoxTableProjection;
import com.SistemSchool.modulo_Financeiro.io.CashBoxStatus;
import com.SistemSchool.modulo_Financeiro.model.CashBox;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CashBoxRepository extends JpaRepository<CashBox, Long> {

    // =====================================================
    // Lazy Loading para PrimeFaces DataTable (com filtros)
    // =====================================================

    @Query(value = """
            SELECT
                cb.ph_cash_box AS phCashBox,
                cb.cash_box_number AS cashBoxNumber,
                cb.operator AS operator,
                cb.opening_balance AS openingBalance,
                COALESCE(SUM(
                    CASE
                        WHEN fm.type = 'INCOME'
                        THEN fm.amount
                        ELSE 0
                    END
                ),0) AS totalIncome,
                COALESCE(SUM(
                    CASE
                        WHEN fm.type = 'EXPENSE'
                        THEN fm.amount
                        ELSE 0
                    END
                ),0) AS totalExpense,
                (
                    cb.opening_balance +
                    COALESCE(SUM(
                        CASE
                            WHEN fm.type = 'INCOME'
                            THEN fm.amount
                            ELSE 0
                        END
                    ),0)
                    -
                    COALESCE(SUM(
                        CASE
                            WHEN fm.type = 'EXPENSE'
                            THEN fm.amount
                            ELSE 0
                        END
                    ),0)
                ) AS currentBalance,
                cb.status AS status,
                cb.opening_date AS openingDate,
                cb.closing_date AS closingDate,
                cb.created_at AS createdAt,
                cb.updated_at AS updatedAt
            FROM cash_box cb
            LEFT JOIN financial_movement fm
                ON fm.cash_box_pk = cb.ph_cash_box
            WHERE (:cashBoxNumber IS NULL OR cb.cash_box_number LIKE CONCAT('%', :cashBoxNumber, '%'))
              AND (:operator IS NULL OR cb.operator LIKE CONCAT('%', :operator, '%'))
              AND (:status IS NULL OR cb.status = :status)
              AND (:startDate IS NULL OR cb.opening_date >= :startDate)
              AND (:endDate IS NULL OR cb.opening_date <= :endDate)
            GROUP BY
                cb.ph_cash_box
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT cb.ph_cash_box)
                    FROM cash_box cb
                    WHERE (:cashBoxNumber IS NULL OR cb.cash_box_number LIKE CONCAT('%', :cashBoxNumber, '%'))
                      AND (:operator IS NULL OR cb.operator LIKE CONCAT('%', :operator, '%'))
                      AND (:status IS NULL OR cb.status = :status)
                      AND (:startDate IS NULL OR cb.opening_date >= :startDate)
                      AND (:endDate IS NULL OR cb.opening_date <= :endDate)
                    """,
            nativeQuery = true)
    Page<CashBoxTableProjection> findAllForTable(
            @Param("cashBoxNumber") String cashBoxNumber,
            @Param("operator") String operator,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // =====================================================
    // Lista completa usando DTO
    // =====================================================

    @Query("""
            SELECT new com.SistemSchool.modulo_Financeiro.dto.CashBoxDTO(
                cb.phCashBox,
                cb.cashBoxNumber,
                cb.openingBalance,
                cb.closingBalance,
                cb.operator,
                cb.status,
                cb.openingDate,
                cb.closingDate,
                cb.observation,
                cb.createdAt,
                cb.updatedAt
            )
            FROM CashBox cb
            """)
    List<CashBoxDTO> findAllCashBoxesDTO();

    @Query("SELECT COALESCE(SUM(c.openingBalance + c.totalIncome - c.totalExpense), 0) " +
            "FROM CashBox c WHERE c.status = com.SistemSchool.modulo_Financeiro.io.CashBoxStatus.OPEN")
    BigDecimal sumSaldoCaixasAbertos();

    // =====================================================
    // Consultas utilitárias
    // =====================================================

    List<CashBox> findByStatus(CashBoxStatus status);

    List<CashBox> findByOperator(String operator);

    List<CashBox> findByOpeningDate(LocalDate openingDate);

    List<CashBox> findByOpeningDateBetween(
            LocalDate startDate,
            LocalDate endDate);

    boolean existsByCashBoxNumber(String cashBoxNumber);

    boolean existsByStatus(CashBoxStatus status);

    CashBox findFirstByStatusOrderByOpeningDateDesc(
            CashBoxStatus status);

    @Query("""
            SELECT SUM(cb.openingBalance)
            FROM CashBox cb
            WHERE cb.status =
            com.SistemSchool.modulo_Financeiro.io.CashBoxStatus.OPEN
            """)
    BigDecimal getTotalOpeningBalance();

    // =====================================================
    // NOVOS: Geração automática de número + saldo atual
    // =====================================================

    @Query(value = """
            SELECT cash_box_number 
            FROM cash_box 
            WHERE cash_box_number LIKE CONCAT('CX-', YEAR(CURDATE()), '-%') 
            ORDER BY cash_box_number DESC 
            LIMIT 1
            """, nativeQuery = true)
    String findLastCashBoxNumberOfCurrentYear();

    @Query(value = """
            SELECT 
                (cb.opening_balance + 
                 COALESCE(SUM(CASE WHEN fm.type = 'INCOME' THEN fm.amount ELSE 0 END), 0) -
                 COALESCE(SUM(CASE WHEN fm.type = 'EXPENSE' THEN fm.amount ELSE 0 END), 0)
                ) as currentBalance
            FROM cash_box cb
            LEFT JOIN financial_movement fm ON fm.cash_box_pk = cb.ph_cash_box
            WHERE cb.ph_cash_box = :id
            GROUP BY cb.ph_cash_box
            """, nativeQuery = true)
    BigDecimal getCurrentBalanceById(@Param("id") Long id);
}