package com.SistemSchool.modulo_pedagogico.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import com.SistemSchool.modulo_pedagogico.dto.BoletimDTO;
import com.SistemSchool.modulo_pedagogico.dto.DisciplineGradeDTO;
import com.SistemSchool.modulo_pedagogico.io.ReportCardStatus;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.ReportCard;
import com.SistemSchool.modulo_pedagogico.model.Schedule;
import com.SistemSchool.modulo_pedagogico.repository.GradeRepository;
import com.SistemSchool.modulo_pedagogico.repository.ReportCardRepository;
import com.SistemSchool.modulo_pedagogico.repository.ScheduleRepository;
import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.repository.EnrolmentRepository;

@Service
@Transactional
public class BoletimService {

    private final EnrolmentRepository enrolmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final GradeRepository gradeRepository;
    private final ReportCardRepository reportCardRepository;

    public BoletimService(EnrolmentRepository enrolmentRepository,
                           ScheduleRepository scheduleRepository,
                           GradeRepository gradeRepository,
                           ReportCardRepository reportCardRepository) {
        this.enrolmentRepository = enrolmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.gradeRepository = gradeRepository;
        this.reportCardRepository = reportCardRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // GERAÇÃO DO BOLETIM
    // ─────────────────────────────────────────────────────────────

    public BoletimDTO gerarBoletim(Long enrolmentPk, Integer trimester) {

        Enrolment enrolment = enrolmentRepository.findById(enrolmentPk)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada com id: " + enrolmentPk));

        List<Discipline> disciplinas = scheduleRepository
                .findBySchoolClass_PkSchoolClass(enrolment.getSchoolClass().getPkSchoolClass())
                .stream()
                .map(Schedule::getDiscipline)
                .distinct()
                .toList();

        List<DisciplineGradeDTO> notas = disciplinas.stream()
                .map(d -> new DisciplineGradeDTO(
                        d.getDisciplineName(),
                        d.getDisciplineCode(),
                        gradeRepository.calcularMediaFinal(
                                enrolment.getStudent().getPkStudent(),
                                d.getPkDiscipline(),
                                trimester)))
                .toList();

        ReportCard reportCard = reportCardRepository
                .findByEnrolment_PhEnrolmentAndTrimester(enrolmentPk, trimester)
                .orElseGet(() -> new ReportCard()
                        .enrolment(enrolment)
                        .trimester(trimester)
                        .status(ReportCardStatus.DRAFT));

        return new BoletimDTO()
                .studentName(enrolment.getStudent().getFullName())
                .studentNumber(enrolment.getEnrolmentNumer())
                .schoolClassName(enrolment.getSchoolClass().getClassName())
                .academicYear(enrolment.getSchoolClass().getAnoLectivo())
                .trimester(trimester)
                .period(reportCard.getPeriod())
                .disciplineGrades(notas)
                .behavior(reportCard.getBehavior())
                .observation(reportCard.getObservation());
    }

    // ─────────────────────────────────────────────────────────────
    // LANÇAMENTO MANUAL (comportamento / observação / período)
    // ─────────────────────────────────────────────────────────────

    public ReportCard salvarDadosManuais(Long enrolmentPk, Integer trimester, String period,
                                          String behavior, String observation) {

        Enrolment enrolment = enrolmentRepository.findById(enrolmentPk)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada com id: " + enrolmentPk));

        ReportCard reportCard = reportCardRepository
                .findByEnrolment_PhEnrolmentAndTrimester(enrolmentPk, trimester)
                .orElseGet(ReportCard::new);

        reportCard.setEnrolment(enrolment);
        reportCard.setTrimester(trimester);
        reportCard.setPeriod(period);
        reportCard.setBehavior(behavior);
        reportCard.setObservation(observation);
        reportCard.setStatus(ReportCardStatus.ISSUED);

        return reportCardRepository.save(reportCard);
    }
}