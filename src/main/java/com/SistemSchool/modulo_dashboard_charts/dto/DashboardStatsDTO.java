package com.SistemSchool.modulo_dashboard_charts.dto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardStatsDTO {

    private final long totalStudents;
    private final long totalTeachers;
    private final long totalClasses;
    private final long totalEnrolments;
    private final long totalDisciplines;
    private final BigDecimal revenueThisMonth;
    private final BigDecimal pendingAmount;
    private final BigDecimal cashBoxBalance;

    public DashboardStatsDTO(long totalStudents, long totalTeachers, long totalClasses,
                              long totalEnrolments, long totalDisciplines,
                              BigDecimal revenueThisMonth, BigDecimal pendingAmount,
                              BigDecimal cashBoxBalance) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.totalClasses = totalClasses;
        this.totalEnrolments = totalEnrolments;
        this.totalDisciplines = totalDisciplines;
        this.revenueThisMonth = revenueThisMonth;
        this.pendingAmount = pendingAmount;
        this.cashBoxBalance = cashBoxBalance;
    }

    public long getTotalStudents() { return totalStudents; }
    public long getTotalTeachers() { return totalTeachers; }
    public long getTotalClasses() { return totalClasses; }
    public long getTotalEnrolments() { return totalEnrolments; }
    public long getTotalDisciplines() { return totalDisciplines; }
    public BigDecimal getRevenueThisMonth() { return revenueThisMonth; }
    public BigDecimal getPendingAmount() { return pendingAmount; }
    public BigDecimal getCashBoxBalance() { return cashBoxBalance; }

    public String getRevenueThisMonthFormatted() { return formatKz(revenueThisMonth); }
    public String getPendingAmountFormatted() { return formatKz(pendingAmount); }
    public String getCashBoxBalanceFormatted() { return formatKz(cashBoxBalance); }

    private String formatKz(BigDecimal valor) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("pt", "AO"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(valor != null ? valor : BigDecimal.ZERO);
    }
}