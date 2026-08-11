package com.SistemSchool.modulo_Financeiro.lazy;

import com.SistemSchool.modulo_Financeiro.dto.CashBoxDTO;
import com.SistemSchool.modulo_Financeiro.io.CashBoxStatus;
import com.SistemSchool.modulo_Financeiro.service.CashBoxService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CashBoxLazyModel extends LazyDataModel<CashBoxDTO> {

    private static final long serialVersionUID = 1L;

    private final CashBoxService cashBoxService;

    // ─────────────────────────────────────────────────────────────
    // FILTROS AVANÇADOS
    // ─────────────────────────────────────────────────────────────

    private String filterCashBoxNumber;
    private String filterOperator;
    private CashBoxStatus filterStatus;
    private LocalDate filterStartDate;
    private LocalDate filterEndDate;

    public CashBoxLazyModel(CashBoxService cashBoxService) {
        this.cashBoxService = cashBoxService;
    }

    // ─────────────────────────────────────────────────────────────
    // LOAD
    // ─────────────────────────────────────────────────────────────

    @Override
    public List<CashBoxDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy,
            Map<String, FilterMeta> filterBy) {
        int page = first / pageSize;

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        if (sortBy != null && !sortBy.isEmpty()) {
            SortMeta sortMeta = sortBy.values().iterator().next();
            Sort.Direction direction = sortMeta.getOrder().isAscending()
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            sort = Sort.by(direction, sortMeta.getField());
        }

        Map<String, Object> filters = buildFilters();

        Page<CashBoxDTO> result = cashBoxService.findLazy(page, pageSize, sort, filters);

        setRowCount((int) result.getTotalElements());

        return result.getContent();
    }

    // ─────────────────────────────────────────────────────────────
    // COUNT
    // ─────────────────────────────────────────────────────────────

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        Map<String, Object> filters = buildFilters();
        Page<CashBoxDTO> page = cashBoxService.findLazy(0, 1, Sort.unsorted(), filters);
        return (int) page.getTotalElements();
    }

    // ─────────────────────────────────────────────────────────────
    // BUILD FILTERS
    // ─────────────────────────────────────────────────────────────

    private Map<String, Object> buildFilters() {
        Map<String, Object> filters = new HashMap<>();

        if (filterCashBoxNumber != null && !filterCashBoxNumber.isBlank()) {
            filters.put("cashBoxNumber", filterCashBoxNumber);
        }
        if (filterOperator != null && !filterOperator.isBlank()) {
            filters.put("operator", filterOperator);
        }
        if (filterStatus != null) {
            filters.put("status", filterStatus);
        }
        if (filterStartDate != null) {
            filters.put("startDate", filterStartDate);
        }
        if (filterEndDate != null) {
            filters.put("endDate", filterEndDate);
        }

        return filters;
    }

    // ─────────────────────────────────────────────────────────────
    // ROW DATA
    // ─────────────────────────────────────────────────────────────

    @Override
    public CashBoxDTO getRowData(String rowKey) {
        return cashBoxService.getAllCashBoxes()
                .stream()
                .filter(c -> c.getPhCashBox().toString().equals(rowKey))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getRowKey(CashBoxDTO cashBoxDTO) {
        return cashBoxDTO.getPhCashBox() != null
                ? cashBoxDTO.getPhCashBox().toString()
                : null;
    }

    // ─────────────────────────────────────────────────────────────
    // GETTERS & SETTERS
    // ─────────────────────────────────────────────────────────────

    public String getFilterCashBoxNumber() {
        return filterCashBoxNumber;
    }

    public void setFilterCashBoxNumber(String filterCashBoxNumber) {
        this.filterCashBoxNumber = filterCashBoxNumber;
    }

    public String getFilterOperator() {
        return filterOperator;
    }

    public void setFilterOperator(String filterOperator) {
        this.filterOperator = filterOperator;
    }

    public CashBoxStatus getFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(CashBoxStatus filterStatus) {
        this.filterStatus = filterStatus;
    }

    public LocalDate getFilterStartDate() {
        return filterStartDate;
    }

    public void setFilterStartDate(LocalDate filterStartDate) {
        this.filterStartDate = filterStartDate;
    }

    public LocalDate getFilterEndDate() {
        return filterEndDate;
    }

    public void setFilterEndDate(LocalDate filterEndDate) {
        this.filterEndDate = filterEndDate;
    }
}