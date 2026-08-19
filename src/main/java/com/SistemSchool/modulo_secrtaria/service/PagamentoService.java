package com.SistemSchool.modulo_secrtaria.service;

import com.SistemSchool.modulo_secrtaria.dto.PagamentoDTO;
import com.SistemSchool.modulo_secrtaria.interfaces.PagamentoTableProjection;
import com.SistemSchool.modulo_secrtaria.io.EstadoPagamento;
import com.SistemSchool.modulo_secrtaria.io.FormaPagamento;
import com.SistemSchool.modulo_secrtaria.io.MesReferencia;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.Pagamento;
import com.SistemSchool.modulo_secrtaria.repository.EnrolmentRepository;
import com.SistemSchool.modulo_secrtaria.repository.PagamentoRepository;

import com.SistemSchool.modulo_Financeiro.io.CashBoxStatus;
import com.SistemSchool.modulo_Financeiro.io.MovementStatus;
import com.SistemSchool.modulo_Financeiro.io.MovementType;
import com.SistemSchool.modulo_Financeiro.model.CashBox;
import com.SistemSchool.modulo_Financeiro.model.Fee;
import com.SistemSchool.modulo_Financeiro.model.FinancialMovement;
import com.SistemSchool.modulo_Financeiro.repository.CashBoxRepository;
import com.SistemSchool.modulo_Financeiro.repository.FeeRepository;
import com.SistemSchool.modulo_Financeiro.service.CashBoxService;
import com.SistemSchool.modulo_Financeiro.service.FinancialMovementService;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
public class PagamentoService {

    private static final Logger LOGGER = Logger.getLogger(PagamentoService.class.getName());

    private static final int DIA_LIMITE_PAGAMENTO = 10;
    private static final BigDecimal VALOR_MULTA_ATRASO = new BigDecimal("1000");

    private final PagamentoRepository repository;
    private final EnrolmentRepository enrolmentRepository;
    private final FeeRepository feeRepository;
    private final CashBoxRepository cashBoxRepository;
    private final CashBoxService cashBoxService;
    private final FinancialMovementService financialMovementService;
    private final PdfGeneratorService pdfGeneratorService;

    public PagamentoService(PagamentoRepository repository,
            EnrolmentRepository enrolmentRepository,
            FeeRepository feeRepository,
            CashBoxRepository cashBoxRepository,
            CashBoxService cashBoxService,
            FinancialMovementService financialMovementService,
            PdfGeneratorService pdfGeneratorService) {
        this.repository = repository;
        this.enrolmentRepository = enrolmentRepository;
        this.feeRepository = feeRepository;
        this.cashBoxRepository = cashBoxRepository;
        this.cashBoxService = cashBoxService;
        this.financialMovementService = financialMovementService;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Pagamento save(Pagamento pagamento) {
        if (pagamento.getEnrolment() == null || pagamento.getEnrolment().getPhEnrolment() == null) {
            throw new RuntimeException("É necessário indicar a matrícula do aluno.");
        }
        if (pagamento.getFee() == null || pagamento.getFee().getPhFee() == null) {
            throw new RuntimeException("É necessário indicar a propina.");
        }
        if (pagamento.getCashBox() == null || pagamento.getCashBox().getPhCashBox() == null) {
            throw new RuntimeException("É necessário indicar o caixa.");
        }

        Enrolment enrolment = enrolmentRepository.findById(pagamento.getEnrolment().getPhEnrolment())
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada."));
        pagamento.setEnrolment(enrolment);

        Fee fee = feeRepository.findById(pagamento.getFee().getPhFee())
                .orElseThrow(() -> new RuntimeException("Propina não encontrada."));
        pagamento.setFee(fee);

        CashBox cashBox = cashBoxRepository.findById(pagamento.getCashBox().getPhCashBox())
                .orElseThrow(() -> new RuntimeException("Caixa não encontrado."));
        pagamento.setCashBox(cashBox);

        if (pagamento.getEstado() == EstadoPagamento.PAGO && pagamento.getMesReferencia() == null) {
            throw new RuntimeException("É necessário indicar o mês de referência para confirmar o pagamento.");
        }

        if (pagamento.getEstado() == EstadoPagamento.PAGO) {
            validarPagamentoDuplicado(enrolment.getPhEnrolment(), pagamento.getMesReferencia(),
                    pagamento.getPkPagamento());
        }

        LocalDateTime dataPagamento = pagamento.getDataPagamento() != null
                ? pagamento.getDataPagamento()
                : LocalDateTime.now();
        pagamento.setDataPagamento(dataPagamento);
        BigDecimal multa = calcularMulta(pagamento.getMesReferencia(), dataPagamento);
        pagamento.setMulta(multa);
        pagamento.setTotal(somarSeguro(pagamento.getValor(), multa));

        if (pagamento.getNumeroDocumento() == null || pagamento.getNumeroDocumento().isBlank()) {
            pagamento.setNumeroDocumento(generateNumeroDocumento());
        } else if (repository.existsByNumeroDocumento(pagamento.getNumeroDocumento())) {
            throw new RuntimeException("Número de documento já existe: " + pagamento.getNumeroDocumento());
        }

        Pagamento salvo = repository.save(pagamento);

        if (salvo.getEstado() == EstadoPagamento.PAGO) {
            aplicarEntradaNoCaixa(salvo, resolveOperatorName());
        }
        return salvo;
    }

    public void update(PagamentoDTO dto) {
        Pagamento pagamento = repository.findById(dto.getPkPagamento())
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com id: " + dto.getPkPagamento()));

        EstadoPagamento estadoAnterior = pagamento.getEstado();
        BigDecimal totalAnterior = pagamento.getTotal();
        CashBox cashBoxAnterior = pagamento.getCashBox();

        if (dto.getEnrolmentPk() != null && !dto.getEnrolmentPk().equals(pagamento.getEnrolment().getPhEnrolment())) {
            Enrolment enrolment = enrolmentRepository.findById(dto.getEnrolmentPk())
                    .orElseThrow(() -> new RuntimeException("Matrícula não encontrada."));
            pagamento.setEnrolment(enrolment);
        }
        if (dto.getFeePk() != null && !dto.getFeePk().equals(pagamento.getFee().getPhFee())) {
            Fee fee = feeRepository.findById(dto.getFeePk())
                    .orElseThrow(() -> new RuntimeException("Propina não encontrada."));
            pagamento.setFee(fee);
        }
        if (dto.getCashBoxPk() != null && !dto.getCashBoxPk().equals(pagamento.getCashBox().getPhCashBox())) {
            CashBox cashBox = cashBoxRepository.findById(dto.getCashBoxPk())
                    .orElseThrow(() -> new RuntimeException("Caixa não encontrado."));
            pagamento.setCashBox(cashBox);
        }

        if (dto.getEstado() == EstadoPagamento.PAGO && dto.getMesReferencia() == null) {
            throw new RuntimeException("É necessário indicar o mês de referência para confirmar o pagamento.");
        }

        if (dto.getEstado() == EstadoPagamento.PAGO) {
            validarPagamentoDuplicado(pagamento.getEnrolment().getPhEnrolment(), dto.getMesReferencia(),
                    pagamento.getPkPagamento());
        }

        pagamento.setValor(dto.getValor());
        pagamento.setDataPagamento(dto.getDataPagamento());
        pagamento.setFormaPagamento(dto.getFormaPagamento());
        pagamento.setEstado(dto.getEstado());
        pagamento.setMesReferencia(dto.getMesReferencia());
        pagamento.setReferencia(dto.getReferencia());
        pagamento.setObservacao(dto.getObservacao());

        LocalDateTime dataPagamento = pagamento.getDataPagamento() != null
                ? pagamento.getDataPagamento()
                : LocalDateTime.now();
        pagamento.setDataPagamento(dataPagamento);
        BigDecimal multa = calcularMulta(pagamento.getMesReferencia(), dataPagamento);
        pagamento.setMulta(multa);
        pagamento.setTotal(somarSeguro(pagamento.getValor(), multa));

        repository.save(pagamento);
        reconciliarSaldoCaixa(estadoAnterior, totalAnterior, cashBoxAnterior, pagamento);
    }

    public void delete(Long id) {
        Pagamento pagamento = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com id: " + id));
        repository.deleteById(id);

        if (pagamento.getEstado() == EstadoPagamento.PAGO && pagamento.getCashBox() != null
                && pagamento.getTotal() != null) {
            try {
                cashBoxService.debitarValor(pagamento.getCashBox().getPhCashBox(), pagamento.getTotal());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao estornar saldo do caixa após eliminação do pagamento " + id, e);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDAÇÃO DE PAGAMENTO DUPLICADO
    // ─────────────────────────────────────────────────────────────

    private void validarPagamentoDuplicado(Long enrolmentPk, MesReferencia mesReferencia, Long excludeId) {
        if (mesReferencia == null)
            return;
        boolean existePorMes = repository.existsPagamentoPagoByEnrolmentAndMes(enrolmentPk, mesReferencia, excludeId);
        if (existePorMes) {
            throw new RuntimeException(
                    "Já existe um pagamento confirmado para " + mesReferencia + " referente a esta matrícula.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CÁLCULO AUTOMÁTICO DE MULTA POR ATRASO
    // ─────────────────────────────────────────────────────────────

    public BigDecimal calcularMultaPreview(MesReferencia mesReferencia, LocalDateTime dataPagamento) {
        return calcularMulta(mesReferencia, dataPagamento);
    }

    private BigDecimal calcularMulta(MesReferencia mesReferencia, LocalDateTime dataPagamento) {
        if (mesReferencia == null || dataPagamento == null || !mesReferencia.isMensalidade()) {
            return BigDecimal.ZERO;
        }
        LocalDateTime dataLimite = calcularDataLimite(mesReferencia, dataPagamento);
        return dataPagamento.isAfter(dataLimite) ? VALOR_MULTA_ATRASO : BigDecimal.ZERO;
    }

    private LocalDateTime calcularDataLimite(MesReferencia mesReferencia, LocalDateTime dataPagamento) {
        int mesNum = mesNumero(mesReferencia);
        int anoReferencia = dataPagamento.getYear();
        if (mesNum == 12 && dataPagamento.getMonthValue() <= 2) {
            anoReferencia -= 1;
        }
        int mesLimite = mesNum == 12 ? 1 : mesNum + 1;
        int anoLimite = mesNum == 12 ? anoReferencia + 1 : anoReferencia;
        return LocalDateTime.of(anoLimite, mesLimite, DIA_LIMITE_PAGAMENTO, 23, 59, 59);
    }

    private int mesNumero(MesReferencia mes) {
        switch (mes.name()) {
            case "JANEIRO":
                return 1;
            case "FEVEREIRO":
                return 2;
            case "MARCO":
            case "MARÇO":
                return 3;
            case "ABRIL":
                return 4;
            case "MAIO":
                return 5;
            case "JUNHO":
                return 6;
            case "JULHO":
                return 7;
            case "AGOSTO":
                return 8;
            case "SETEMBRO":
                return 9;
            case "OUTUBRO":
                return 10;
            case "NOVEMBRO":
                return 11;
            case "DEZEMBRO":
                return 12;
            default:
                return mes.ordinal() + 1;
        }
    }

    private BigDecimal somarSeguro(BigDecimal valor, BigDecimal multa) {
        BigDecimal v = valor != null ? valor : BigDecimal.ZERO;
        BigDecimal m = multa != null ? multa : BigDecimal.ZERO;
        return v.add(m);
    }

    // ─────────────────────────────────────────────────────────────
    // MOVIMENTAÇÃO DE CAIXA
    // ─────────────────────────────────────────────────────────────

    private void aplicarEntradaNoCaixa(Pagamento pagamento, String operador) {
        try {
            cashBoxService.creditarValor(pagamento.getCashBox().getPhCashBox(), pagamento.getTotal());
            registrarMovimentoFinanceiro(pagamento, operador);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Erro ao atualizar o saldo do caixa para o pagamento " + pagamento.getPkPagamento(), e);
            throw new RuntimeException("Erro ao atualizar o saldo do caixa: " + e.getMessage(), e);
        }
    }

    private void registrarMovimentoFinanceiro(Pagamento pagamento, String operador) {
        FinancialMovement movement = new FinancialMovement();
        movement.setMovementNumber(generateMovementNumber());
        movement.setCashBox(pagamento.getCashBox());
        movement.setDescription("Pagamento " + pagamento.getNumeroDocumento() + " - matrícula "
                + pagamento.getEnrolment().getEnrolmentNumer());
        movement.setAmount(pagamento.getTotal());
        movement.setType(MovementType.INCOME);
        movement.setStatus(MovementStatus.ACTIVE);
        movement.setCategory("PROPINA");
        movement.setResponsible(operador);
        movement.setMovementDate(LocalDateTime.now());
        financialMovementService.save(movement);
    }

    private void reconciliarSaldoCaixa(EstadoPagamento estadoAnterior, BigDecimal totalAnterior,
            CashBox cashBoxAnterior, Pagamento pagamentoAtual) {
        boolean eraPago = estadoAnterior == EstadoPagamento.PAGO;
        boolean ePago = pagamentoAtual.getEstado() == EstadoPagamento.PAGO;

        try {
            if (eraPago && !ePago) {
                if (cashBoxAnterior != null && totalAnterior != null) {
                    cashBoxService.debitarValor(cashBoxAnterior.getPhCashBox(), totalAnterior);
                }
                return;
            }
            if (!eraPago && ePago) {
                aplicarEntradaNoCaixa(pagamentoAtual, resolveOperatorName());
                return;
            }
            if (eraPago && ePago) {
                Long caixaAntigoId = cashBoxAnterior != null ? cashBoxAnterior.getPhCashBox() : null;
                Long caixaNovoId = pagamentoAtual.getCashBox() != null ? pagamentoAtual.getCashBox().getPhCashBox()
                        : null;

                if (caixaAntigoId != null && !caixaAntigoId.equals(caixaNovoId)) {
                    if (totalAnterior != null)
                        cashBoxService.debitarValor(caixaAntigoId, totalAnterior);
                    if (caixaNovoId != null && pagamentoAtual.getTotal() != null)
                        cashBoxService.creditarValor(caixaNovoId, pagamentoAtual.getTotal());
                    return;
                }
                if (caixaNovoId != null) {
                    BigDecimal anterior = totalAnterior != null ? totalAnterior : BigDecimal.ZERO;
                    BigDecimal atual = pagamentoAtual.getTotal() != null ? pagamentoAtual.getTotal() : BigDecimal.ZERO;
                    BigDecimal diferenca = atual.subtract(anterior);
                    if (diferenca.compareTo(BigDecimal.ZERO) > 0) {
                        cashBoxService.creditarValor(caixaNovoId, diferenca);
                    } else if (diferenca.compareTo(BigDecimal.ZERO) < 0) {
                        cashBoxService.debitarValor(caixaNovoId, diferenca.abs());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao reconciliar saldo do caixa após atualização de pagamento", e);
            throw new RuntimeException("Erro ao atualizar o saldo do caixa: " + e.getMessage(), e);
        }
    }

    private String resolveOperatorName() {
        return "Sistema";
    }

    // ─────────────────────────────────────────────────────────────
    // CONFIRMAÇÃO DE PAGAMENTO
    // ─────────────────────────────────────────────────────────────

    public Pagamento confirmarPagamento(Long enrolmentPk, Long feePk, BigDecimal valor,
            FormaPagamento formaPagamento, MesReferencia mesReferencia,
            String referencia, String observacao, String operador) {
        if (enrolmentPk == null)
            throw new RuntimeException("É necessário indicar a matrícula.");
        if (feePk == null)
            throw new RuntimeException("É necessário indicar a propina.");
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("O valor pago deve ser maior que zero.");
        if (formaPagamento == null)
            throw new RuntimeException("É necessário indicar a forma de pagamento.");
        if (mesReferencia == null)
            throw new RuntimeException("É necessário indicar o mês de referência.");

        Enrolment enrolment = enrolmentRepository.findById(enrolmentPk)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada com id: " + enrolmentPk));
        Fee fee = feeRepository.findById(feePk)
                .orElseThrow(() -> new RuntimeException("Propina não encontrada com id: " + feePk));

        validarPagamentoDuplicado(enrolmentPk, mesReferencia, null);

        CashBox cashBox = cashBoxRepository.findFirstByStatusOrderByOpeningDateDesc(CashBoxStatus.OPEN);
        if (cashBox == null) {
            throw new RuntimeException("Não existe nenhum caixa aberto. Abra um caixa antes de registar o pagamento.");
        }

        LocalDateTime dataPagamento = LocalDateTime.now();
        BigDecimal multa = calcularMulta(mesReferencia, dataPagamento);
        BigDecimal total = somarSeguro(valor, multa);

        Pagamento pagamento = new Pagamento();
        pagamento.setEnrolment(enrolment);
        pagamento.setFee(fee);
        pagamento.setCashBox(cashBox);
        pagamento.setValor(valor);
        pagamento.setMulta(multa);
        pagamento.setTotal(total);
        pagamento.setFormaPagamento(formaPagamento);
        pagamento.setMesReferencia(mesReferencia);
        pagamento.setReferencia(referencia);
        pagamento.setObservacao(observacao);
        pagamento.setDataPagamento(dataPagamento);
        pagamento.setEstado(EstadoPagamento.PAGO);
        pagamento.setNumeroDocumento(generateNumeroDocumento());

        pagamento = repository.save(pagamento);
        repository.flush();
        aplicarEntradaNoCaixa(pagamento, operador);
        return pagamento;
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS / LAZY / FILTROS
    // ─────────────────────────────────────────────────────────────

    public List<PagamentoDTO> getAllPagamentos() {
        return repository.findAllPagamentosDTO();
    }

    public Page<PagamentoDTO> findLazy(int page, int size, Sort sort,
            String numeroDocumento, String studentName,
            FormaPagamento formaPagamento, EstadoPagamento estado,
            LocalDateTime dataInicio, LocalDateTime dataFim) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Pagamento> result = repository.findComFiltros(
                blankToNull(numeroDocumento), blankToNull(studentName),
                formaPagamento, estado, dataInicio, dataFim, pageable);
        return result.map(this::toDto);
    }

    public List<PagamentoDTO> buscarComFiltros(String numeroDocumento, String studentName,
            FormaPagamento formaPagamento, EstadoPagamento estado,
            LocalDateTime dataInicio, LocalDateTime dataFim) {
        try {
            return repository.findComFiltros(
                    blankToNull(numeroDocumento), blankToNull(studentName),
                    formaPagamento, estado, dataInicio, dataFim, Pageable.unpaged())
                    .getContent()
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar pagamentos com filtros", e);
            throw new RuntimeException("Erro ao buscar pagamentos: " + e.getMessage(), e);
        }
    }

    private PagamentoDTO toDto(Pagamento p) {
        PagamentoDTO dto = new PagamentoDTO();
        dto.setPkPagamento(p.getPkPagamento());
        dto.setNumeroDocumento(p.getNumeroDocumento());
        if (p.getEnrolment() != null) {
            dto.setEnrolmentPk(p.getEnrolment().getPhEnrolment());
            dto.setEnrolmentNumero(p.getEnrolment().getEnrolmentNumer());
            if (p.getEnrolment().getStudent() != null) {
                dto.setStudentFullName(p.getEnrolment().getStudent().getFullName());
            }
        }
        if (p.getFee() != null) {
            dto.setFeePk(p.getFee().getPhFee());
            dto.setFeeDescricao(p.getFee().getDescription());
        }
        if (p.getCashBox() != null) {
            dto.setCashBoxPk(p.getCashBox().getPhCashBox());
            dto.setCashBoxNumber(p.getCashBox().getCashBoxNumber());
        }
        dto.setValor(p.getValor());
        dto.setMulta(p.getMulta());
        dto.setTotal(p.getTotal());
        dto.setDataEmissao(p.getDataEmissao());
        dto.setDataPagamento(p.getDataPagamento());
        dto.setFormaPagamento(p.getFormaPagamento());
        dto.setEstado(p.getEstado());
        dto.setMesReferencia(p.getMesReferencia());
        dto.setReferencia(p.getReferencia());
        dto.setObservacao(p.getObservacao());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    // ─────────────────────────────────────────────────────────────
    // RELATÓRIOS / PDF
    // ─────────────────────────────────────────────────────────────

    public byte[] gerarListaPagamentosPdf(List<PagamentoDTO> pagamentos, String titulo) {
        try {
            return pdfGeneratorService.generatePagamentosListPdf(pagamentos, titulo);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao gerar PDF da lista de pagamentos", e);
            throw new RuntimeException("Erro ao gerar PDF da lista: " + e.getMessage(), e);
        }
    }

    public byte[] gerarComprovativoPdf(Long id) {
        Pagamento pagamento = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com id: " + id));
        if (pagamento.getEnrolment() != null && pagamento.getEnrolment().getStudent() != null) {
            pagamento.getEnrolment().getStudent().getFullName();
        }
        if (pagamento.getFee() != null)
            pagamento.getFee().getPhFee();
        if (pagamento.getCashBox() != null)
            pagamento.getCashBox().getCashBoxNumber();
        return pdfGeneratorService.generatePagamentoPdf(pagamento);
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    public List<Pagamento> getByEnrolment(Long enrolmentPk) {
        return repository.findByEnrolment_PhEnrolment(enrolmentPk);
    }

    public List<Pagamento> getByStudent(Long studentPk) {
        return repository.findByEnrolment_Student_PkStudent(studentPk);
    }

    public List<Pagamento> getByFee(Long feePk) {
        return repository.findByFee_PhFee(feePk);
    }

    public List<Pagamento> getByCashBox(Long cashBoxPk) {
        return repository.findByCashBox_PhCashBox(cashBoxPk);
    }

    public List<Pagamento> getByEstado(EstadoPagamento estado) {
        return repository.findByEstado(estado);
    }

    public List<Pagamento> getByFormaPagamento(FormaPagamento formaPagamento) {
        return repository.findByFormaPagamento(formaPagamento);
    }

    public List<Pagamento> getByDataPagamentoBetween(LocalDateTime start, LocalDateTime end) {
        return repository.findByDataPagamentoBetween(start, end);
    }

    public boolean existsByNumeroDocumento(String numeroDocumento) {
        return repository.existsByNumeroDocumento(numeroDocumento);
    }

    public BigDecimal getTotalConfirmado() {
        BigDecimal total = repository.getTotalConfirmado();
        return total != null ? total : BigDecimal.ZERO;
    }

    public Pagamento getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com id: " + id));
    }

    public Pagamento findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado com id: " + id));
    }

    // ─────────────────────────────────────────────────────────────
    // GERAÇÃO DE NÚMEROS SEQUENCIAIS
    // ─────────────────────────────────────────────────────────────

    private String generateNumeroDocumento() {
        int year = Year.now().getValue();
        long count = repository.count() + 1;
        return String.format("PAG-%d-%05d", year, count);
    }

    private String generateMovementNumber() {
        int year = Year.now().getValue();
        long count = financialMovementService.count() + 1;
        return String.format("MOV-%d-%05d", year, count);
    }
}