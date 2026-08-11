package com.SistemSchool.modulo_dashboard_charts.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;

import com.SistemSchool.modulo_Financeiro.io.CashBoxStatus;
import com.SistemSchool.modulo_Financeiro.io.MovementType;
import com.SistemSchool.modulo_Financeiro.repository.CashBoxRepository;
import com.SistemSchool.modulo_Financeiro.repository.FinancialMovementRepository;
import com.SistemSchool.modulo_pedagogico.io.DisciplineStatus;
import com.SistemSchool.modulo_pedagogico.repository.DisciplineRepository;
import com.SistemSchool.modulo_Recursoa_Humano.io.TeacherStatus;
import com.SistemSchool.modulo_Recursoa_Humano.repository.TeacherRepository;
import com.SistemSchool.modulo_dashboard_charts.dto.CategoryTotalDTO;
import com.SistemSchool.modulo_dashboard_charts.dto.DashboardFilterDTO;
import com.SistemSchool.modulo_dashboard_charts.dto.DashboardStatsDTO;
import com.SistemSchool.modulo_dashboard_charts.dto.FinancialFilterStatsDTO;
import com.SistemSchool.modulo_dashboard_charts.dto.ProfileCountDTO;
import com.SistemSchool.modulo_dashboard_charts.interfaces.MonthlyFinancialProjection;
import com.SistemSchool.modulo_secrtaria.io.EstadoPagamento;
import com.SistemSchool.modulo_secrtaria.io.SchoolClaassStatus;
import com.SistemSchool.modulo_secrtaria.io.StudentStatus;
import com.SistemSchool.modulo_secrtaria.repository.EnrolmentRepository;
import com.SistemSchool.modulo_secrtaria.repository.PagamentoRepository;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;
import com.SistemSchool.modulo_secrtaria.repository.StudentRepository;
import com.SistemSchool.repository.UserRepository;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final EnrolmentRepository enrolmentRepository;
    private final DisciplineRepository disciplineRepository;
    private final PagamentoRepository pagamentoRepository;
    private final CashBoxRepository cashBoxRepository;
    private final UserRepository userRepository;
    private final FinancialMovementRepository financialMovementRepository;

    public DashboardService(StudentRepository studentRepository,
                             TeacherRepository teacherRepository,
                             SchoolClassRepository schoolClassRepository,
                             EnrolmentRepository enrolmentRepository,
                             DisciplineRepository disciplineRepository,
                             PagamentoRepository pagamentoRepository,
                             CashBoxRepository cashBoxRepository,
                             UserRepository userRepository,
                             FinancialMovementRepository financialMovementRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.disciplineRepository = disciplineRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.cashBoxRepository = cashBoxRepository;
        this.userRepository = userRepository;
        this.financialMovementRepository = financialMovementRepository;
    }

    public DashboardStatsDTO buildStats() {

        long totalStudents = studentRepository.countByStatus(StudentStatus.ACTIVE);
        long totalTeachers = teacherRepository.countByStatus(TeacherStatus.ACTIVE);
        long totalClasses = schoolClassRepository.countByStatus(SchoolClaassStatus.ACTIVE);
        long totalEnrolments = enrolmentRepository.count();
        long totalDisciplines = disciplineRepository.countByStatus(DisciplineStatus.ACTIVE);

        LocalDateTime inicioMes = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime fimMes = YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX);

        BigDecimal receitaMes = pagamentoRepository
                .sumTotalByEstadoAndPeriodo(EstadoPagamento.PAGO, inicioMes, fimMes);

        BigDecimal totalPendente = pagamentoRepository
                .sumTotalByEstado(EstadoPagamento.PENDENTE);

        BigDecimal saldoCaixa = cashBoxRepository.sumSaldoCaixasAbertos();

        return new DashboardStatsDTO(
                totalStudents, totalTeachers, totalClasses, totalEnrolments, totalDisciplines,
                receitaMes != null ? receitaMes : BigDecimal.ZERO,
                totalPendente != null ? totalPendente : BigDecimal.ZERO,
                saldoCaixa != null ? saldoCaixa : BigDecimal.ZERO
        );
    }

    public List<ProfileCountDTO> buildUsersByProfile() {
        return userRepository.countUsersByPerfil();
    }

    /**
     * Estatísticas financeiras filtradas por período, categoria e tipo
     * de movimento. Se o filtro não trouxer período, assume o mês atual.
     */
    public FinancialFilterStatsDTO buildFinancialStats(DashboardFilterDTO filter) {

        LocalDateTime start = filter.hasPeriod()
                ? filter.getStartDate().atStartOfDay()
                : YearMonth.now().atDay(1).atStartOfDay();

        LocalDateTime end = filter.hasPeriod()
                ? filter.getEndDate().atTime(LocalTime.MAX)
                : YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX);

        String category = (filter.getCategory() != null && !filter.getCategory().isBlank())
                ? filter.getCategory()
                : null;

        MovementType type = (filter.getMovementType() != null && !filter.getMovementType().isBlank())
                ? MovementType.valueOf(filter.getMovementType())
                : null;

        BigDecimal totalIncome = financialMovementRepository
                .getTotalIncomeByPeriod(start, end, category);

        BigDecimal totalExpense = financialMovementRepository
                .getTotalExpenseByPeriod(start, end, category);

        List<CategoryTotalDTO> categoryTotals = financialMovementRepository
                .getTotalsByCategory(start, end, type);

        List<MonthlyFinancialProjection> monthlyEvolution = financialMovementRepository
                .getMonthlyEvolution(start.minusMonths(5));

        BigDecimal income = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        BigDecimal expense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

        return new FinancialFilterStatsDTO(
                income,
                expense,
                income.subtract(expense),
                categoryTotals,
                monthlyEvolution
        );
    }

    public List<String> buildAvailableCategories() {
        return financialMovementRepository.findDistinctCategories();
    }
}