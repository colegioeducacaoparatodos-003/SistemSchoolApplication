package com.SistemSchool.modulo_Financeiro.lazy;

import com.SistemSchool.modulo_Financeiro.dto.FeeDTO;
import com.SistemSchool.modulo_Financeiro.service.FeeService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeeLazyModel extends LazyDataModel<FeeDTO> {

    private static final long serialVersionUID = 1L;

    private final FeeService feeService;
    private List<FeeDTO> preFilteredList;

    public FeeLazyModel(FeeService feeService) {
        this.feeService = feeService;
    }

    public FeeLazyModel(FeeService feeService, List<FeeDTO> preFilteredList) {
        this.feeService = feeService;
        this.preFilteredList = preFilteredList;
    }

    @Override
    public List<FeeDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy,
            Map<String, FilterMeta> filterBy) {

        if (preFilteredList != null) {
            setRowCount(preFilteredList.size());
            if (first >= preFilteredList.size()) {
                return new ArrayList<>();
            }

            List<FeeDTO> sorted = new ArrayList<>(preFilteredList);
            if (sortBy != null && !sortBy.isEmpty()) {
                SortMeta sortMeta = sortBy.values().iterator().next();
                String field = sortMeta.getField();
                boolean asc = sortMeta.getOrder().isAscending();
                sorted.sort((a, b) -> {
                    try {
                        Object va = getFieldValue(a, field);
                        Object vb = getFieldValue(b, field);
                        if (va == null && vb == null) return 0;
                        if (va == null) return asc ? -1 : 1;
                        if (vb == null) return asc ? 1 : -1;
                        if (va instanceof Comparable && vb instanceof Comparable) {
                            int cmp = ((Comparable) va).compareTo(vb);
                            return asc ? cmp : -cmp;
                        }
                        return asc ? va.toString().compareTo(vb.toString())
                                   : vb.toString().compareTo(va.toString());
                    } catch (Exception e) {
                        return 0;
                    }
                });
            }

            int toIndex = Math.min(first + pageSize, sorted.size());
            return sorted.subList(first, toIndex);
        }

        int page = first / pageSize;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        if (sortBy != null && !sortBy.isEmpty()) {
            SortMeta sortMeta = sortBy.values().iterator().next();
            Sort.Direction direction = sortMeta.getOrder().isAscending()
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            String field = mapSortField(sortMeta.getField());
            sort = Sort.by(direction, field);
        }

        Map<String, Object> filters = extractFilters(filterBy);
        Page<FeeDTO> result = feeService.findLazy(page, pageSize, sort, filters);
        setRowCount((int) result.getTotalElements());
        return result.getContent();
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        if (preFilteredList != null) {
            return preFilteredList.size();
        }
        Map<String, Object> filters = extractFilters(filterBy);
        Page<FeeDTO> page = feeService.findLazy(0, 1, Sort.unsorted(), filters);
        return (int) page.getTotalElements();
    }

    @Override
    public FeeDTO getRowData(String rowKey) {
        return feeService.getAllFees()
                .stream()
                .filter(f -> f.getPhFee().toString().equals(rowKey))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getRowKey(FeeDTO feeDTO) {
        return feeDTO.getPhFee() != null
                ? feeDTO.getPhFee().toString()
                : null;
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private Object getFieldValue(FeeDTO dto, String field) {
        if (field == null || dto == null) return null;
        return switch (field) {
            case "phFee" -> dto.getPhFee();
            case "feeCode" -> dto.getFeeCode();
            case "description" -> dto.getDescription();
            case "feeType" -> dto.getFeeType();
            case "schoolClassName" -> dto.getSchoolClassName();
            case "schoolYear" -> dto.getSchoolYear();
            case "amount" -> dto.getAmount();
            case "status" -> dto.getStatus();
            case "startDate" -> dto.getStartDate();
            case "endDate" -> dto.getEndDate();
            case "createdAt" -> dto.getCreatedAt();
            case "updatedAt" -> dto.getUpdatedAt();
            default -> null;
        };
    }

    private String mapSortField(String field) {
        if (field == null) {
            return "createdAt";
        }
        return switch (field) {
            case "schoolClassName" -> "schoolClass.name";
            case "phFee", "feeCode", "description", "feeType", "schoolYear",
                 "amount", "startDate", "endDate", "status",
                 "createdAt", "updatedAt" -> field;
            default -> "createdAt";
        };
    }

    private Map<String, Object> extractFilters(Map<String, FilterMeta> filterBy) {
        Map<String, Object> filters = new HashMap<>();
        if (filterBy != null) {
            for (FilterMeta meta : filterBy.values()) {
                Object value = meta.getFilterValue();
                if (value != null && !value.toString().isBlank()) {
                    filters.put(meta.getField(), value);
                }
            }
        }
        return filters;
    }
}