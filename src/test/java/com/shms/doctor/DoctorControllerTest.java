package com.shms.doctor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shms.alert.Alert;
import com.shms.alert.AlertRepository;
import com.shms.patient.Patient;
import com.shms.patient.PatientRepository;
import com.shms.vital.Vital;
import com.shms.vital.VitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private VitalRepository vitalRepository;

    @Autowired
    private AlertRepository alertRepository;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        vitalRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/doctors - Success returns 201 Created")
    void createDoctor_Success() throws Exception {
        DoctorRequest request = new DoctorRequest(
                "Dr. Gregory House",
                "house@diagnostics.org",
                "Nephrology & Infectious Disease",
                "+1-555-0199"
        );

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.fullName", is("Dr. Gregory House")))
                .andExpect(jsonPath("$.specialization", is("Nephrology & Infectious Disease")));
    }

    @Test
    @DisplayName("Doctor workflow: Create, Assign Patient, Retrieve Patients & Dashboard")
    void doctorFullWorkflow() throws Exception {
        // 1. Create Doctor
        Doctor doctor = new Doctor("Dr. Sarah Wilson", "sarah@hospital.com", "Cardiology", "555-1234");
        Doctor savedDoctor = doctorRepository.save(doctor);

        // 2. Create Patient
        Patient patient = new Patient("Patient Alpha", "alpha@test.com", "123", LocalDate.of(1980, 5, 1), "Male", "O+", "Address", "None");
        Patient savedPatient = patientRepository.save(patient);

        // 3. Assign Patient to Doctor
        mockMvc.perform(put("/api/doctors/" + savedDoctor.getId() + "/patients/" + savedPatient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedDoctorId", is(savedDoctor.getId().intValue())))
                .andExpect(jsonPath("$.assignedDoctorName", is("Dr. Sarah Wilson")));

        // 4. Retrieve assigned patients
        mockMvc.perform(get("/api/doctors/" + savedDoctor.getId() + "/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is("Patient Alpha")));

        // 5. Add a Vital and an Alert for the patient
        Vital vital = new Vital(savedPatient, 135, 150, 95, 93.0, 38.5, LocalDateTime.now());
        Vital savedVital = vitalRepository.save(vital);

        Alert alert = new Alert(savedPatient, savedVital, "HIGH_HEART_RATE", "Heart rate elevated", "HIGH");
        alertRepository.save(alert);

        // 6. Retrieve Doctor Dashboard
        mockMvc.perform(get("/api/doctors/" + savedDoctor.getId() + "/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId", is(savedDoctor.getId().intValue())))
                .andExpect(jsonPath("$.doctorName", is("Dr. Sarah Wilson")))
                .andExpect(jsonPath("$.totalPatients", is(1)))
                .andExpect(jsonPath("$.totalActiveAlerts", is(1)))
                .andExpect(jsonPath("$.patients[0].patientName", is("Patient Alpha")))
                .andExpect(jsonPath("$.patients[0].latestVital.heartRateBpm", is(135)))
                .andExpect(jsonPath("$.patients[0].activeAlerts", hasSize(1)));
    }
}
