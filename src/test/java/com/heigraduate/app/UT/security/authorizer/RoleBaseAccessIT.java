package com.heigraduate.app.UT.security.authorizer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heigraduate.app.IT.AbstractIntegrationTest;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.model.UserRole;
import com.heigraduate.app.graduate.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

class RoleBaseAccessIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private static final String RAW_PASSWORD = "P@ssw0rd123!";

  private String tokenFor(UserRole role) throws Exception {
    String email = role.name().toLowerCase() + "+" + UUID.randomUUID() + "@hei.mg";
    userRepository.save(
        User.builder()
            .email(email)
            .password(passwordEncoder.encode(RAW_PASSWORD))
            .role(role)
            .active(true)
            .build());

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
  void listUsers_shouldReturn403_forTeacherToken() throws Exception {
    String teacherToken = tokenFor(UserRole.TEACHER);

    mockMvc
        .perform(get("/api/users").header("Authorization", "Bearer " + teacherToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void listGrades_shouldReturn403_forStudentToken() throws Exception {
    String studentToken = tokenFor(UserRole.STUDENT);

    mockMvc
        .perform(get("/api/grades").header("Authorization", "Bearer " + studentToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void listUsers_shouldReturn200_forAdminToken() throws Exception {
    String adminToken = tokenFor(UserRole.ADMIN);

    mockMvc
        .perform(get("/api/users").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk());
  }
}
