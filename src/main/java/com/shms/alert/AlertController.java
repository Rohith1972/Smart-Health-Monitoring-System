package com.shms.alert;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/api/patients/{patientId}/alerts")
    public List<AlertResponse> getAlertsByPatientId(@PathVariable Long patientId) {
        return alertService.getAlertsByPatientId(patientId);
    }

    @GetMapping("/api/alerts/{id}")
    public AlertResponse getAlertById(@PathVariable Long id) {
        return alertService.getAlertById(id);
    }

    @PatchMapping("/api/alerts/{id}/acknowledge")
    public AlertResponse acknowledgeAlert(@PathVariable Long id) {
        return alertService.acknowledgeAlert(id);
    }
}
