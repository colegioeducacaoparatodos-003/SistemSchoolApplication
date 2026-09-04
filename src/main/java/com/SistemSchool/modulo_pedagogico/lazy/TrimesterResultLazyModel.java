package com.SistemSchool.modulo_pedagogico.lazy;

import com.SistemSchool.modulo_pedagogico.dto.TrimesterResultDTO;
import com.SistemSchool.modulo_pedagogico.service.TrimesterResultService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrimesterResultLazyModel extends LazyDataModel<TrimesterResultDTO> {

    private static final long serialVersionUID = 1L;

    private final TrimesterResultService trimesterResultService;
    private Map<String, Object> activeFilters = new HashMap<>();

    public TrimesterResultLazyModel(TrimesterResultService trimesterResultService) {
        this.trimesterResultService = trimesterResultService;
    }

    @Override
    public List<TrimesterResultDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy,
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
        Page<TrimesterResultDTO> result = trimesterResultService.findLazy(page, pageSize, sort, activeFilters);

        setRowCount((int) result.getTotalElements());

        return result.getContent();
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        Map<String, Object> filters = extractFilters(filterBy);
        Page<TrimesterResultDTO> page = trimesterResultService.findLazy(0, 1, Sort.unsorted(), filters);
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
    public TrimesterResultDTO getRowData(String rowKey) {
        return trimesterResultService.getAllResults()
                .stream()
                .filter(tr -> tr.getPkTrimesterResult().toString().equals(rowKey))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getRowKey(TrimesterResultDTO trimesterResultDTO) {
        return trimesterResultDTO.getPkTrimesterResult() != null
                ? trimesterResultDTO.getPkTrimesterResult().toString()
                : null;
    }
}