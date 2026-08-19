package com.SistemSchool.modulo_pedagogico.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import com.SistemSchool.modulo_pedagogico.dto.AlunoNotaDTO;
import com.SistemSchool.modulo_pedagogico.dto.PautaDTO;
import com.SistemSchool.modulo_pedagogico.model.Discipline;
import com.SistemSchool.modulo_pedagogico.repository.DisciplineRepository;
import com.SistemSchool.modulo_pedagogico.repository.GradeRepository;
import com.SistemSchool.modulo_secrtaria.model.SchoolClass;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.SchoolClassRepository;
import com.SistemSchool.modulo_secrtaria.repository.StudentRepository;

@Service
@Transactional
public class PautaService {

    // Ajusta à escala de notas realmente usada (0-10, 0-20, etc.)
    private static final double NOTA_APROVACAO = 10.0;

    private final SchoolClassRepository schoolClassRepository;
    private final DisciplineRepository disciplineRepository;
    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;

    public PautaService(SchoolClassRepository schoolClassRepository,
                         DisciplineRepository disciplineRepository,
                         StudentRepository studentRepository,
                         GradeRepository gradeRepository) {
        this.schoolClassRepository = schoolClassRepository;
        this.disciplineRepository = disciplineRepository;
        this.studentRepository = studentRepository;
        this.gradeRepository = gradeRepository;
    }

    public PautaDTO gerarPauta(Long schoolClassPk, Long disciplinePk, Integer trimester) {

        SchoolClass schoolClass = schoolClassRepository.findById(schoolClassPk)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada com id: " + schoolClassPk));

        Discipline discipline = disciplineRepository.findById(disciplinePk)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com id: " + disciplinePk));

        List<Object[]> medias = gradeRepository.calcularMediasDaTurma(schoolClassPk, disciplinePk, trimester);

        List<AlunoNotaDTO> linhas = new ArrayList<>();
        int numero = 1;
        for (Object[] linha : medias) {
            Long studentPk = (Long) linha[0];
            Double media = (Double) linha[1];

            Student student = studentRepository.findById(studentPk)
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado com id: " + studentPk));

            String situacao = media == null ? "-" : (media >= NOTA_APROVACAO ? "Aprovado" : "Reprovado");

            linhas.add(new AlunoNotaDTO(numero++, student.getFullName(), media, situacao));
        }

        return new PautaDTO()
                .schoolClassName(schoolClass.getClassName())
                .disciplineName(discipline.getDisciplineName())
                .trimester(trimester)
                .academicYear(schoolClass.getAnoLectivo())
                .studentGrades(linhas);
    }
}