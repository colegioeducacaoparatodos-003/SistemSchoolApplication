package com.SistemSchool.modulo_dashboard_charts.dto;

import java.math.BigDecimal;
import java.util.List;

import com.SistemSchool.modulo_dashboard_charts.interfaces.MonthlyFinancialProjection;

public class FinancialFilterStatsDTO {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal saldoLiquido;
    private List<CategoryTotalDTO> categoryTotals;
    private List<MonthlyFinancialProjection> monthlyEvolution;

    public FinancialFilterStatsDTO() {
    }

    public FinancialFilterStatsDTO(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal saldoLiquido,
            List<CategoryTotalDTO> categoryTotals,
            List<MonthlyFinancialProjection> monthlyEvolution) {

        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.saldoLiquido = saldoLiquido;
        this.categoryTotals = categoryTotals;
        this.monthlyEvolution = monthlyEvolution;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public BigDecimal getSaldoLiquido() {
        return saldoLiquido;
    }

    public List<CategoryTotalDTO> getCategoryTotals() {
        return categoryTotals;
    }

    public List<MonthlyFinancialProjection> getMonthlyEvolution() {
        return monthlyEvolution;
    }
}