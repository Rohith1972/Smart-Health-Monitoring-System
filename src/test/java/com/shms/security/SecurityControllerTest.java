package com.shms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - Successfully registers user and returns JWT")
    void registerUser_Success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "testadmin",
                "Password123!",
                Role.ROLE_ADMIN,
                null,
                null
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username", is("testadmin")))
                .andExpect(jsonPath("$.role", is("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Success returns JWT, failure returns 401")
    void loginWorkflow() throws Exception {
        RegisterRequest register = new RegisterRequest("doctor_bob", "SecretPass123", Role.ROLE_DOCTOR, null, 1L);
        authService.register(register);

        // Positive test: valid login
        AuthRequest validLogin = new AuthRequest("doctor_bob", "SecretPass123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role", is("ROLE_DOCTOR")));

        // Negative test: invalid password
        AuthRequest badPass = new AuthRequest("doctor_bob", "WrongPassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badPass)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("Protected endpoints: Missing token returns 401, valid token allows access")
    void tokenValidationOnProtectedEndpoints() throws Exception {
        // 1. Missing token -> 401
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));

        // 2. Invalid token -> 401
        mockMvc.perform(get("/api/patients")
                        .header("Authorization", "Bearer invalid.fake.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));

        // 3. Register admin and get valid token
        RegisterRequest adminReq = new RegisterRequest("admin_user", "AdminSecure123", Role.ROLE_ADMIN, null, null);
        AuthResponse auth = authService.register(adminReq);

        // 4. Access with valid token -> 200 OK
        mockMvc.perform(get("/api/patients")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk());
    }
}
