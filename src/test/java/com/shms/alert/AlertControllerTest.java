package com.shms.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shms.patient.Patient;
import com.shms.patient.PatientRepository;
import com.shms.vital.VitalRequest;
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
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private VitalRepository vitalRepository;

    @Autowired
    private AlertRepository alertRepository;

    private Patient patient;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        vitalRepository.deleteAll();
        patientRepository.deleteAll();

        Patient p = new Patient(
                "Mark Davis",
                "mark@example.com",
                "111222333",
                LocalDate.of(1988, 4, 12),
                "Male",
                "B+",
                "789 Pine Rd",
                "None"
        );
        patient = patientRepository.save(p);
    }

    @Test
    @DisplayName("Normal readings do not generate alerts")
    void normalReading_NoAlerts() throws Exception {
        VitalRequest normal = new VitalRequest(75, 118, 78, 98.0, 36.6, LocalDateTime.now());

        mockMvc.perform(post("/api/patients/" + patient.getId() + "/vitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(normal)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients/" + patient.getId() + "/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Abnormal readings generate alerts and allow retrieval and acknowledgment")
    void abnormalReading_GeneratesAlerts_AndAcknowledge() throws Exception {
        // High heart rate (145), low oxygen (88), high blood pressure (150/95), high fever (39.5)
        VitalRequest abnormal = new VitalRequest(145, 150, 95, 88.0, 39.5, LocalDateTime.now());

        mockMvc.perform(post("/api/patients/" + patient.getId() + "/vitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abnormal)))
                .andExpect(status().isCreated());

        // Check alerts were created
        mockMvc.perform(get("/api/patients/" + patient.getId() + "/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].status", everyItem(is("ACTIVE"))));

        // Get single alert
        Alert firstAlert = alertRepository.findAll().get(0);
        mockMvc.perform(get("/api/alerts/" + firstAlert.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstAlert.getId().intValue())))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        // Acknowledge alert
        mockMvc.perform(patch("/api/alerts/" + firstAlert.getId() + "/acknowledge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstAlert.getId().intValue())))
                .andExpect(jsonPath("$.status", is("ACKNOWLEDGED")))
                .andExpect(jsonPath("$.acknowledgedAt", notNullValue()));
    }
}
