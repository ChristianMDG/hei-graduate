package com.heigraduate.app.UT.graduate.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heigraduate.app.IT.AbstractIntegrationTest;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.model.UserRole;
import com.heigraduate.app.graduate.repository.StudentRepository;
import com.heigraduate.app.graduate.repository.UserRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

class StudentControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private StudentRepository studentRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private static final String RAW_PASSWORD = "P@ssw0rd123!";

  private Student otherStudent;
  private String adminToken;
  private String studentToken;

  @BeforeEach
  void setUp() throws Exception {
    User adminUser = persistUser("admin+" + UUID.randomUUID() + "@hei.mg", UserRole.ADMIN);
    adminToken = login(adminUser.getEmail());

    User ownUser = persistUser("me+" + UUID.randomUUID() + "@hei.mg", UserRole.STUDENT);
    studentToken = login(ownUser.getEmail());

    User otherUser = persistUser("autre+" + UUID.randomUUID() + "@hei.mg", UserRole.STUDENT);
    otherStudent =
        studentRepository.save(
            Student.builder()
                .userId(otherUser.getId())
                .studentNumber("STD" + System.nanoTime() % 100000)
                .lastName("Rakoto")
                .firstName("Voahangy")
                .enrollmentDate(LocalDate.of(2023, 9, 1))
                .status("ACTIVE")
                .build());
  }

  private User persistUser(String email, UserRole role) {
    return userRepository.save(
        User.builder()
            .email(email)
            .password(passwordEncoder.encode(RAW_PASSWORD))
            .role(role)
            .active(true)
            .build());
  }

  private String login(String email) throws Exception {
    // LoginRequest is a record (email, password) — build the JSON body to match that shape.
    String payload = "{\"email\":\"" + email + "\",\"password\":\"" + RAW_PASSWORD + "\"}";

    String response =
        mockMvc
            .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode json = objectMapper.readTree(response);
    return json.get("token").asText();
  }

  @Test
  void getById_shouldReturn403_whenCalledWithStudentToken_evenForAnotherStudent() throws Exception {
    mockMvc
        .perform(
            get("/api/students/{id}", otherStudent.getId())
                .header("Authorization", "Bearer " + studentToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void getById_shouldReturn200_whenCalledWithAdminToken() throws Exception {
    mockMvc
        .perform(
            get("/api/students/{id}", otherStudent.getId())
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk());
  }
}
