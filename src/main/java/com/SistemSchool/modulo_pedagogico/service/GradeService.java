package com.SistemSchool.modulo_pedagogico.service;

import com.SistemSchool.modulo_pedagogico.dto.GradeDTO;
import com.SistemSchool.modulo_pedagogico.interfaces.GradeTableProjection;
import com.SistemSchool.modulo_pedagogico.io.GradeStatus;
import com.SistemSchool.modulo_pedagogico.model.Evaluation;
import com.SistemSchool.modulo_pedagogico.model.Grade;
import com.SistemSchool.modulo_pedagogico.repository.EvaluationRepository;
import com.SistemSchool.modulo_pedagogico.repository.GradeRepository;

import com.SistemSchool.modulo_secrtaria.model.Enrolment;
import com.SistemSchool.modulo_secrtaria.model.Student;
import com.SistemSchool.modulo_secrtaria.repository.EnrolmentRepository;
import com.SistemSchool.modulo_secrtaria.repository.StudentRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GradeService {

    private final GradeRepository repository;
    private final EvaluationRepository evaluationRepository;
    private final EnrolmentRepository enrolmentRepository;
    private final StudentRepository studentRepository;

    public GradeService(GradeRepository gradeRepository, EvaluationRepository evaluationRepository,
            EnrolmentRepository enrolmentRepository, StudentRepository studentRepository) {
        this.repository = gradeRepository;
        this.evaluationRepository = evaluationRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.studentRepository = studentRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // CRUD PRINCIPAL
    // ─────────────────────────────────────────────────────────────

    public Grade save(Grade grade) {
        if (grade.getEvaluation() == null || grade.getEvaluation().getPkEvaluation() == null) {
            throw new RuntimeException("É necessário indicar a avaliação da nota.");
        }
        if (grade.getEnrolment() == null || grade.getEnrolment().getPhEnrolment() == null) {
            throw new RuntimeException("É necessário indicar a matrícula (enrolment) da nota.");
        }
        if (grade.getStudent() == null || grade.getStudent().getPkStudent() == null) {
            throw new RuntimeException("É necessário indicar o aluno da nota.");
        }
        if (repository.existsByEvaluation_PkEvaluationAndStudent_PkStudent(
                grade.getEvaluation().getPkEvaluation(), grade.getStudent().getPkStudent())) {
            throw new RuntimeException("Já existe uma nota lançada para este aluno nesta avaliação.");
        }

        Evaluation evaluation = evaluationRepository.findById(grade.getEvaluation().getPkEvaluation())
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada."));
        Enrolment enrolment = enrolmentRepository.findById(grade.getEnrolment().getPhEnrolment())
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada."));
        Student student = studentRepository.findById(grade.getStudent().getPkStudent())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado."));

        grade.setEvaluation(evaluation);
        grade.setEnrolment(enrolment);
        grade.setStudent(student);

        return repository.save(grade);
    }

    public void update(GradeDTO dto) {
        Grade grade = repository.findById(dto.getPkGrade())
                .orElseThrow(() -> new RuntimeException("Nota não encontrada com id: " + dto.getPkGrade()));

        if (dto.getEvaluationPk() != null
                && !dto.getEvaluationPk().equals(grade.getEvaluation().getPkEvaluation())) {
            Evaluation evaluation = evaluationRepository.findById(dto.getEvaluationPk())
                    .orElseThrow(() -> new RuntimeException("Avaliação não encontrada."));
            grade.setEvaluation(evaluation);
        }

        if (dto.getEnrolmentPk() != null
                && !dto.getEnrolmentPk().equals(grade.getEnrolment().getPhEnrolment())) {
            Enrolment enrolment = enrolmentRepository.findById(dto.getEnrolmentPk())
                    .orElseThrow(() -> new RuntimeException("Matrícula não encontrada."));
            grade.setEnrolment(enrolment);
        }

        if (dto.getStudentPk() != null
                && !dto.getStudentPk().equals(grade.getStudent().getPkStudent())) {
            Student student = studentRepository.findById(dto.getStudentPk())
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado."));
            grade.setStudent(student);
        }

        grade.setValue(dto.getValue());
        grade.setStatus(dto.getStatus());
        grade.setObservation(dto.getObservation());

        repository.save(grade);
    }

    public void delete(Long id) {
        try {
            Grade grade = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Nota não encontrada"));
            repository.delete(grade);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Não é possível eliminar esta nota porque está associada a outros registos.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR TODOS (lista completa com DTO)
    // ─────────────────────────────────────────────────────────────

    public List<GradeDTO> getAllGrades() {
        return repository.findAllGradesDTO();
    }

    // ─────────────────────────────────────────────────────────────
    // LAZY LOADING PARA TABELA
    // ─────────────────────────────────────────────────────────────

    public Page<GradeDTO> findLazy(int page, int size, Sort sort, Map<String, Object> filters) {
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<GradeTableProjection> projections = repository.findAllForTable(pageable);

        return projections.map(p -> new GradeDTO(
                p.getPkGrade(),
                p.getEvaluationPk(),
                p.getEvaluationDescription(),
                p.getEnrolmentPk(),
                p.getEnrolmentNumber(),
                p.getStudentPk(),
                p.getStudentFullName(),
                p.getValue(),
                p.getStatus() != null ? GradeStatus.valueOf(p.getStatus()) : null,
                p.getObservation(),
                p.getCreatedAt(),
                p.getUpdatedAt()));
    }

    // ─────────────────────────────────────────────────────────────
    // QUERIES UTILITÁRIAS
    // ─────────────────────────────────────────────────────────────

    public List<Grade> getByEvaluation(Long evaluationPk) {
        return repository.findByEvaluation_PkEvaluation(evaluationPk);
    }

    public List<Grade> getByEnrolment(Long enrolmentPk) {
        return repository.findByEnrolment_PhEnrolment(enrolmentPk);
    }

    public List<Grade> getByStudent(Long studentPk) {
        return repository.findByStudent_PkStudent(studentPk);
    }

    public List<Grade> getByStatus(GradeStatus status) {
        return repository.findByStatus(status);
    }

    public Grade getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota não encontrada com id: " + id));
    }

    public Grade findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota não encontrada com id: " + id));
    }
}