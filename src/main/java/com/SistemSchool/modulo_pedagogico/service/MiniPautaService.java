package com.SistemSchool.modulo_pedagogico.service;

import com.SistemSchool.modulo_pedagogico.dto.MiniPautaDTO;
import com.SistemSchool.modulo_pedagogico.dto.MiniPautaStudentRowDTO;
import com.SistemSchool.modulo_pedagogico.io.EvaluationType;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;
import com.SistemSchool.modulo_pedagogico.model.Grade;
import com.SistemSchool.modulo_pedagogico.repository.GradeRepository;

import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.model.Schedule;
import com.SistemSchool.modulo_pedagogico.repository.DisciplineRepository;
import com.SistemSchool.modulo_pedagogico.repository.ScheduleRepository;

import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MiniPautaService {

    private final GradeRepository gradeRepository;
    private final DisciplineRepository disciplineRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final ScheduleRepository scheduleRepository;

    public MiniPautaService(GradeRepository gradeRepository,
            DisciplineRepository disciplineRepository,
            SchoolClassRepository schoolClassRepository,
            ScheduleRepository scheduleRepository) {
        this.gradeRepository = gradeRepository;
        this.disciplineRepository = disciplineRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * Gera a Mini-Pauta para uma disciplina, turma e trimestre específicos.
     * Formato angolano: MAC | NPP | NPT | MT | MFD | Observação
     */
    public MiniPautaDTO generateMiniPauta(Long disciplinePk, Long schoolClassPk, Integer trimester) {
        Discipline discipline = disciplineRepository.findById(disciplinePk)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
        SchoolClass schoolClass = schoolClassRepository.findById(schoolClassPk)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada."));

        // Buscar professor responsável (via horário)
        String teacherName = scheduleRepository.findByDiscipline_PkDiscipline(disciplinePk).stream()
                .filter(s -> s.getSchoolClass() != null
                        && s.getSchoolClass().getPkSchoolClass() != null
                        && s.getSchoolClass().getPkSchoolClass().equals(schoolClassPk))
                .findFirst()
                .map(s -> {
                    if (s.getTeacher() != null && s.getTeacher().getDisplayName() != null) {
                        return s.getTeacher().getDisplayName(); // ✅ Usa getDisplayName()
                    }
                    return "—";
                })
                .orElse("—");

        List<Grade> grades = gradeRepository.findByDisciplineAndTrimesterAndClass(
                disciplinePk, trimester, schoolClassPk);

        // Agrupar por aluno
        Map<Long, List<Grade>> byStudent = grades.stream()
                .collect(Collectors.groupingBy(g -> g.getStudent().getPkStudent()));

        List<MiniPautaStudentRowDTO> rows = new ArrayList<>();
        int number = 1;

        for (Map.Entry<Long, List<Grade>> entry : byStudent.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().get(0).getStudent().getFullName()))
                .toList()) {

            Student student = entry.getValue().get(0).getStudent();
            MiniPautaStudentRowDTO row = new MiniPautaStudentRowDTO();
            row.setStudentNumber(number++);
            row.setStudentPk(student.getPkStudent());
            row.setStudentFullName(student.getFullName());

            // Mapear notas por tipo de avaliação
            for (Grade g : entry.getValue()) {
                EvaluationType type = g.getEvaluation().getType();
                if (type == EvaluationType.CONTINUOUS_ASSESSMENT) {
                    row.setMac(g.getValue());
                } else if (type == EvaluationType.TEACHER_TEST) {
                    row.setNpp(g.getValue());
                } else if (type == EvaluationType.FINAL_TEST) {
                    row.setNpt(g.getValue());
                }
            }

            // Calcular MT (média trimestral ponderada)
            row.setMt(calcularMediaTrimestral(entry.getValue()));

            // Buscar MFD acumulada (média final até agora)
            Double mfd = calcularMediaFinalDisciplina(student.getPkStudent(), disciplinePk, trimester);
            row.setMfd(mfd);

            // Observação automática baseada na MT
            if (row.getMt() != null) {
                row.setObservation(row.getMt() >= 10 ? "Aprovado" : "Reprovado");
            }

            rows.add(row);
        }

        // Estatísticas da turma
        double classAvg = rows.stream()
                .filter(r -> r.getMt() != null)
                .mapToDouble(MiniPautaStudentRowDTO::getMt)
                .average().orElse(0.0);

        long approved = rows.stream().filter(r -> r.getMt() != null && r.getMt() >= 10).count();
        long failed = rows.stream().filter(r -> r.getMt() != null && r.getMt() < 10).count();

        MiniPautaDTO dto = new MiniPautaDTO();
        dto.setSchoolName("Escola Primária N.º 7 Santa Teresinha"); // ou buscar da config
        dto.setDisciplineName(discipline.getDisciplineName());
        dto.setSchoolClassName(schoolClass.getClassName());
        dto.setSchoolClassCode(schoolClass.getClassCode());
        dto.setTeacherName(teacherName);
        dto.setTrimester(trimester);
        dto.setAcademicYear(Year.now().getValue() + "/" + (Year.now().getValue() + 1));
        dto.setStudents(rows);
        dto.setClassAverage(classAvg);
        dto.setApprovedCount(approved);
        dto.setFailedCount(failed);

        return dto;
    }

    /**
     * Calcula a média trimestral ponderada (MAC, NPP, NPT).
     * Pesos padrão: MAC=0.30, NPP=0.30, NPT=0.40
     */
    private Double calcularMediaTrimestral(List<Grade> grades) {
        double mac = 0, npp = 0, npt = 0;
        double wMac = 0, wNpp = 0, wNpt = 0;

        for (Grade g : grades) {
            EvaluationType type = g.getEvaluation().getType();
            Double weight = g.getEvaluation().getWeight();
            if (weight == null)
                weight = 1.0;

            if (type == EvaluationType.CONTINUOUS_ASSESSMENT) {
                mac = g.getValue();
                wMac = weight;
            } else if (type == EvaluationType.TEACHER_TEST) {
                npp = g.getValue();
                wNpp = weight;
            } else if (type == EvaluationType.FINAL_TEST) {
                npt = g.getValue();
                wNpt = weight;
            }
        }

        double totalWeight = wMac + wNpp + wNpt;
        if (totalWeight == 0)
            return null;
        return (mac * wMac + npp * wNpp + npt * wNpt) / totalWeight;
    }

    /**
     * Média final da disciplina acumulada até um determinado trimestre.
     */
    private Double calcularMediaFinalDisciplina(Long studentPk, Long disciplinePk, Integer trimester) {
        double sum = 0;
        int count = 0;
        for (int t = 1; t <= trimester; t++) {
            Double mt = gradeRepository.calcularMediaFinal(studentPk, disciplinePk, t);
            if (mt != null) {
                sum += mt;
                count++;
            }
        }
        return count > 0 ? sum / count : null;
    }
}
