package com.SistemSchool.modulo_dashboard_charts.dto;

import java.time.LocalDate;

public class DashboardFilterDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private String category;
    private String movementType; // "INCOME", "EXPENSE" ou null (todos)

    public DashboardFilterDTO() {
    }

    public boolean hasPeriod() {
        return startDate != null && endDate != null;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }
}