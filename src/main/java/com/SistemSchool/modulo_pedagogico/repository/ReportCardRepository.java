package com.SistemSchool.modulo_pedagogico.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SistemSchool.modulo_pedagogico.model.ReportCard;

@Repository
public interface ReportCardRepository extends JpaRepository<ReportCard, Long> {

    Optional<ReportCard> findByEnrolment_PhEnrolmentAndTrimester(Long enrolmentPk, Integer trimester);
}