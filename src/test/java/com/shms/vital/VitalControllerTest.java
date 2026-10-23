package com.shms.vital;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shms.patient.Patient;
import com.shms.patient.PatientRepository;
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
class VitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private VitalRepository vitalRepository;

    @Autowired
    private com.shms.alert.AlertRepository alertRepository;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        vitalRepository.deleteAll();
        patientRepository.deleteAll();

        Patient patient = new Patient(
                "Jane Doe",
                "jane.vitals@example.com",
                "123456789",
                LocalDate.of(1992, 8, 20),
                "Female",
                "A+",
                "456 Health St",
                "Healthy individual"
        );
        testPatient = patientRepository.save(patient);
    }

    @Test
    @DisplayName("POST /api/patients/{patientId}/vitals - Success returns 201 Created")
    void recordVital_Success() throws Exception {
        VitalRequest request = new VitalRequest(
                72,
                120,
                80,
                98.5,
                36.6,
                LocalDateTime.now()
        );

        mockMvc.perform(post("/api/patients/" + testPatient.getId() + "/vitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.patientId", is(testPatient.getId().intValue())))
                .andExpect(jsonPath("$.heartRateBpm", is(72)))
                .andExpect(jsonPath("$.systolicBloodPressure", is(120)))
                .andExpect(jsonPath("$.diastolicBloodPressure", is(80)))
                .andExpect(jsonPath("$.oxygenSaturation", is(98.5)))
                .andExpect(jsonPath("$.temperatureCelsius", is(36.6)));
    }

    @Test
    @DisplayName("POST /api/patients/{patientId}/vitals - Patient not found returns 404")
    void recordVital_PatientNotFound() throws Exception {
        VitalRequest request = new VitalRequest(75, 120, 80, 99.0, 37.0, null);

        mockMvc.perform(post("/api/patients/999999/vitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("POST /api/patients/{patientId}/vitals - Invalid values return 400 Bad Request")
    void recordVital_ValidationFailure() throws Exception {
        VitalRequest badRequest = new VitalRequest(
                10, // too low (< 20)
                500, // too high (> 300)
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/patients/" + testPatient.getId() + "/vitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.heartRateBpm", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/patients/{patientId}/vitals - Returns patient vital history")
    void getPatientVitals() throws Exception {
        Vital v1 = new Vital(testPatient, 70, 115, 75, 99.0, 36.5, LocalDateTime.now().minusHours(2));
        Vital v2 = new Vital(testPatient, 75, 120, 80, 98.0, 36.8, LocalDateTime.now().minusHours(1));
        vitalRepository.save(v1);
        vitalRepository.save(v2);

        mockMvc.perform(get("/api/patients/" + testPatient.getId() + "/vitals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/patients/{patientId}/vitals/latest - Returns latest vital reading")
    void getLatestVital() throws Exception {
        Vital v1 = new Vital(testPatient, 70, 115, 75, 99.0, 36.5, LocalDateTime.now().minusHours(2));
        Vital v2 = new Vital(testPatient, 85, 130, 85, 97.0, 37.2, LocalDateTime.now().minusMinutes(10));
        vitalRepository.save(v1);
        vitalRepository.save(v2);

        mockMvc.perform(get("/api/patients/" + testPatient.getId() + "/vitals/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heartRateBpm", is(85)))
                .andExpect(jsonPath("$.systolicBloodPressure", is(130)));
    }

    @Test
    @DisplayName("GET /api/vitals/{id} - Returns vital by ID")
    void getVitalById() throws Exception {
        Vital v = new Vital(testPatient, 78, 122, 82, 98.0, 36.7, LocalDateTime.now());
        Vital saved = vitalRepository.save(v);

        mockMvc.perform(get("/api/vitals/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.heartRateBpm", is(78)));
    }
}
