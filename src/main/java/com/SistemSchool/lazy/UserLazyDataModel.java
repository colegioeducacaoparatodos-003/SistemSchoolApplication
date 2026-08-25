package com.SistemSchool.lazy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import com.SistemSchool.dto.UserDTO;
import com.SistemSchool.io.Perfil;
import com.SistemSchool.service.UserService;

public class UserLazyDataModel extends LazyDataModel<UserDTO.UserResponseDTO> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UserService userService;

    // Filtros mantidos no estado do modelo
    private String filterEmail;
    private Perfil filterPerfil;
    private Boolean filterActive;

    public UserLazyDataModel(UserService userService) {
        this.userService = userService;
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        return (int) userService.countAllUsers();
    }

    @Override
    public List<UserDTO.UserResponseDTO> load(int first, int pageSize,
            Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {

        String sortField = "pkUser";
        org.primefaces.model.SortOrder sortOrder = org.primefaces.model.SortOrder.ASCENDING;

        if (sortBy != null && !sortBy.isEmpty()) {
            SortMeta meta = sortBy.values().iterator().next();
            sortField = meta.getField();
            sortOrder = meta.getOrder();
        }

        var page = userService.findUsersPage(
                first, pageSize, sortField, sortOrder,
                filterEmail, filterPerfil, filterActive);

        // CORREÇÃO: setRowCount é do LazyDataModel, não do serviço
        setRowCount((int) page.getTotalElements());

        return page.getContent();
    }

    public String getFilterEmail() {
        return filterEmail;
    }

    public void setFilterEmail(String filterEmail) {
        this.filterEmail = filterEmail;
    }

    public Perfil getFilterPerfil() {
        return filterPerfil;
    }

    public void setFilterPerfil(Perfil filterPerfil) {
        this.filterPerfil = filterPerfil;
    }

    public Boolean getFilterActive() {
        return filterActive;
    }

    public void setFilterActive(Boolean filterActive) {
        this.filterActive = filterActive;
    }
}