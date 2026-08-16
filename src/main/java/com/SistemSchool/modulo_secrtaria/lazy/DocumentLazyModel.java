package com.SistemSchool.modulo_secrtaria.lazy;

import com.SistemSchool.modulo_secrtaria.controller.DocumentController;
import com.SistemSchool.modulo_secrtaria.dto.DocumentDTO;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

/**
 * Lê o critério de filtro ativo diretamente do {@link DocumentController}
 * (toolbar de filtros da view) a cada chamada de load()/count(), em vez de
 * depender dos filtros nativos de coluna do p:dataTable.
 */
public class DocumentLazyModel extends LazyDataModel<DocumentDTO> {

    private static final long serialVersionUID = 1L;

    private final DocumentController controller;

    public DocumentLazyModel(DocumentController controller) {
        this.controller = controller;
    }

    @Override
    public List<DocumentDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy,
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

        Page<DocumentDTO> result = controller.getDocumentService()
                .findLazy(page, pageSize, sort, controller.buildFilterCriteria());

        setRowCount((int) result.getTotalElements());

        return result.getContent();
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        Page<DocumentDTO> page = controller.getDocumentService()
                .findLazy(0, 1, Sort.unsorted(), controller.buildFilterCriteria());
        return (int) page.getTotalElements();
    }

    @Override
    public DocumentDTO getRowData(String rowKey) {
        return controller.getDocumentService().getAllDocuments()
                .stream()
                .filter(d -> d.getPhDocument().toString().equals(rowKey))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getRowKey(DocumentDTO documentDTO) {
        return documentDTO.getPhDocument() != null
                ? documentDTO.getPhDocument().toString()
                : null;
    }
}