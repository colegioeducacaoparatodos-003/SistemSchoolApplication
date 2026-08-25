package com.SistemSchool.modulo_pedagogico.service;

import com.SistemSchool.modulo_pedagogico.dto.BoletimDTO;
import com.SistemSchool.modulo_pedagogico.dto.BoletimDisciplineRowDTO;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;
import com.SistemSchool.modulo_pedagogico.model.Grade;
import com.SistemSchool.modulo_pedagogico.repository.GradeRepository;

import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.EnrolmentRepository;
import com.SistemSchool.modulo_secrtaria.repository.StudentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BoletimService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final EnrolmentRepository enrolmentRepository;

    public BoletimService(GradeRepository gradeRepository,
                          StudentRepository studentRepository,
                          EnrolmentRepository enrolmentRepository) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.enrolmentRepository = enrolmentRepository;
    }

    /**
     * Gera o Boletim de Notas para um aluno, matrícula e trimestre.
     * Formato angolano: Disciplina | MAC | NPP | NPT | MT
     */
    public BoletimDTO generateBoletim(Long studentPk, Long enrolmentPk, Integer trimester) {
        Student student = studentRepository.findById(studentPk)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado."));
        Enrolment enrolment = enrolmentRepository.findById(enrolmentPk)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada."));

        List<Grade> grades = gradeRepository.findByStudentAndEnrolmentAndTrimester(
                studentPk, enrolmentPk, trimester);

        // Agrupar por disciplina
        Map<Long, List<Grade>> byDiscipline = grades.stream()
                .collect(Collectors.groupingBy(g -> g.getEvaluation().getDiscipline().getPkDiscipline()));

        List<BoletimDisciplineRowDTO> rows = new ArrayList<>();
        double generalSum = 0;
        int generalCount = 0;

        for (List<Grade> list : byDiscipline.values()) {
            BoletimDisciplineRowDTO row = new BoletimDisciplineRowDTO();
            row.setDisciplineName(list.get(0).getEvaluation().getDiscipline().getDisciplineName());

            for (Grade g : list) {
                EvaluationType type = g.getEvaluation().getType();
                if (type == EvaluationType.CONTINUOUS_ASSESSMENT) {
                    row.setMac(g.getValue());
                } else if (type == EvaluationType.TEACHER_TEST) {
                    row.setNpp(g.getValue());
                } else if (type == EvaluationType.FINAL_TEST) {
                    row.setNpt(g.getValue());
                }
            }

            row.setMt(calcularMediaTrimestral(list));
            if (row.getMt() != null) {
                generalSum += row.getMt();
                generalCount++;
            }
            rows.add(row);
        }

        // Ordenar por nome da disciplina
        rows.sort(Comparator.comparing(BoletimDisciplineRowDTO::getDisciplineName));

        Double generalAvg = generalCount > 0 ? generalSum / generalCount : null;

        BoletimDTO dto = new BoletimDTO();
        dto.setStudentFullName(student.getFullName());
        dto.setEnrolmentNumber(enrolment.getEnrolmentNumer());
        dto.setSchoolClassName(enrolment.getSchoolClass() != null ? enrolment.getSchoolClass().getClassName() : "—");
        dto.setAcademicYear(Year.now().getValue() + "/" + (Year.now().getValue() + 1));
        dto.setTrimester(trimester);
        dto.setPeriod("Manhã"); // ou buscar da matrícula
        dto.setDisciplines(rows);
        dto.setGeneralAverage(generalAvg);
        dto.setFinalResult(generalAvg != null && generalAvg >= 10 ? "Aprovado" : "Reprovado");

        return dto;
    }

    private Double calcularMediaTrimestral(List<Grade> grades) {
        double mac = 0, npp = 0, npt = 0;
        double wMac = 0, wNpp = 0, wNpt = 0;

        for (Grade g : grades) {
            EvaluationType type = g.getEvaluation().getType();
            Double weight = g.getEvaluation().getWeight();
            if (weight == null) weight = 1.0;

            if (type == EvaluationType.CONTINUOUS_ASSESSMENT) {
                mac = g.getValue(); wMac = weight;
            } else if (type == EvaluationType.TEACHER_TEST) {
                npp = g.getValue(); wNpp = weight;
            } else if (type == EvaluationType.FINAL_TEST) {
                npt = g.getValue(); wNpt = weight;
            }
        }

        double totalWeight = wMac + wNpp + wNpt;
        if (totalWeight == 0) return null;
        return (mac * wMac + npp * wNpp + npt * wNpt) / totalWeight;
    }
}
