package com.shms.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shms.alert.AlertRepository;
import com.shms.doctor.DoctorRepository;
import com.shms.doctor.DoctorRequest;
import com.shms.patient.PatientRepository;
import com.shms.patient.PatientRequest;
import com.shms.security.AuthRequest;
import com.shms.security.RegisterRequest;
import com.shms.security.Role;
import com.shms.security.UserRepository;
import com.shms.vital.VitalRepository;
import com.shms.vital.VitalRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FullWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private VitalRepository vitalRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        vitalRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Complete SHMS End-to-End Workflow: Phase 1 to Phase 6")
    @WithMockUser(roles = "ADMIN")
    void testCompleteWorkflow() throws Exception {
        // Step 1: Register a patient
        PatientRequest patientRequest = new PatientRequest(
                "Michael Scott",
                "michael.scott@dundermifflin.com",
                "+1-555-0144",
                LocalDate.of(1965, 3, 15),
                "Male",
                "A+",
                "Scranton Business Park",
                "Mild hypertension"
        );

        MvcResult patientResult = mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn();

        JsonNode patientNode = objectMapper.readTree(patientResult.getResponse().getContentAsString());
        long patientId = patientNode.get("id").asLong();

        // Step 2: Create a doctor
        DoctorRequest doctorRequest = new DoctorRequest(
                "Dr. Leonard McCoy",
                "mccoy@starfleet.org",
                "Cardiology",
                "+1-555-0170"
        );

        MvcResult doctorResult = mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctorRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn();

        JsonNode doctorNode = objectMapper.readTree(doctorResult.getResponse().getContentAsString());
        long doctorId = doctorNode.get("id").asLong();

        // Step 3: Assign the patient to the doctor
        mockMvc.perform(put("/api/doctors/" + doctorId + "/patients/" + patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedDoctorId", is((int) doctorId)))
                .andExpect(jsonPath("$.assignedDoctorName", is("Dr. Leonard McCoy")));

        // Step 4: Submit vital readings for that patient (abnormal reading: tachycardia + fever + hypertension)
        VitalRequest vitalReading = new VitalRequest(
                135, // Heart rate > 100 (abnormal)
                155, // Systolic BP > 120 (abnormal)
                95,  // Diastolic BP > 80 (abnormal)
                92.0,// Oxygen < 95% (abnormal)
                38.8,// Temp > 37.5°C (abnormal)
                LocalDateTime.now()
        );

        mockMvc.perform(post("/api/patients/" + patientId + "/vitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vitalReading)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId", is((int) patientId)))
                .andExpect(jsonPath("$.heartRateBpm", is(135)));

        // Step 5: Verify that abnormal readings generated alerts
        MvcResult alertsResult = mockMvc.perform(get("/api/patients/" + patientId + "/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andReturn();

        JsonNode alertsNode = objectMapper.readTree(alertsResult.getResponse().getContentAsString());
        long alertId = alertsNode.get(0).get("id").asLong();

        // Step 6: Retrieve the doctor's dashboard
        mockMvc.perform(get("/api/doctors/" + doctorId + "/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId", is((int) doctorId)))
                .andExpect(jsonPath("$.totalPatients", is(1)))
                .andExpect(jsonPath("$.totalActiveAlerts", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.patients[0].patientId", is((int) patientId)))
                .andExpect(jsonPath("$.patients[0].latestVital.heartRateBpm", is(135)));

        // Step 7: Acknowledge an alert
        mockMvc.perform(patch("/api/alerts/" + alertId + "/acknowledge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) alertId)))
                .andExpect(jsonPath("$.status", is("ACKNOWLEDGED")))
                .andExpect(jsonPath("$.acknowledgedAt", notNullValue()));

        // Step 8: Register and log in as the doctor, then verify authorized access with JWT
        RegisterRequest docUser = new RegisterRequest("dr_mccoy", "BonesPass2026!", Role.ROLE_DOCTOR, null, doctorId);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(docUser)))
                .andExpect(status().isCreated());

        AuthRequest loginRequest = new AuthRequest("dr_mccoy", "BonesPass2026!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        String jwtToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
        assertNotNull(jwtToken);

        // Access dashboard using the Doctor's JWT Bearer token
        mockMvc.perform(get("/api/doctors/" + doctorId + "/dashboard")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorName", is("Dr. Leonard McCoy")));
    }
}
