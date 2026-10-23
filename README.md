# Smart Health Monitoring System (SHMS) — Backend

A robust, simple, and functional backend REST API for a **Smart Health Monitoring System (SHMS)** built with **Java 21**, **Spring Boot 3.4.3**, and **MySQL**.

This system allows patients to register and manage basic medical information, store chronological vital readings, generate automated threshold-based alerts for abnormal vitals, allow doctors to manage assigned patients, and view aggregate health dashboards with real-time WebSocket updates.

---

## 1. Technologies Used

- **Java**: Version 21 (LTS)
- **Framework**: Spring Boot 3.4.3
  - Spring Web (REST API)
  - Spring Data JPA (Hibernate ORM)
  - Jakarta Bean Validation
  - Spring Security & JJWT (0.12.6) for stateless token authentication
  - Spring WebSocket with STOMP messaging for real-time updates
- **Database**: MySQL 9.5 / 8.x (`shms_db`)
- **Build Tool**: Apache Maven (`mvnw`)
- **API Testing**: Postman & MockMvc Test Suite

---

## 2. Architecture & Design Principles

The application adheres to clean, student-friendly Spring Boot conventions:
```text
Client (Postman / Frontend / WebSocket)
   │
   ▼
Controllers (HTTP REST / STOMP WebSocket)
   │
   ▼
Services (Business logic, Threshold rules, Security)
   │
   ▼
Repositories (Spring Data JPA)
   │
   ▼
MySQL Database (shms_db)
```

- **Constructor Injection**: All dependencies injected via constructors.
- **DTO Pattern**: Clean request/response Java records separating API contracts from database entities.
- **Unified Error Handling**: Single `@RestControllerAdvice` returning structured JSON error responses (400, 401, 403, 404, 409, 500) without exposing internal stack traces.
- **No Unnecessary Frameworks**: No Lombok, no MapStruct, no external message brokers (Kafka/Redis) or microservice overhead.

---

## 3. Package Structure

```text
com.shms
├── ShmsApplication.java                     # Spring Boot Application entry point
├── common
│   ├── DuplicateResourceException.java      # 409 Conflict
│   ├── ErrorResponse.java                   # Uniform error JSON structure
│   ├── GlobalExceptionHandler.java          # Global REST Exception Handler
│   └── ResourceNotFoundException.java       # 404 Not Found
├── patient
│   ├── Patient.java                         # JPA Entity for patients
│   ├── PatientRequest.java                  # Registration & update DTO
│   ├── PatientResponse.java                 # Response DTO
│   ├── PatientRepository.java               # JPA Repository
│   ├── PatientService.java                  # Business logic & validations
│   └── PatientController.java               # REST Endpoints (/api/patients)
├── vital
│   ├── Vital.java                           # JPA Entity for vital signs
│   ├── VitalRequest.java                    # Vital reading input DTO
│   ├── VitalResponse.java                   # Vital reading response DTO
│   ├── VitalRepository.java                 # Query methods for vitals
│   ├── VitalService.java                    # Vitals persistence & evaluation
│   └── VitalController.java                 # REST Endpoints
├── alert
│   ├── Alert.java                           # JPA Entity for health alerts
│   ├── AlertThresholdConfig.java            # Demonstration threshold parameters
│   ├── AlertResponse.java                   # Alert DTO
│   ├── AlertRepository.java                 # Alert query methods
│   ├── AlertService.java                    # Rule evaluation & acknowledgment
│   └── AlertController.java                 # REST Endpoints (/api/alerts)
├── doctor
│   ├── Doctor.java                          # JPA Entity for doctors
│   ├── DoctorRequest.java                   # Doctor registration DTO
│   ├── DoctorResponse.java                  # Doctor profile DTO
│   ├── DoctorDashboardResponse.java         # Aggregated health dashboard DTO
│   ├── DoctorRepository.java                # JPA Repository
│   ├── DoctorService.java                   # Patient assignment & dashboard
│   └── DoctorController.java                # REST Endpoints (/api/doctors)
├── security
│   ├── Role.java                            # ROLE_ADMIN, ROLE_DOCTOR, ROLE_PATIENT
│   ├── User.java                            # JPA Entity for auth credentials
│   ├── UserRepository.java                  # User persistence
│   ├── JwtService.java                      # Token generation & verification
│   ├── JwtAuthenticationFilter.java         # Bearer token filter
│   ├── CustomUserDetailsService.java        # Spring Security UserDetails provider
│   ├── CustomAuthenticationEntryPoint.java  # 401 JSON error handler
│   ├── CustomAccessDeniedHandler.java       # 403 JSON error handler
│   ├── SecurityConfig.java                  # Security filter chain & permissions
│   ├── RegisterRequest.java                 # User registration DTO
│   ├── AuthRequest.java                     # Login credentials DTO
│   ├── AuthResponse.java                    # JWT Token response DTO
│   ├── AuthService.java                     # BCrypt password hashing & auth logic
│   └── AuthController.java                  # REST Endpoints (/api/auth)
└── websocket
    ├── WebSocketConfig.java                 # STOMP configuration (/ws, /topic)
    └── NotificationService.java             # Real-time event broadcasting
```

---

## 4. Database Setup & Configuration

1. **Create MySQL Database**:
   Log in to MySQL and create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS shms_db;
   ```

2. **Configure Database Credentials**:
   In `src/main/resources/application.properties`:
   ```properties
   spring.application.name=shms
   server.port=8080

   spring.datasource.url=jdbc:mysql://localhost:3306/shms_db
   spring.datasource.username=${DB_USERNAME:root}
   spring.datasource.password=${DB_PASSWORD:YOUR_PASSWORD}
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   spring.jpa.open-in-view=false
   ```
   > You can also supply your credentials as environment variables: `DB_USERNAME` and `DB_PASSWORD`.

---

## 5. How to Run & Test the Application

### 1. Compile & Execute Tests
```powershell
.\mvnw.cmd test
```
All **22 automated unit and integration tests** will execute and verify database schema creation, input validation, JWT authentication, and the end-to-end user journey.

### 2. Start the Server
```powershell
.\mvnw.cmd spring-boot:run
```
The server will start on port `8080`.

### 3. Postman Import
Import the comprehensive master collection:
- File path: `postman/SHMS_Master_Postman_Collection.json`
- Base URL: `http://localhost:8080`
- The collection automatically handles setting collection variables (`jwtToken`, `patientId`, `doctorId`, etc.) from responses.

---

## 6. End-to-End Demonstration Workflow

```text
Step 1: Register User & Login        --> Receive Bearer JWT Token
Step 2: Register Patient             --> Creates Patient record (ID: 1)
Step 3: Create Doctor Profile        --> Creates Doctor record (ID: 1)
Step 4: Assign Patient to Doctor     --> Links patient to doctor
Step 5: Submit Normal Vitals         --> Saved without generating alerts
Step 6: Submit Abnormal Vitals       --> Triggers automated health alerts
Step 7: Retrieve Doctor Dashboard    --> Aggregates patient info, vitals, active alerts
Step 8: Acknowledge Alert            --> Marks alert status as ACKNOWLEDGED
```

---

## 7. Complete API Endpoint Reference

### Authentication (`/api/auth`)
| Method | Endpoint | Access | Description | Status |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register user (`ROLE_ADMIN`, `ROLE_DOCTOR`, `ROLE_PATIENT`) | `201 Created` |
| `POST` | `/api/auth/login` | Public | Login with username and password, returns JWT | `200 OK` |

#### Example: Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "admin_user",
  "password": "Password123!",
  "role": "ROLE_ADMIN"
}
```

#### Example: Login User
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin_user",
  "password": "Password123!"
}
```
**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "admin_user",
  "role": "ROLE_ADMIN",
  "patientId": null,
  "doctorId": null,
  "expiresIn": 86400000
}
```

---

### Patient Management (`/api/patients`)
*All endpoints require header: `Authorization: Bearer <token>`*

| Method | Endpoint | Description | Status |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/patients` | Register a new patient | `201 Created` |
| `GET` | `/api/patients` | Retrieve list of all patients | `200 OK` |
| `GET` | `/api/patients/{id}` | Retrieve patient by ID | `200 OK` |
| `PUT` | `/api/patients/{id}` | Update patient details | `200 OK` |
| `DELETE` | `/api/patients/{id}` | Delete patient and cascade records | `204 No Content` |

#### Example: Register Patient
```http
POST /api/patients
Content-Type: application/json
Authorization: Bearer <token>

{
  "fullName": "Michael Scott",
  "email": "michael.scott@dundermifflin.com",
  "phone": "+1-555-0144",
  "dateOfBirth": "1965-03-15",
  "gender": "Male",
  "bloodGroup": "A+",
  "address": "1725 Slough Avenue, Scranton, PA",
  "medicalNotes": "Mild hypertension, no drug allergies"
}
```

---

### Vital Statistics (`/api/patients/{patientId}/vitals` & `/api/vitals`)
*All endpoints require header: `Authorization: Bearer <token>`*

| Method | Endpoint | Description | Status |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/patients/{patientId}/vitals` | Submit vital reading (evaluates thresholds) | `201 Created` |
| `GET` | `/api/patients/{patientId}/vitals` | Retrieve vital history for a patient | `200 OK` |
| `GET` | `/api/patients/{patientId}/vitals/latest` | Retrieve latest recorded reading | `200 OK` |
| `GET` | `/api/vitals/{id}` | Retrieve single vital reading by ID | `200 OK` |

#### Example: Submit Vital Reading (Abnormal — Triggers Alerts)
```http
POST /api/patients/1/vitals
Content-Type: application/json
Authorization: Bearer <token>

{
  "heartRateBpm": 142,
  "systolicBloodPressure": 155,
  "diastolicBloodPressure": 96,
  "oxygenSaturation": 89.0,
  "temperatureCelsius": 39.2
}
```

---

### Alert Management (`/api/patients/{patientId}/alerts` & `/api/alerts`)
*All endpoints require header: `Authorization: Bearer <token>`*

| Method | Endpoint | Description | Status |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/patients/{patientId}/alerts` | Retrieve alerts for a patient | `200 OK` |
| `GET` | `/api/alerts/{id}` | Retrieve specific alert by ID | `200 OK` |
| `PATCH` | `/api/alerts/{id}/acknowledge` | Acknowledge an active alert | `200 OK` |

#### Example: Acknowledge Alert
```http
PATCH /api/alerts/1/acknowledge
Authorization: Bearer <token>
```
**Response**:
```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "Michael Scott",
  "vitalId": 2,
  "alertType": "HIGH_HEART_RATE",
  "message": "Elevated heart rate detected: 142 bpm (Demonstration threshold: max 100 bpm)",
  "severity": "CRITICAL",
  "status": "ACKNOWLEDGED",
  "createdAt": "2026-10-23T22:33:15.000",
  "acknowledgedAt": "2026-10-23T22:35:45.123"
}
```

---

### Doctor Management & Dashboard (`/api/doctors`)
*All endpoints require header: `Authorization: Bearer <token>`*

| Method | Endpoint | Description | Status |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/doctors` | Create doctor profile | `201 Created` |
| `GET` | `/api/doctors` | Retrieve all doctors | `200 OK` |
| `GET` | `/api/doctors/{id}` | Retrieve doctor by ID | `200 OK` |
| `PUT` | `/api/doctors/{doctorId}/patients/{patientId}` | Assign patient to doctor | `200 OK` |
| `GET` | `/api/doctors/{doctorId}/patients` | Retrieve doctor's assigned patients | `200 OK` |
| `GET` | `/api/doctors/{doctorId}/dashboard` | Retrieve Doctor Dashboard | `200 OK` |

#### Example: Doctor Health Dashboard
```http
GET /api/doctors/1/dashboard
Authorization: Bearer <token>
```
**Response**:
```json
{
  "doctorId": 1,
  "doctorName": "Dr. Leonard McCoy",
  "specialization": "Cardiology & Intensive Care",
  "totalPatients": 1,
  "totalActiveAlerts": 3,
  "patients": [
    {
      "patientId": 1,
      "patientName": "Michael Scott",
      "email": "michael.scott@dundermifflin.com",
      "phone": "+1-555-0144",
      "bloodGroup": "A+",
      "latestVital": {
        "id": 2,
        "patientId": 1,
        "patientName": "Michael Scott",
        "heartRateBpm": 142,
        "systolicBloodPressure": 155,
        "diastolicBloodPressure": 96,
        "oxygenSaturation": 89.0,
        "temperatureCelsius": 39.2,
        "recordedAt": "2026-10-23T22:33:15.000"
      },
      "activeAlerts": [
        {
          "id": 2,
          "patientId": 1,
          "alertType": "LOW_OXYGEN_SATURATION",
          "message": "Low oxygen saturation: 89.0% (Demonstration threshold: min 95.0%)",
          "severity": "CRITICAL",
          "status": "ACTIVE"
        }
      ]
    }
  ]
}
```

---

### Real-Time WebSocket (`/ws`)
- **Handshake Endpoint**: `ws://localhost:8080/ws`
- **Subscribed Channels**:
  - `/topic/vitals`: Broadcasts all submitted vitals in real time.
  - `/topic/alerts`: Broadcasts all generated alerts in real time.
  - `/topic/doctor/{doctorId}/vitals`: Private vital stream for a doctor's assigned patients.
  - `/topic/doctor/{doctorId}/alerts`: Private alert stream for a doctor's assigned patients.

---

## 8. Demonstration Thresholds

*(Note: Thresholds are for software demonstration only and are not diagnostic medical criteria.)*

| Vital Parameter | Normal Range | Alert Triggered | Default Severity |
| :--- | :--- | :--- | :--- |
| **Heart Rate** | 60 – 100 bpm | `< 60 bpm` or `> 100 bpm` | `MEDIUM` / `HIGH` / `CRITICAL` (&ge; 130 or &le; 45) |
| **Blood Pressure (Systolic)** | 90 – 120 mmHg | `> 120 mmHg` | `MEDIUM` / `HIGH` (&ge; 140) |
| **Blood Pressure (Diastolic)**| 60 – 80 mmHg | `> 80 mmHg` | `MEDIUM` / `HIGH` (&ge; 90) |
| **Oxygen Saturation (SpO2)**  | 95% – 100% | `< 95%` | `HIGH` / `CRITICAL` (< 90%) |
| **Body Temperature**          | 36.0°C – 37.5°C | `< 36.0°C` or `> 37.5°C` | `MEDIUM` / `HIGH` (&ge; 39.0°C) |

---

## 9. Error Response Specification

All negative tests produce uniform error payloads:
```json
{
  "timestamp": "2026-10-23T22:40:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/patients",
  "validationErrors": {
    "fullName": "Full name is required",
    "email": "Please provide a valid email address"
  }
}
```
Common status codes:
- `400 Bad Request`: Validation failure or out-of-range inputs.
- `401 Unauthorized`: Missing, invalid, or expired JWT token; bad credentials.
- `403 Forbidden`: Authenticated user lacks required role.
- `404 Not Found`: Record not found for the requested ID.
- `409 Conflict`: Duplicate unique field (e.g. email or username already taken).
