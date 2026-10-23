package com.shms.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/patients - Success returns 201 Created")
    void registerPatient_Success() throws Exception {
        PatientRequest request = new PatientRequest(
                "John Doe",
                "john.doe@example.com",
                "+1234567890",
                LocalDate.of(1990, 1, 1),
                "Male",
                "O+",
                "123 Medical Center Way",
                "No known drug allergies"
        );

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.fullName", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.bloodGroup", is("O+")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/patients - Duplicate email returns 409 Conflict")
    void registerPatient_DuplicateEmail() throws Exception {
        Patient existing = new Patient("Jane Doe", "duplicate@example.com", "123", LocalDate.of(1995, 5, 5), "Female", "A+", "Address", "None");
        patientRepository.save(existing);

        PatientRequest request = new PatientRequest(
                "Another Jane",
                "duplicate@example.com",
                "987",
                LocalDate.of(1995, 5, 5),
                "Female",
                "A+",
                "Address",
                "None"
        );

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("POST /api/patients - Invalid input returns 400 Bad Request")
    void registerPatient_ValidationFailure() throws Exception {
        PatientRequest request = new PatientRequest(
                "", // empty name
                "invalid-email", // invalid email
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.fullName", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.email", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/patients - Returns all patients")
    void getAllPatients() throws Exception {
        Patient p1 = new Patient("Patient One", "p1@example.com", "111", null, "Male", "B+", null, null);
        Patient p2 = new Patient("Patient Two", "p2@example.com", "222", null, "Female", "AB+", null, null);
        patientRepository.save(p1);
        patientRepository.save(p2);

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email", is("p1@example.com")))
                .andExpect(jsonPath("$[1].email", is("p2@example.com")));
    }

    @Test
    @DisplayName("GET /api/patients/{id} - Found returns 200, Not Found returns 404")
    void getPatientById() throws Exception {
        Patient p = new Patient("Alice Smith", "alice@example.com", "123", null, "Female", "O-", null, null);
        Patient saved = patientRepository.save(p);

        mockMvc.perform(get("/api/patients/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.fullName", is("Alice Smith")));

        mockMvc.perform(get("/api/patients/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("PUT /api/patients/{id} - Update details returns 200 OK")
    void updatePatient() throws Exception {
        Patient p = new Patient("Bob Brown", "bob@example.com", "123", LocalDate.of(1985, 3, 10), "Male", "A-", "Old Address", "None");
        Patient saved = patientRepository.save(p);

        PatientRequest updateRequest = new PatientRequest(
                "Robert Brown",
                "bob.updated@example.com",
                "999888777",
                LocalDate.of(1985, 3, 10),
                "Male",
                "A-",
                "New Address 456",
                "Mild asthma"
        );

        mockMvc.perform(put("/api/patients/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Robert Brown")))
                .andExpect(jsonPath("$.email", is("bob.updated@example.com")))
                .andExpect(jsonPath("$.address", is("New Address 456")))
                .andExpect(jsonPath("$.medicalNotes", is("Mild asthma")));
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} - Returns 204 No Content")
    void deletePatient() throws Exception {
        Patient p = new Patient("Charlie", "charlie@example.com", "555", null, "Male", "B-", null, null);
        Patient saved = patientRepository.save(p);

        mockMvc.perform(delete("/api/patients/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/patients/" + saved.getId()))
                .andExpect(status().isNotFound());
    }
}
