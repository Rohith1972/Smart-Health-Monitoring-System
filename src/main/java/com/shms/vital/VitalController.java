package com.shms.vital;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VitalController {

    private final VitalService vitalService;

    public VitalController(VitalService vitalService) {
        this.vitalService = vitalService;
    }

    @PostMapping("/api/patients/{patientId}/vitals")
    @ResponseStatus(HttpStatus.CREATED)
    public VitalResponse recordVital(@PathVariable Long patientId,
                                     @Valid @RequestBody VitalRequest request) {
        return vitalService.recordVital(patientId, request);
    }

    @GetMapping("/api/patients/{patientId}/vitals")
    public List<VitalResponse> getVitalsByPatientId(@PathVariable Long patientId) {
        return vitalService.getVitalsByPatientId(patientId);
    }

    @GetMapping("/api/patients/{patientId}/vitals/latest")
    public VitalResponse getLatestVitalByPatientId(@PathVariable Long patientId) {
        return vitalService.getLatestVitalByPatientId(patientId);
    }

    @GetMapping("/api/vitals/{id}")
    public VitalResponse getVitalById(@PathVariable Long id) {
        return vitalService.getVitalById(id);
    }
}
