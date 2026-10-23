package com.shms.doctor;

import com.shms.patient.PatientResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse createDoctor(@Valid @RequestBody DoctorRequest request) {
        return doctorService.createDoctor(request);
    }

    @GetMapping
    public List<DoctorResponse> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    public DoctorResponse getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id);
    }

    @PutMapping("/{doctorId}/patients/{patientId}")
    public PatientResponse assignPatientToDoctor(@PathVariable Long doctorId,
                                                @PathVariable Long patientId) {
        return doctorService.assignPatientToDoctor(doctorId, patientId);
    }

    @GetMapping("/{doctorId}/patients")
    public List<PatientResponse> getAssignedPatients(@PathVariable Long doctorId) {
        return doctorService.getAssignedPatients(doctorId);
    }

    @GetMapping("/{doctorId}/dashboard")
    public DoctorDashboardResponse getDoctorDashboard(@PathVariable Long doctorId) {
        return doctorService.getDoctorDashboard(doctorId);
    }
}
