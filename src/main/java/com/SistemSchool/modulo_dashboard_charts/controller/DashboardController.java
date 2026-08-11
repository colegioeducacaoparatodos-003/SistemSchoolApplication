package com.SistemSchool.modulo_dashboard_charts.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.SistemSchool.modulo_dashboard_charts.dto.CategoryTotalDTO;
import com.SistemSchool.modulo_dashboard_charts.dto.DashboardFilterDTO;
import com.SistemSchool.modulo_dashboard_charts.dto.DashboardStatsDTO;
import com.SistemSchool.modulo_dashboard_charts.dto.FinancialFilterStatsDTO;
import com.SistemSchool.modulo_dashboard_charts.dto.ProfileCountDTO;
import com.SistemSchool.modulo_dashboard_charts.interfaces.MonthlyFinancialProjection;
import com.SistemSchool.modulo_dashboard_charts.service.DashboardService;

import software.xdev.chartjs.model.color.RGBAColor;
import software.xdev.chartjs.model.charts.BarChart;
import software.xdev.chartjs.model.charts.LineChart;
import software.xdev.chartjs.model.charts.PieChart;
import software.xdev.chartjs.model.data.BarData;
import software.xdev.chartjs.model.data.LineData;
import software.xdev.chartjs.model.data.PieData;
import software.xdev.chartjs.model.dataset.BarDataset;
import software.xdev.chartjs.model.dataset.LineDataset;
import software.xdev.chartjs.model.dataset.PieDataset;
import software.xdev.chartjs.model.options.BarOptions;
import software.xdev.chartjs.model.options.LineOptions;
import software.xdev.chartjs.model.options.PieOptions;
import software.xdev.chartjs.model.options.LegendOptions;
import software.xdev.chartjs.model.options.Plugins;
import software.xdev.chartjs.model.options.Title;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class DashboardController implements Serializable {

    @Inject
    private DashboardService dashboardService;

    private DashboardStatsDTO stats;
    private String usersByProfileChartJson;
    private List<ProfileCountDTO> usersByProfile;

    // ---- Filtros financeiros ----
    private DashboardFilterDTO filter = new DashboardFilterDTO();
    private FinancialFilterStatsDTO financialStats;
    private List<String> availableCategories;
    private List<String> movementTypes = List.of("INCOME", "EXPENSE");
    private String categoryChartJson;
    private String monthlyEvolutionChartJson;

    @PostConstruct
    public void init() {
        this.stats = dashboardService.buildStats();
        this.usersByProfile = dashboardService.buildUsersByProfile();
        this.usersByProfileChartJson = buildUsersByProfileChartJson(usersByProfile);

        this.availableCategories = dashboardService.buildAvailableCategories();

        this.filter.setStartDate(LocalDate.now().withDayOfMonth(1));
        this.filter.setEndDate(LocalDate.now());

        aplicarFiltro();
    }

    public void aplicarFiltro() {
        this.financialStats = dashboardService.buildFinancialStats(filter);
        this.categoryChartJson = buildCategoryChartJson(financialStats.getCategoryTotals());
        this.monthlyEvolutionChartJson = buildMonthlyEvolutionChartJson(financialStats.getMonthlyEvolution());
    }

    public void limparFiltro() {
        this.filter = new DashboardFilterDTO();
        this.filter.setStartDate(LocalDate.now().withDayOfMonth(1));
        this.filter.setEndDate(LocalDate.now());

        aplicarFiltro();
    }

    // =====================================================
    // Gráfico: usuários por perfil (Bar)
    // =====================================================
    private String buildUsersByProfileChartJson(List<ProfileCountDTO> dados) {

        BarData data = new BarData();

        // Paleta igual à imagem de referência: ciano, azul, roxo, verde (cicla se
        // houver mais perfis)
        List<RGBAColor> palette = List.of(
                new RGBAColor(34, 211, 238, 0.9), // ciano
                new RGBAColor(59, 130, 246, 0.9), // azul
                new RGBAColor(168, 85, 247, 0.9), // roxo
                new RGBAColor(16, 185, 129, 0.9), // verde
                new RGBAColor(234, 179, 8, 0.9) // dourado (extra, caso haja 5º perfil)
        );

        List<RGBAColor> barColors = new java.util.ArrayList<>();
        BarDataset dataset = new BarDataset()
                .setLabel("Distribuição por Função")
                .setBackgroundColor(barColors)
                .setBorderWidth(0);

        if (dados == null || dados.isEmpty()) {
            data.addLabels("Sem dados");
            dataset.addData(0);
            data.addDataset(dataset);
            return new BarChart(data, new BarOptions()).toJson();
        }

        for (ProfileCountDTO item : dados) {
            data.addLabels(toProfileLabel(item.getPerfil()));
            dataset.addData(item.getTotal() != null ? item.getTotal() : 0L);
            barColors.add(palette.get(barColors.size() % palette.size()));
        }

        dataset.setBackgroundColor(barColors);
        data.addDataset(dataset);

        // título/legenda removidos: o extender no XHTML já esconde legend/title
        // e aplica o styling dos eixos igual à imagem
        return new BarChart(data, new BarOptions()).toJson();
    }

    private String toProfileLabel(com.SistemSchool.io.Perfil perfil) {
        if (perfil == null) {
            return "Não definido";
        }

        return switch (perfil) {
            case ADMIN -> "Administrador";
            case SECRETARY -> "Secretária";
            case FINANCIAL -> "Financeiro";
            case PEDAGOGICAL -> "Pedagógico";
        };
    }

    public String getFilterPeriodLabel() {
        if (filter == null || filter.getStartDate() == null || filter.getEndDate() == null) {
            return "";
        }
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return filter.getStartDate().format(fmt) + " – " + filter.getEndDate().format(fmt);
    }
    // =====================================================
    // Gráfico: total por categoria (Pie)
    // =====================================================

    private String buildCategoryChartJson(List<CategoryTotalDTO> categoryTotals) {

        PieData data = new PieData();

        // setBackgroundColor(Object) só aceita UM valor -> passamos uma lista de cores
        List<RGBAColor> palette = List.of(
                new RGBAColor(11, 31, 58, 0.85),
                new RGBAColor(197, 160, 60, 0.85),
                new RGBAColor(180, 40, 40, 0.85),
                new RGBAColor(60, 140, 90, 0.85),
                new RGBAColor(90, 90, 90, 0.85));

        PieDataset dataset = new PieDataset()
                .setLabel("Total por Categoria")
                .setBackgroundColor(palette);

        for (CategoryTotalDTO item : categoryTotals) {
            data.addLabels(item.getCategory());
            dataset.addData(item.getTotal());
        }

        data.addDataset(dataset);

        Title title = new Title().setDisplay(true).setText("Movimentos por Categoria");
        Plugins plugins = new Plugins().setTitle(title);

        // PieChart exige PieOptions, não BarOptions
        PieOptions options = new PieOptions().setPlugins(plugins);

        return new PieChart(data, options).toJson();
    }

    // =====================================================
    // Gráfico: evolução mensal (Line)
    // =====================================================

    private String buildMonthlyEvolutionChartJson(List<MonthlyFinancialProjection> monthly) {

        LineData data = new LineData();

        LineDataset incomeDataset = new LineDataset()
                .setLabel("Receitas")
                .setBorderColor(new RGBAColor(60, 140, 90, 1.0))
                .setBackgroundColor(new RGBAColor(60, 140, 90, 0.2));
        // .setFill(...) removido: a versão atual da lib exige um objeto
        // Fill<?> (não um boolean). Sem essa chamada o Chart.js simplesmente
        // não pinta a área sob a linha — visualmente ainda funciona bem.
        // Se quiseres a área preenchida, dá para fazer isso direto no
        // template com Chart.js puro (data.datasets[i].fill = true no JS),
        // ou me diz que eu procuro o construtor certo de Fill na tua versão
        // exata da lib (confere no teu pom.xml).

        LineDataset expenseDataset = new LineDataset()
                .setLabel("Despesas")
                .setBorderColor(new RGBAColor(180, 40, 40, 1.0))
                .setBackgroundColor(new RGBAColor(180, 40, 40, 0.2));

        for (MonthlyFinancialProjection item : monthly) {
            data.addLabels(item.getYearMonth());
            incomeDataset.addData(item.getIncome());
            expenseDataset.addData(item.getExpense());
        }

        data.addDataset(incomeDataset);
        data.addDataset(expenseDataset);

        Title title = new Title().setDisplay(true).setText("Evolução Mensal (Receitas x Despesas)");
        Plugins plugins = new Plugins().setTitle(title);

        // LineChart exige LineOptions, não BarOptions
        LineOptions options = new LineOptions().setPlugins(plugins);

        return new LineChart(data, options).toJson();
    }

    public String goToDashboard() {
        return "/components/public/dashboard.xhtml?faces-redirect=true";
    }

    // ---- Getters/Setters ----

    public DashboardStatsDTO getStats() {
        return stats;
    }

    public String getUsersByProfileChartJson() {
        return usersByProfileChartJson;
    }

    public List<ProfileCountDTO> getUsersByProfile() {
        return usersByProfile;
    }

    public DashboardFilterDTO getFilter() {
        return filter;
    }

    public void setFilter(DashboardFilterDTO filter) {
        this.filter = filter;
    }

    public FinancialFilterStatsDTO getFinancialStats() {
        return financialStats;
    }

    public List<String> getAvailableCategories() {
        return availableCategories;
    }

    public List<String> getMovementTypes() {
        return movementTypes;
    }

    public String getCategoryChartJson() {
        return categoryChartJson;
    }

    public String getMonthlyEvolutionChartJson() {
        return monthlyEvolutionChartJson;
    }
}