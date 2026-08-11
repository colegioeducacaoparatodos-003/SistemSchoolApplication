package com.SistemSchool.modulo_dashboard_charts.interfaces;

import java.math.BigDecimal;

public interface MonthlyFinancialProjection {

    String getYearMonth();

    BigDecimal getIncome();

    BigDecimal getExpense();
}