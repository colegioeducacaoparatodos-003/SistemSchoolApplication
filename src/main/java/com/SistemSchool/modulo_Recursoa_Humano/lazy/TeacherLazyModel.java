package com.SistemSchool.modulo_Recursoa_Humano.lazy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.SistemSchool.modulo_Recursoa_Humano.controller.TeacherController;
import com.SistemSchool.modulo_Recursoa_Humano.dto.TeacherDTO;

public class TeacherLazyModel extends LazyDataModel<TeacherDTO> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TeacherController controller;

    public TeacherLazyModel(TeacherController controller) {
        this.controller = controller;
    }

    @Override
    public List<TeacherDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy,
            Map<String, FilterMeta> filterBy) {

        int page = pageSize > 0 ? first / pageSize : 0;

        Sort sort = Sort.by(Direction.DESC, "createdAt");
        if (sortBy != null && !sortBy.isEmpty()) {
            SortMeta meta = sortBy.values().iterator().next();
            Direction direction = meta.getOrder() == SortOrder.ASCENDING ? Direction.ASC : Direction.DESC;
            sort = Sort.by(direction, meta.getField());
        }

        Page<TeacherDTO> result = controller.getTeacherService().findLazy(page, pageSize, sort,
                controller.getFilterTeacherNumber(), controller.getFilterName(), controller.getFilterStatus());

        this.setRowCount((int) result.getTotalElements());

        return result.getContent();
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        return (int) controller.getTeacherService().countFiltered(
                controller.getFilterTeacherNumber(), controller.getFilterName(), controller.getFilterStatus());
    }

}