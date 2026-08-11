package com.SistemSchool.modulo_secrtaria.lazy;

import com.SistemSchool.modulo_secrtaria.dto.EnrolmentDTO;
import com.SistemSchool.modulo_secrtaria.service.EnrolmentService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnrolmentLazyModel extends LazyDataModel<EnrolmentDTO> {

    private static final long serialVersionUID = 1L;

    private final EnrolmentService enrolmentService;
    private Map<String, Object> activeFilters = new HashMap<>();

    public EnrolmentLazyModel(EnrolmentService enrolmentService) {
        this.enrolmentService = enrolmentService;
    }

    @Override
    public List<EnrolmentDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy,
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

        activeFilters = extractFilters(filterBy);
        Page<EnrolmentDTO> result = enrolmentService.findLazy(page, pageSize, sort, activeFilters);

        setRowCount((int) result.getTotalElements());

        return result.getContent();
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        Map<String, Object> filters = extractFilters(filterBy);
        Page<EnrolmentDTO> page = enrolmentService.findLazy(0, 1, Sort.unsorted(), filters);
        return (int) page.getTotalElements();
    }

    private Map<String, Object> extractFilters(Map<String, FilterMeta> filterBy) {
        Map<String, Object> filters = new HashMap<>();
        if (filterBy == null) {
            return filters;
        }

        for (FilterMeta meta : filterBy.values()) {
            Object value = meta.getFilterValue();
            if (value != null && !value.toString().isBlank()) {
                filters.put(meta.getField(), value);
            }
        }

        return filters;
    }

    public void clearFilters() {
        activeFilters = new HashMap<>();
    }

    @Override
    public EnrolmentDTO getRowData(String rowKey) {
        return enrolmentService.getAllEnrolments()
                .stream()
                .filter(e -> e.getPhEnrolment().toString().equals(rowKey))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getRowKey(EnrolmentDTO enrolmentDTO) {
        return enrolmentDTO.getPhEnrolment() != null
                ? enrolmentDTO.getPhEnrolment().toString()
                : null;
    }
}