package com.SistemSchool.modulo_secrtaria.lazy;

import com.SistemSchool.modulo_secrtaria.dto.StudentDTO;
import com.SistemSchool.io.Gender;
import com.SistemSchool.modulo_secrtaria.io.StudentStatus;
import com.SistemSchool.modulo_secrtaria.service.StudentService;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentLazyModel extends LazyDataModel<StudentDTO> {

    private final StudentService service;

    private String searchText;
    private StudentStatus filterStatus;
    private Gender filterGender;
    private LocalDate filterBirthDateFrom;
    private LocalDate filterBirthDateTo;
    private String filterStudentName;

    public StudentLazyModel(StudentService service) {
        this.service = service;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public void setFilterStatus(StudentStatus filterStatus) {
        this.filterStatus = filterStatus;
    }

    public void setFilterGender(Gender filterGender) {
        this.filterGender = filterGender;
    }

    public void setFilterBirthDateFrom(LocalDate filterBirthDateFrom) {
        this.filterBirthDateFrom = filterBirthDateFrom;
    }

    public void setFilterBirthDateTo(LocalDate filterBirthDateTo) {
        this.filterBirthDateTo = filterBirthDateTo;
    }

    public void setFilterStudentName(String filterStudentName) {
        this.filterStudentName = filterStudentName;
    }

    public void clearFilters() {
        this.searchText = null;
        this.filterStatus = null;
        this.filterGender = null;
        this.filterBirthDateFrom = null;
        this.filterBirthDateTo = null;
        this.filterStudentName = null;
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        return (int) service.countWithFilters(
                searchText, filterStatus, filterGender,
                filterBirthDateFrom, filterBirthDateTo, filterStudentName);
    }

    @Override
    public List<StudentDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy,
                                 Map<String, FilterMeta> filterBy) {

        List<Order> orders = new ArrayList<>();
        if (sortBy != null && !sortBy.isEmpty()) {
            for (SortMeta meta : sortBy.values()) {
                String field = meta.getField();
                Sort.Direction direction = meta.getOrder().isAscending()
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
                orders.add(new Order(direction, field));
            }
        }

        Sort sort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        int page = first / pageSize;

        Page<StudentDTO> result = service.findLazyWithFilters(
                page, pageSize, sort,
                searchText, filterStatus, filterGender,
                filterBirthDateFrom, filterBirthDateTo, filterStudentName);

        setRowCount((int) result.getTotalElements());
        return result.getContent();
    }
}