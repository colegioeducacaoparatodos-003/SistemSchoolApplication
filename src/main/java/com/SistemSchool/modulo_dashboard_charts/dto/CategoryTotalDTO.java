package com.SistemSchool.modulo_dashboard_charts.dto;

import java.math.BigDecimal;

public class CategoryTotalDTO {

    private String category;
    private BigDecimal total;

    public CategoryTotalDTO() {
    }

    public CategoryTotalDTO(String category, BigDecimal total) {
        this.category = category;
        this.total = total;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}