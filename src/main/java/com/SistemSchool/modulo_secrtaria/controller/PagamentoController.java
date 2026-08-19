package com.SistemSchool.modulo_secrtaria.controller;

import com.SistemSchool.modulo_secrtaria.dto.PagamentoDTO;
import com.SistemSchool.modulo_secrtaria.io.EstadoPagamento;
import com.SistemSchool.modulo_secrtaria.io.FormaPagamento;
import com.SistemSchool.modulo_secrtaria.io.MesReferencia;
import com.SistemSchool.modulo_secrtaria.lazy.PagamentoLazyModel;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.Pagamento;
import com.SistemSchool.modulo_secrtaria.repository.EnrolmentRepository;
import com.SistemSchool.modulo_secrtaria.service.PagamentoService;
import com.SistemSchool.report.PdfReportService;
import com.SistemSchool.modulo_Financeiro.model.CashBox;
import com.SistemSchool.modulo_Financeiro.model.Fee;
import com.SistemSchool.modulo_Financeiro.repository.CashBoxRepository;
import com.SistemSchool.modulo_Financeiro.repository.FeeRepository;

import com.itextpdf.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.IOException;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class PagamentoController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PagamentoController.class.getName());

    private Pagamento pagamento = new Pagamento();
    private PagamentoDTO editDto = new PagamentoDTO();
    private PagamentoDTO selectedPagamento = new PagamentoDTO();
    private Long selectedId;
    private Long selectedEnrolmentId;
    private Long selectedFeeId;
    private Long selectedCashBoxId;

    // Apenas a DATA é escolhida pelo utilizador; a HORA é sempre a hora do sistema.
    private LocalDate dataPagamentoData;
    private LocalDate dataPagamentoDataEdit;

    private BigDecimal valorConfirmar;
    private FormaPagamento formaPagamentoConfirmar;
    private MesReferencia mesReferenciaConfirmar;
    private String referenciaConfirmar;
    private String observacaoConfirmar;

    private List<Enrolment> enrolments = new java.util.ArrayList<>();
    private List<Fee> fees = new java.util.ArrayList<>();
    private List<CashBox> cashBoxes = new java.util.ArrayList<>();

    private long totalPagamentoCount;
    private long confirmadoCount;
    private BigDecimal totalConfirmadoAmount;

    private String filtroNumeroDocumento;
    private String filtroStudentName;
    private FormaPagamento filtroFormaPagamento;
    private EstadoPagamento filtroEstado;
    private LocalDateTime filtroDataInicio;
    private LocalDateTime filtroDataFim;

    @Inject private PagamentoService pagamentoService;
    @Inject private EnrolmentRepository enrolmentRepository;
    @Inject private FeeRepository feeRepository;
    @Inject private CashBoxRepository cashBoxRepository;
    private transient PagamentoLazyModel lazyModel;

    @PostConstruct
    public void init() {
        lazyModel = new PagamentoLazyModel(pagamentoService);
        loadEnrolments();
        loadFees();
        loadCashBoxes();
        computeStatistics();
    }

    public void prepareNewPagamento() {
        this.pagamento = new Pagamento();
        this.selectedEnrolmentId = null;
        this.selectedFeeId = null;
        this.selectedCashBoxId = null;
        this.valorConfirmar = null;
        this.formaPagamentoConfirmar = null;
        this.mesReferenciaConfirmar = null;
        this.referenciaConfirmar = null;
        this.observacaoConfirmar = null;
        this.dataPagamentoData = LocalDate.now();
    }

    private void loadEnrolments() {
        try { enrolments = enrolmentRepository.findAllWithStudent(); }
        catch (Exception e) { addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar matriculas", e.getMessage()); }
    }

    private void loadFees() {
        try { fees = feeRepository.findAll(); }
        catch (Exception e) { addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar propinas", e.getMessage()); }
    }

    private void loadCashBoxes() {
        try { cashBoxes = cashBoxRepository.findAll(); }
        catch (Exception e) { addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar caixas", e.getMessage()); }
    }

    private void computeStatistics() {
        try {
            List<PagamentoDTO> all = pagamentoService.getAllPagamentos();
            totalPagamentoCount = all.size();
            confirmadoCount = all.stream().filter(p -> p.getEstado() == EstadoPagamento.PAGO).count();
            totalConfirmadoAmount = pagamentoService.getTotalConfirmado();
        } catch (Exception e) {
            totalPagamentoCount = 0; confirmadoCount = 0; totalConfirmadoAmount = BigDecimal.ZERO;
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao calcular estatisticas", e.getMessage());
        }
    }

    private BigDecimal calcularTotal(BigDecimal valor, BigDecimal multa) {
        BigDecimal v = valor != null ? valor : BigDecimal.ZERO;
        BigDecimal m = multa != null ? multa : BigDecimal.ZERO;
        return v.add(m);
    }

    /**
     * Combina a data escolhida pelo utilizador com a hora atual do sistema.
     * Garante que ninguém consegue "escolher" a hora manualmente.
     */
    private LocalDateTime resolverDataHoraAtual(LocalDate data) {
        LocalDate base = data != null ? data : LocalDate.now();
        return LocalDateTime.of(base, LocalTime.now());
    }

    public String load() {
        try { init(); } catch (Exception e) { addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar pagamentos", e.getMessage()); }
        return "/management/financeiro/pagamentos.xhtml?faces-redirect=true";
    }

    public PagamentoLazyModel getLazyModel() { return lazyModel; }

    public void applyFilters() {
        if (lazyModel != null) {
            lazyModel.setFiltroNumeroDocumento(blankToNull(filtroNumeroDocumento));
            lazyModel.setFiltroStudentName(blankToNull(filtroStudentName));
            lazyModel.setFiltroFormaPagamento(filtroFormaPagamento);
            lazyModel.setFiltroEstado(filtroEstado);
            lazyModel.setFiltroDataInicio(filtroDataInicio);
            lazyModel.setFiltroDataFim(filtroDataFim);
        }
    }

    public void limparFiltros() {
        filtroNumeroDocumento = null;
        filtroStudentName = null;
        filtroFormaPagamento = null;
        filtroEstado = null;
        filtroDataInicio = null;
        filtroDataFim = null;
        if (lazyModel != null) {
            lazyModel.setFiltroNumeroDocumento(null);
            lazyModel.setFiltroStudentName(null);
            lazyModel.setFiltroFormaPagamento(null);
            lazyModel.setFiltroEstado(null);
            lazyModel.setFiltroDataInicio(null);
            lazyModel.setFiltroDataFim(null);
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public List<PagamentoDTO> getPagamentosFiltrados() {
        return pagamentoService.buscarComFiltros(
                filtroNumeroDocumento, filtroStudentName,
                filtroFormaPagamento, filtroEstado,
                filtroDataInicio, filtroDataFim);
    }

    public String savePagamento() {
        try {
            if (selectedEnrolmentId == null) { addMessage(FacesMessage.SEVERITY_ERROR, "Pagamento", "Selecione uma matricula."); return null; }
            if (selectedFeeId == null) { addMessage(FacesMessage.SEVERITY_ERROR, "Pagamento", "Selecione uma propina."); return null; }
            if (selectedCashBoxId == null) { addMessage(FacesMessage.SEVERITY_ERROR, "Pagamento", "Selecione um caixa."); return null; }

            Enrolment enrolment = enrolments.stream().filter(e -> selectedEnrolmentId.equals(e.getPhEnrolment())).findFirst()
                    .orElseThrow(() -> new RuntimeException("Matricula nao encontrada."));
            pagamento.setEnrolment(enrolment);

            Fee fee = fees.stream().filter(f -> selectedFeeId.equals(f.getPhFee())).findFirst()
                    .orElseThrow(() -> new RuntimeException("Propina nao encontrada."));
            pagamento.setFee(fee);

            CashBox cashBox = cashBoxes.stream().filter(c -> selectedCashBoxId.equals(c.getPhCashBox())).findFirst()
                    .orElseThrow(() -> new RuntimeException("Caixa nao encontrado."));
            pagamento.setCashBox(cashBox);

            // A hora é sempre a hora atual do sistema; o utilizador só escolhe o dia.
            pagamento.setDataPagamento(resolverDataHoraAtual(dataPagamentoData));

            pagamento.setTotal(calcularTotal(pagamento.getValor(), pagamento.getMulta()));
            pagamentoService.save(pagamento);

            pagamento = new Pagamento();
            selectedEnrolmentId = null; selectedFeeId = null; selectedCashBoxId = null;
            dataPagamentoData = LocalDate.now();
            init();
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            addMessage(FacesMessage.SEVERITY_INFO, "Pagamento", "Pagamento registado com sucesso");
            return "/management/financeiro/pagamentos.xhtml?faces-redirect=true";
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gravar pagamento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Pagamento", e.getMessage());
            return null;
        }
    }

    public void openConfirmarPagamentoDialog(Long enrolmentPk, Long feePk) {
        if (enrolmentPk == null || feePk == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Selecione a matricula e a propina!", "");
            return;
        }
        this.selectedEnrolmentId = enrolmentPk;
        this.selectedFeeId = feePk;
        this.valorConfirmar = null;
        this.formaPagamentoConfirmar = null;
        this.mesReferenciaConfirmar = null;
        this.referenciaConfirmar = null;
        this.observacaoConfirmar = null;
    }

    public String confirmarPagamento() {
        try {
            if (selectedEnrolmentId == null || selectedFeeId == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Pagamento", "Selecione a matricula e a propina.");
                return null;
            }
            pagamentoService.confirmarPagamento(
                    selectedEnrolmentId, selectedFeeId, valorConfirmar,
                    formaPagamentoConfirmar, mesReferenciaConfirmar,
                    referenciaConfirmar, observacaoConfirmar, resolveOperatorName());

            selectedEnrolmentId = null; selectedFeeId = null; selectedCashBoxId = null;
            valorConfirmar = null; formaPagamentoConfirmar = null; mesReferenciaConfirmar = null;
            referenciaConfirmar = null; observacaoConfirmar = null;
            init();
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            addMessage(FacesMessage.SEVERITY_INFO, "Pagamento", "Pagamento confirmado com sucesso");
            return "/management/financeiro/pagamentos.xhtml?faces-redirect=true";
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao confirmar pagamento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Pagamento", e.getMessage());
            return null;
        }
    }

    private String resolveOperatorName() { return "Sistema"; }

    public void openEditDialog(Long id) {
        if (id == null) { addMessage(FacesMessage.SEVERITY_ERROR, "Nenhum pagamento selecionado!", ""); return; }
        this.selectedId = id;
        PagamentoDTO dto = pagamentoService.getAllPagamentos().stream().filter(p -> id.equals(p.getPkPagamento())).findFirst().orElse(null);
        if (dto != null) {
            mapDtoFields(dto, editDto = new PagamentoDTO());
            mapDtoFields(dto, selectedPagamento);
            selectedEnrolmentId = dto.getEnrolmentPk();
            selectedFeeId = dto.getFeePk();
            selectedCashBoxId = dto.getCashBoxPk();
            dataPagamentoDataEdit = dto.getDataPagamento() != null ? dto.getDataPagamento().toLocalDate() : LocalDate.now();
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Pagamento nao encontrado", "");
        }
    }

    public void loadSelectedPagamento() {
        if (selectedId == null) return;
        PagamentoDTO dto = pagamentoService.getAllPagamentos().stream().filter(p -> selectedId.equals(p.getPkPagamento())).findFirst().orElse(null);
        if (dto != null) mapDtoFields(dto, selectedPagamento);
        else addMessage(FacesMessage.SEVERITY_WARN, "Pagamento nao encontrado", "");
    }

    private void mapDtoFields(PagamentoDTO source, PagamentoDTO target) {
        target.setPkPagamento(source.getPkPagamento());
        target.setNumeroDocumento(source.getNumeroDocumento());
        target.setEnrolmentPk(source.getEnrolmentPk());
        target.setEnrolmentNumero(source.getEnrolmentNumero());
        target.setStudentFullName(source.getStudentFullName());
        target.setFeePk(source.getFeePk());
        target.setFeeDescricao(source.getFeeDescricao());
        target.setCashBoxPk(source.getCashBoxPk());
        target.setCashBoxNumber(source.getCashBoxNumber());
        target.setValor(source.getValor());
        target.setMulta(source.getMulta());
        target.setTotal(source.getTotal());
        target.setDataEmissao(source.getDataEmissao());
        target.setDataPagamento(source.getDataPagamento());
        target.setFormaPagamento(source.getFormaPagamento());
        target.setEstado(source.getEstado());
        target.setMesReferencia(source.getMesReferencia());
        target.setReferencia(source.getReferencia());
        target.setObservacao(source.getObservacao());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    public void saveUpdate() {
        try {
            if (selectedEnrolmentId != null) editDto.setEnrolmentPk(selectedEnrolmentId);
            if (selectedFeeId != null) editDto.setFeePk(selectedFeeId);
            if (selectedCashBoxId != null) editDto.setCashBoxPk(selectedCashBoxId);
            // A hora é sempre a hora atual do sistema; o utilizador só escolhe o dia.
            editDto.setDataPagamento(resolverDataHoraAtual(dataPagamentoDataEdit));
            editDto.setTotal(calcularTotal(editDto.getValor(), editDto.getMulta()));
            pagamentoService.update(editDto);
            init();
            editDto = new PagamentoDTO(); selectedId = null;
            selectedEnrolmentId = null; selectedFeeId = null; selectedCashBoxId = null;
            dataPagamentoDataEdit = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Pagamento", "Pagamento atualizado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar pagamento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Pagamento", e.getMessage());
        }
    }

    public void delete(Long id) {
        if (id == null) { addMessage(FacesMessage.SEVERITY_WARN, "Nenhum pagamento selecionado!", ""); return; }
        try {
            pagamentoService.delete(id);
            selectedId = null; init();
            addMessage(FacesMessage.SEVERITY_INFO, "Pagamento", "Pagamento eliminado com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao eliminar pagamento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Pagamento", e.getMessage());
        }
    }

    public void onFeeChange() {
        if (selectedFeeId == null) {
            pagamento.setValor(null); pagamento.setMulta(null); pagamento.setTotal(null); return;
        }
        fees.stream().filter(f -> selectedFeeId.equals(f.getPhFee())).findFirst().ifPresent(fee -> {
            pagamento.setValor(fee.getAmount());
            recalcularMultaTotal(pagamento);
        });
    }

    public void onMesReferenciaChange() {
        if (pagamento.getValor() != null) recalcularMultaTotal(pagamento);
    }

    /**
     * Recalcula multa/total sempre com a data escolhida pelo utilizador + hora atual do sistema.
     * A multa só é aplicada se o mês de referência selecionado for efetivamente uma mensalidade.
     */
    public void onDataPagamentoChange() {
        if (pagamento.getValor() != null) recalcularMultaTotal(pagamento);
    }

    private void recalcularMultaTotal(Pagamento p) {
        LocalDateTime dataSimulada = resolverDataHoraAtual(dataPagamentoData);
        BigDecimal multa = pagamentoService.calcularMultaPreview(p.getMesReferencia(), dataSimulada);
        p.setMulta(multa);
        p.setTotal(calcularTotal(p.getValor(), multa));
    }

    public void onFeeChangeEdit() {
        if (selectedFeeId == null) {
            editDto.setValor(null); editDto.setMulta(null); editDto.setTotal(null); return;
        }
        fees.stream().filter(f -> selectedFeeId.equals(f.getPhFee())).findFirst().ifPresent(fee -> {
            editDto.setValor(fee.getAmount());
            recalcularMultaTotalEdit();
        });
    }

    public void onMesReferenciaChangeEdit() {
        if (editDto.getValor() != null) recalcularMultaTotalEdit();
    }

    public void onDataPagamentoChangeEdit() {
        if (editDto.getValor() != null) recalcularMultaTotalEdit();
    }

    private void recalcularMultaTotalEdit() {
        LocalDateTime dataSimulada = resolverDataHoraAtual(dataPagamentoDataEdit);
        BigDecimal multa = pagamentoService.calcularMultaPreview(editDto.getMesReferencia(), dataSimulada);
        editDto.setMulta(multa);
        editDto.setTotal(calcularTotal(editDto.getValor(), multa));
    }

    /**
     * Usado na view para mostrar/ocultar avisos sobre multa (ex: "isento de multa"),
     * já que apenas mensalidades (Janeiro..Dezembro) estão sujeitas a multa por atraso.
     */
    public boolean isMensalidadeSelecionada() {
        return pagamento.getMesReferencia() != null && pagamento.getMesReferencia().isMensalidade();
    }

    public boolean isMensalidadeSelecionadaEdit() {
        return editDto.getMesReferencia() != null && editDto.getMesReferencia().isMensalidade();
    }

    public void exportPagamentoListPdf() {
        try {
            List<PagamentoDTO> pagamentos = pagamentoService.getAllPagamentos();
            if (pagamentos == null || pagamentos.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Nenhum pagamento para exportar", ""); return;
            }
            byte[] pdf = PdfReportService.generatePagamentoListReport(pagamentos);
            String fileName = "lista_pagamentos_" + java.time.LocalDate.now() + ".pdf";
            PdfReportService.streamToResponse(pdf, fileName, true);
        } catch (DocumentException | IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao exportar lista de pagamentos", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao exportar lista", e.getMessage());
        }
    }

    public void baixarPdf(Long id) { streamPdf(id, "attachment"); }
    public void imprimirPdf(Long id) { streamPdf(id, "inline"); }

    private void streamPdf(Long id, String disposition) {
        if (id == null) { addMessage(FacesMessage.SEVERITY_WARN, "Nenhum pagamento selecionado!", ""); return; }
        try {
            byte[] pdfBytes = pagamentoService.gerarComprovativoPdf(id);
            Pagamento p = pagamentoService.getById(id);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            externalContext.responseReset();
            externalContext.setResponseContentType("application/pdf");
            externalContext.setResponseHeader("Content-Disposition", disposition + "; filename=\"pagamento-" + p.getNumeroDocumento() + ".pdf\"");
            externalContext.setResponseContentLength(pdfBytes.length);
            OutputStream responseOutputStream = externalContext.getResponseOutputStream();
            responseOutputStream.write(pdfBytes); responseOutputStream.flush();
            facesContext.responseComplete();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar PDF do pagamento", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Pagamento", "Nao foi possivel gerar o PDF: " + e.getMessage());
        }
    }

    public void baixarListaPdf() { streamListaPdf("attachment"); }
    public void imprimirListaPdf() { streamListaPdf("inline"); }

    private void streamListaPdf(String disposition) {
        try {
            List<PagamentoDTO> lista = getPagamentosFiltrados();
            byte[] pdfBytes = pagamentoService.gerarListaPagamentosPdf(lista, "Relatorio de Pagamentos");
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            externalContext.responseReset();
            externalContext.setResponseContentType("application/pdf");
            externalContext.setResponseHeader("Content-Disposition", disposition + "; filename=\"lista-pagamentos-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".pdf\"");
            externalContext.setResponseContentLength(pdfBytes.length);
            OutputStream out = externalContext.getResponseOutputStream();
            out.write(pdfBytes); out.flush();
            facesContext.responseComplete();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar PDF da lista de pagamentos", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Pagamentos", "Nao foi possivel gerar o PDF: " + e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public Pagamento getPagamento() { return pagamento; }
    public void setPagamento(Pagamento pagamento) { this.pagamento = pagamento; }
    public PagamentoDTO getEditDto() { return editDto; }
    public void setEditDto(PagamentoDTO editDto) { this.editDto = editDto; }
    public PagamentoDTO getSelectedPagamento() { return selectedPagamento; }
    public void setSelectedPagamento(PagamentoDTO selectedPagamento) { this.selectedPagamento = selectedPagamento; }
    public Long getSelectedId() { return selectedId; }
    public void setSelectedId(Long selectedId) { this.selectedId = selectedId; }
    public Long getSelectedEnrolmentId() { return selectedEnrolmentId; }
    public void setSelectedEnrolmentId(Long selectedEnrolmentId) { this.selectedEnrolmentId = selectedEnrolmentId; }
    public Long getSelectedFeeId() { return selectedFeeId; }
    public void setSelectedFeeId(Long selectedFeeId) { this.selectedFeeId = selectedFeeId; }
    public Long getSelectedCashBoxId() { return selectedCashBoxId; }
    public void setSelectedCashBoxId(Long selectedCashBoxId) { this.selectedCashBoxId = selectedCashBoxId; }
    public BigDecimal getValorConfirmar() { return valorConfirmar; }
    public void setValorConfirmar(BigDecimal valorConfirmar) { this.valorConfirmar = valorConfirmar; }
    public FormaPagamento getFormaPagamentoConfirmar() { return formaPagamentoConfirmar; }
    public void setFormaPagamentoConfirmar(FormaPagamento formaPagamentoConfirmar) { this.formaPagamentoConfirmar = formaPagamentoConfirmar; }
    public MesReferencia getMesReferenciaConfirmar() { return mesReferenciaConfirmar; }
    public void setMesReferenciaConfirmar(MesReferencia mesReferenciaConfirmar) { this.mesReferenciaConfirmar = mesReferenciaConfirmar; }
    public String getReferenciaConfirmar() { return referenciaConfirmar; }
    public void setReferenciaConfirmar(String referenciaConfirmar) { this.referenciaConfirmar = referenciaConfirmar; }
    public String getObservacaoConfirmar() { return observacaoConfirmar; }
    public void setObservacaoConfirmar(String observacaoConfirmar) { this.observacaoConfirmar = observacaoConfirmar; }
    public void setLazyModel(PagamentoLazyModel lazyModel) { this.lazyModel = lazyModel; }

    public LocalDate getDataPagamentoData() { return dataPagamentoData; }
    public void setDataPagamentoData(LocalDate dataPagamentoData) { this.dataPagamentoData = dataPagamentoData; }
    public LocalDate getDataPagamentoDataEdit() { return dataPagamentoDataEdit; }
    public void setDataPagamentoDataEdit(LocalDate dataPagamentoDataEdit) { this.dataPagamentoDataEdit = dataPagamentoDataEdit; }

    public long getTotalPagamentoCount() { return totalPagamentoCount; }
    public long getConfirmadoCount() { return confirmadoCount; }
    public BigDecimal getTotalConfirmadoAmount() { return totalConfirmadoAmount; }

    public FormaPagamento[] getFormasPagamento() { return FormaPagamento.values(); }
    public EstadoPagamento[] getEstados() { return EstadoPagamento.values(); }
    public MesReferencia[] getMesesReferencia() { return MesReferencia.values(); }
    public List<Enrolment> getEnrolments() { return enrolments; }
    public List<Fee> getFees() { return fees; }
    public List<CashBox> getCashBoxes() { return cashBoxes; }
    public void refreshEnrolments() { loadEnrolments(); }
    public void refreshFees() { loadFees(); }
    public void refreshCashBoxes() { loadCashBoxes(); }
    public List<PagamentoDTO> getPagamentos() { return pagamentoService.getAllPagamentos(); }

    public String getFiltroNumeroDocumento() { return filtroNumeroDocumento; }
    public void setFiltroNumeroDocumento(String filtroNumeroDocumento) { this.filtroNumeroDocumento = filtroNumeroDocumento; }
    public String getFiltroStudentName() { return filtroStudentName; }
    public void setFiltroStudentName(String filtroStudentName) { this.filtroStudentName = filtroStudentName; }
    public FormaPagamento getFiltroFormaPagamento() { return filtroFormaPagamento; }
    public void setFiltroFormaPagamento(FormaPagamento filtroFormaPagamento) { this.filtroFormaPagamento = filtroFormaPagamento; }
    public EstadoPagamento getFiltroEstado() { return filtroEstado; }
    public void setFiltroEstado(EstadoPagamento filtroEstado) { this.filtroEstado = filtroEstado; }
    public LocalDateTime getFiltroDataInicio() { return filtroDataInicio; }
    public void setFiltroDataInicio(LocalDateTime filtroDataInicio) { this.filtroDataInicio = filtroDataInicio; }
    public LocalDateTime getFiltroDataFim() { return filtroDataFim; }
    public void setFiltroDataFim(LocalDateTime filtroDataFim) { this.filtroDataFim = filtroDataFim; }
}