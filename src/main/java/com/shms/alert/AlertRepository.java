package com.shms.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<Alert> findByStatusOrderByCreatedAtDesc(String status);

    List<Alert> findByPatientIdAndStatusOrderByCreatedAtDesc(Long patientId, String status);

    long countByPatientIdAndStatus(Long patientId, String status);

    long countByStatus(String status);
}
