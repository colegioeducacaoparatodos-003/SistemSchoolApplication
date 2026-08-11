package com.SistemSchool.modulo_pedagogico.lazy;

import com.SistemSchool.modulo_pedagogico.dto.DisciplineDTO;
import com.SistemSchool.modulo_pedagogico.service.DisciplineService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisciplineLazyModel extends LazyDataModel<DisciplineDTO> {

    private static final long serialVersionUID = 1L;
    
    private final DisciplineService disciplineService;

    public DisciplineLazyModel(DisciplineService disciplineService) {
        this.disciplineService = disciplineService;
    }

    @Override
    public List<DisciplineDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy,
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

        Page<DisciplineDTO> result = disciplineService.findLazy(page, pageSize, sort, null);

        setRowCount((int) result.getTotalElements());

        return result.getContent();
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        Map<String, Object> filters = new HashMap<>();
        if (filterBy != null) {
            for (FilterMeta meta : filterBy.values()) {
                Object value = meta.getFilterValue();
                if (value != null && !value.toString().isBlank()) {
                    filters.put(meta.getField(), value);
                }
            }
        }
        Page<DisciplineDTO> page = disciplineService.findLazy(0, 1, Sort.unsorted(), filters);
        return (int) page.getTotalElements();
    }

    @Override
    public DisciplineDTO getRowData(String rowKey) {
        return disciplineService.getAllDisciplines()
                .stream()
                .filter(d -> d.getPkDiscipline().toString().equals(rowKey))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getRowKey(DisciplineDTO disciplineDTO) {
        return disciplineDTO.getPkDiscipline() != null
                ? disciplineDTO.getPkDiscipline().toString()
                : null;
    }
}