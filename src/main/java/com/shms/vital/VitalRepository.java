package com.shms.vital;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VitalRepository extends JpaRepository<Vital, Long> {

    List<Vital> findByPatientIdOrderByRecordedAtDesc(Long patientId);

    Optional<Vital> findFirstByPatientIdOrderByRecordedAtDesc(Long patientId);
}
