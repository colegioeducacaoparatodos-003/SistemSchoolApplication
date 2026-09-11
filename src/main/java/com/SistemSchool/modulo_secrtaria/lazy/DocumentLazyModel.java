package com.SistemSchool.modulo_secrtaria.lazy;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import com.SistemSchool.modulo_secrtaria.dto.DocumentTableDTO;
import com.SistemSchool.modulo_secrtaria.service.DocumentService;

public class DocumentLazyModel extends LazyDataModel<DocumentTableDTO> {

    private final DocumentService service;

    public DocumentLazyModel(DocumentService service) {
        this.service = service;
    }

    @Override
    public List<DocumentTableDTO> load(
            int first,
            int pageSize,
            Map<String, SortMeta> sortBy,
            Map<String, FilterMeta> filterBy) {

        int page = first / pageSize;

        Sort sort = Sort.unsorted();

        if (!sortBy.isEmpty()) {

            SortMeta meta = sortBy.values().iterator().next();

            sort = Sort.by(
                    meta.getOrder().isAscending()
                            ? Sort.Direction.ASC
                            : Sort.Direction.DESC,
                    meta.getField());
        }

        Page<DocumentTableDTO> result = service.findLazy(page, pageSize, sort);

        setRowCount((int) result.getTotalElements());

        return result.getContent();
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {

        Page<DocumentTableDTO> page = service.findLazy(0, 1, Sort.unsorted());

        return (int) page.getTotalElements();
    }

    @Override
    public String getRowKey(DocumentTableDTO dto) {
        return String.valueOf(dto.getPkDocument());
    }
    // Aqui

    @Override
    public DocumentTableDTO getRowData(String rowKey) {

        int id = Integer.parseInt(rowKey);

        List<DocumentTableDTO> list = (List<DocumentTableDTO>) getWrappedData();

        if (list != null) {
            for (DocumentTableDTO dto : list) {
                if (dto.getPkDocument() == id) {
                    return dto;
                }
            }
        }

        return null;
    }
}
