package com.SistemSchool.modulo_pedagogico.lazy;

import com.SistemSchool.modulo_pedagogico.dto.ScheduleDTO;
import com.SistemSchool.modulo_pedagogico.service.ScheduleService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleLazyModel extends LazyDataModel<ScheduleDTO> {

    private static final long serialVersionUID = 1L;

    private final ScheduleService scheduleService;
    private Map<String, Object> activeFilters = new HashMap<>();

    public ScheduleLazyModel(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Override
    public List<ScheduleDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy,
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
        Page<ScheduleDTO> result = scheduleService.findLazy(page, pageSize, sort, activeFilters);

        setRowCount((int) result.getTotalElements());

        return result.getContent();
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        Map<String, Object> filters = extractFilters(filterBy);
        Page<ScheduleDTO> page = scheduleService.findLazy(0, 1, Sort.unsorted(), filters);
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
    public ScheduleDTO getRowData(String rowKey) {
        return scheduleService.getAllSchedules()
                .stream()
                .filter(s -> s.getPkSchedule().toString().equals(rowKey))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getRowKey(ScheduleDTO scheduleDTO) {
        return scheduleDTO.getPkSchedule() != null
                ? scheduleDTO.getPkSchedule().toString()
                : null;
    }
}