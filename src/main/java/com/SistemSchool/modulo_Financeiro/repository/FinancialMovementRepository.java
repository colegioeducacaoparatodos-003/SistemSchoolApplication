package com.SistemSchool.modulo_Financeiro.repository;

import com.SistemSchool.modulo_Financeiro.dto.FinancialMovementDTO;
import com.SistemSchool.modulo_Financeiro.interfaces.FinancialMovementTableProjection;
import com.SistemSchool.modulo_Financeiro.io.MovementStatus;
import com.SistemSchool.modulo_Financeiro.io.MovementType;
import com.SistemSchool.modulo_Financeiro.model.FinancialMovement;

import org.springframework.data.repository.query.Param;
import com.SistemSchool.modulo_dashboard_charts.dto.CategoryTotalDTO;
import com.SistemSchool.modulo_dashboard_charts.interfaces.MonthlyFinancialProjection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FinancialMovementRepository
                extends JpaRepository<FinancialMovement, Long> {

        // =====================================================
        // Lazy Loading para PrimeFaces DataTable
        // =====================================================

        @Query(value = """
                        SELECT
                            fm.ph_movement AS phMovement,
                            fm.movement_number AS movementNumber,
                            cb.ph_cash_box AS cashBoxPk,
                            cb.cash_box_number AS cashBoxNumber,
                            pg.pk_pagamento AS pagamentoPk,
                            pg.numero_documento AS pagamentoNumeroDocumento,
                            fm.description AS description,
                            fm.amount AS amount,
                            fm.type AS type,
                            fm.category AS category,
                            fm.status AS status,
                            fm.responsible AS responsible,
                            fm.observation AS observation,
                            fm.movement_date AS movementDate,
                            fm.created_at AS createdAt,
                            fm.updated_at AS updatedAt

                        FROM financial_movement fm
                        INNER JOIN cash_box cb
                        ON cb.ph_cash_box = fm.cash_box_pk
                        LEFT JOIN pagamento pg
                        ON pg.pk_pagamento = fm.pagamento_pk
                        """, countQuery = """
                        SELECT COUNT(*)
                        FROM financial_movement """, nativeQuery = true)
        Page<FinancialMovementTableProjection> findAllForTable(Pageable pageable);

        // =====================================================
        // Lista completa usando DTO
        // =====================================================

        /**
         * LEFT JOIN fm.pagamento pg é obrigatório aqui: movimentos lançados
         * manualmente (via FinancialMovementController.saveMovement) podem não
         * ter pagamento associado. Usar fm.pagamento.pkPagamento diretamente
         * geraria um INNER JOIN implícito e excluiria esses registos da
         * listagem.
         *
         * A ordem dos argumentos abaixo tem de bater exatamente com a ordem do
         * construtor em FinancialMovementDTO (type, category, status).
         */
        @Query("""
                        SELECT new com.SistemSchool.modulo_Financeiro.dto.FinancialMovementDTO(
                                fm.phMovement,
                                fm.movementNumber,
                                fm.cashBox.phCashBox,
                                fm.cashBox.cashBoxNumber,
                                pg.pkPagamento,
                                pg.numeroDocumento,
                                fm.description,
                                fm.amount,
                                fm.type,
                                fm.category,
                                fm.status,
                                fm.responsible,
                                fm.observation,
                                fm.movementDate,
                                fm.createdAt,
                                fm.updatedAt
                            )
                            FROM FinancialMovement fm
                            LEFT JOIN fm.pagamento pg
                            """)
        List<FinancialMovementDTO> findAllFinancialMovementsDTO();

        // =====================================================
        // Consultas utilitárias
        // =====================================================

        /**
         * Movimentos de um caixa
         */
        List<FinancialMovement> findByCashBox_PhCashBox(
                        Long cashBoxPk);

        /**
         * Movimentos relacionados a pagamento
         */
        List<FinancialMovement> findByPagamento_PkPagamento(
                        Long pagamentoPk);

        /**
         * Entradas ou saídas
         */
        List<FinancialMovement> findByType(
                        MovementType type);

        /**
         * Estado do movimento
         */
        List<FinancialMovement> findByStatus(
                        MovementStatus status);

        /**
         * Categoria financeira
         *
         * PROPINA
         * SALARIO
         * MATERIAL
         */
        List<FinancialMovement> findByCategory(
                        String category);

        /**
         * Pesquisa por período
         */
        List<FinancialMovement> findByMovementDateBetween(
                        LocalDateTime startDate,
                        LocalDateTime endDate);

        /**
         * Validar número do movimento
         */
        boolean existsByMovementNumber(
                        String movementNumber);

        // =====================================================
        // Relatórios Financeiros
        // =====================================================

        /**
         * Total de entradas
         */
        @Query("""

                        SELECT COALESCE(SUM(fm.amount),0)

                        FROM FinancialMovement fm

                        WHERE fm.type =
                        com.SistemSchool.modulo_Financeiro.io.MovementType.INCOME


                        AND fm.status =
                        com.SistemSchool.modulo_Financeiro.io.MovementStatus.ACTIVE


                        """)

        BigDecimal getTotalIncome();

        /**
         * Total de despesas
         */
        @Query("""

                        SELECT COALESCE(SUM(fm.amount),0)

                        FROM FinancialMovement fm

                        WHERE fm.type =
                        com.SistemSchool.modulo_Financeiro.io.MovementType.EXPENSE


                        AND fm.status =
                        com.SistemSchool.modulo_Financeiro.io.MovementStatus.ACTIVE


                        """)

        BigDecimal getTotalExpense();

        // =====================================================
        // Filtros do Dashboard
        // =====================================================

        @Query("""
                        SELECT COALESCE(SUM(fm.amount), 0)
                        FROM FinancialMovement fm
                        WHERE fm.type = com.SistemSchool.modulo_Financeiro.io.MovementType.INCOME
                        AND fm.status = com.SistemSchool.modulo_Financeiro.io.MovementStatus.ACTIVE
                        AND fm.movementDate BETWEEN :start AND :end
                        AND (:category IS NULL OR fm.category = :category)
                        """)
        BigDecimal getTotalIncomeByPeriod(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("category") String category);

        @Query("""
                        SELECT COALESCE(SUM(fm.amount), 0)
                        FROM FinancialMovement fm
                        WHERE fm.type = com.SistemSchool.modulo_Financeiro.io.MovementType.EXPENSE
                        AND fm.status = com.SistemSchool.modulo_Financeiro.io.MovementStatus.ACTIVE
                        AND fm.movementDate BETWEEN :start AND :end
                        AND (:category IS NULL OR fm.category = :category)
                        """)
        BigDecimal getTotalExpenseByPeriod(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("category") String category);

        @Query("""
                        SELECT new com.SistemSchool.modulo_dashboard_charts.dto.CategoryTotalDTO(
                            fm.category, SUM(fm.amount)
                        )
                        FROM FinancialMovement fm
                        WHERE fm.status = com.SistemSchool.modulo_Financeiro.io.MovementStatus.ACTIVE
                        AND fm.movementDate BETWEEN :start AND :end
                        AND (:type IS NULL OR fm.type = :type)
                        GROUP BY fm.category
                        ORDER BY SUM(fm.amount) DESC
                        """)
        List<CategoryTotalDTO> getTotalsByCategory(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("type") MovementType type);

        // Evolução mensal (últimos N meses) para gráfico de linha
        @Query(value = """
                        SELECT
                            DATE_FORMAT(fm.movement_date, '%Y-%m') AS yearMonth,
                            COALESCE(SUM(CASE WHEN fm.type = 'INCOME' THEN fm.amount ELSE 0 END), 0) AS income,
                            COALESCE(SUM(CASE WHEN fm.type = 'EXPENSE' THEN fm.amount ELSE 0 END), 0) AS expense
                        FROM financial_movement fm
                        WHERE fm.status = 'ACTIVE'
                        AND fm.movement_date >= :start
                        GROUP BY DATE_FORMAT(fm.movement_date, '%Y-%m')
                        ORDER BY yearMonth
                        """, nativeQuery = true)
        List<MonthlyFinancialProjection> getMonthlyEvolution(@Param("start") LocalDateTime start);

        // Lista de categorias distintas, para popular o <p:selectOneMenu> do filtro
        @Query("SELECT DISTINCT fm.category FROM FinancialMovement fm ORDER BY fm.category")
        List<String> findDistinctCategories();

}