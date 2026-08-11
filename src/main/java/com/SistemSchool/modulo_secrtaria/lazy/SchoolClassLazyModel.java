package com.SistemSchool.modulo_secrtaria.lazy;

import com.SistemSchool.modulo_secrtaria.dto.SchoolClassDTO;
import com.SistemSchool.modulo_secrtaria.io.Classe;
import com.SistemSchool.modulo_secrtaria.io.SchoolClaassStatus;
import com.SistemSchool.modulo_secrtaria.io.ShiftType;
import com.SistemSchool.modulo_secrtaria.service.SchoolClassService;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SchoolClassLazyModel extends LazyDataModel<SchoolClassDTO> {

    private final SchoolClassService service;
    private String searchText;
    private Classe filterClasse;
    private ShiftType filterTurno;
    private SchoolClaassStatus filterStatus;
    private String filterAnoLectivo;

    public SchoolClassLazyModel(SchoolClassService service) {
        this.service = service;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public void setFilterClasse(Classe filterClasse) {
        this.filterClasse = filterClasse;
    }

    public void setFilterTurno(ShiftType filterTurno) {
        this.filterTurno = filterTurno;
    }

    public void setFilterStatus(SchoolClaassStatus filterStatus) {
        this.filterStatus = filterStatus;
    }

    public void setFilterAnoLectivo(String filterAnoLectivo) {
        this.filterAnoLectivo = filterAnoLectivo;
    }

    public void clearFilters() {
        this.searchText = null;
        this.filterClasse = null;
        this.filterTurno = null;
        this.filterStatus = null;
        this.filterAnoLectivo = null;
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        // Carrega uma página com tamanho 0 para obter apenas o total de elementos
        List<Order> orders = new ArrayList<>();
        Sort sort = Sort.unsorted();
        Page<SchoolClassDTO> result = service.findLazyWithFilters(
            0, Integer.MAX_VALUE, sort, searchText, filterClasse, filterTurno, filterStatus, filterAnoLectivo);
        return (int) result.getTotalElements();
    }

    @Override
    public List<SchoolClassDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        List<Order> orders = new ArrayList<>();
        if (sortBy != null && !sortBy.isEmpty()) {
            for (SortMeta meta : sortBy.values()) {
                String field = meta.getField();
                Sort.Direction direction = meta.getOrder().isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC;
                orders.add(new Order(direction, field));
            }
        }

        Sort sort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
        int page = first / pageSize;

        Page<SchoolClassDTO> result = service.findLazyWithFilters(
            page, pageSize, sort, searchText, filterClasse, filterTurno, filterStatus, filterAnoLectivo);

        setRowCount((int) result.getTotalElements());
        return result.getContent();
    }
}