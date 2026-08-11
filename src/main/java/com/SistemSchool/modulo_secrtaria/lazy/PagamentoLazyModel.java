package com.SistemSchool.modulo_secrtaria.lazy;

import com.SistemSchool.modulo_secrtaria.dto.PagamentoDTO;
import com.SistemSchool.modulo_secrtaria.io.EstadoPagamento;
import com.SistemSchool.modulo_secrtaria.io.FormaPagamento;
import com.SistemSchool.modulo_secrtaria.service.PagamentoService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PagamentoLazyModel extends LazyDataModel<PagamentoDTO> {

    private static final long serialVersionUID = 1L;

    private final PagamentoService pagamentoService;

    // Filtros externos (do controller)
    private String filtroNumeroDocumento;
    private String filtroStudentName;
    private FormaPagamento filtroFormaPagamento;
    private EstadoPagamento filtroEstado;
    private LocalDateTime filtroDataInicio;
    private LocalDateTime filtroDataFim;

    public PagamentoLazyModel(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @Override
    public List<PagamentoDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy,
            Map<String, FilterMeta> filterBy) {
        int page = first / pageSize;

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sortBy != null && !sortBy.isEmpty()) {
            SortMeta sortMeta = sortBy.values().iterator().next();
            Sort.Direction direction = sortMeta.getOrder().isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, sortMeta.getField());
        }

        var result = pagamentoService.findLazy(
                page, pageSize, sort,
                filtroNumeroDocumento, filtroStudentName,
                filtroFormaPagamento, filtroEstado,
                filtroDataInicio, filtroDataFim);

        setRowCount((int) result.getTotalElements());
        return result.getContent();
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        var result = pagamentoService.findLazy(
                0, 1, Sort.unsorted(),
                filtroNumeroDocumento, filtroStudentName,
                filtroFormaPagamento, filtroEstado,
                filtroDataInicio, filtroDataFim);
        return (int) result.getTotalElements();
    }

    @Override
    public PagamentoDTO getRowData(String rowKey) {
        return pagamentoService.getAllPagamentos()
                .stream()
                .filter(p -> p.getPkPagamento().toString().equals(rowKey))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getRowKey(PagamentoDTO pagamentoDTO) {
        return pagamentoDTO.getPkPagamento() != null ? pagamentoDTO.getPkPagamento().toString() : null;
    }

    // ── Getters & Setters ──
    public String getFiltroNumeroDocumento() { return filtroNumeroDocumento; }
    public void setFiltroNumeroDocumento(String v) { this.filtroNumeroDocumento = v; }
    public String getFiltroStudentName() { return filtroStudentName; }
    public void setFiltroStudentName(String v) { this.filtroStudentName = v; }
    public FormaPagamento getFiltroFormaPagamento() { return filtroFormaPagamento; }
    public void setFiltroFormaPagamento(FormaPagamento v) { this.filtroFormaPagamento = v; }
    public EstadoPagamento getFiltroEstado() { return filtroEstado; }
    public void setFiltroEstado(EstadoPagamento v) { this.filtroEstado = v; }
    public LocalDateTime getFiltroDataInicio() { return filtroDataInicio; }
    public void setFiltroDataInicio(LocalDateTime v) { this.filtroDataInicio = v; }
    public LocalDateTime getFiltroDataFim() { return filtroDataFim; }
    public void setFiltroDataFim(LocalDateTime v) { this.filtroDataFim = v; }
}