package com.SistemSchool.modulo_secrtaria.lazy;

import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.modulo_secrtaria.service.StudentService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentLazyModel extends LazyDataModel<StudentDTO> {

    private static final long serialVersionUID = 1L;

    private final StudentService studentService;

    public StudentLazyModel(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public List<StudentDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy,
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

        Map<String, Object> filters = convertFilters(filterBy);
        Page<StudentDTO> result = studentService.findLazy(page, pageSize, sort, filters);

        setRowCount((int) result.getTotalElements());

        return result.getContent();
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        Map<String, Object> filters = convertFilters(filterBy);
        Page<StudentDTO> page = studentService.findLazy(0, 1, Sort.unsorted(), filters);
        return (int) page.getTotalElements();
    }

    @Override
    public StudentDTO getRowData(String rowKey) {
        return studentService.getAllStudents()
                .stream()
                .filter(s -> s.getPkStudent().toString().equals(rowKey))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getRowKey(StudentDTO studentDTO) {
        return studentDTO.getPkStudent() != null
                ? studentDTO.getPkStudent().toString()
                : null;
    }

    /**
     * Limpa filtros internos (compatibilidade com EnrolmentLazyModel).
     */
    public void clearFilters() {
        // O LazyDataModel do PrimeFaces recebe filtros via load/count;
        // este método existe apenas para compatibilidade com o controller.
    }

    private Map<String, Object> convertFilters(Map<String, FilterMeta> filterBy) {
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