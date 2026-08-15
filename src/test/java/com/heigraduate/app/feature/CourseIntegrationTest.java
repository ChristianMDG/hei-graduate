package com.heigraduate.app.feature;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heigraduate.app.graduate.dto.CourseRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CourseIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("aws.eventBridge.bus", () -> "test-bus");
    registry.add("aws.s3.bucket", () -> "test-bucket");
  }

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser(roles = "ADMINISTRATOR")
  void should_create_then_find_then_update_then_deactivate_a_course() throws Exception {
    CourseRequest createRequest = new CourseRequest("PROG4", "Advanced Programming", 5);

    String response =
        mockMvc
            .perform(
                post("/api/courses")
                    .with(csrf())
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.courseReference").value("PROG4"))
            .andExpect(jsonPath("$.active").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String id = objectMapper.readTree(response).get("id").asText();

    mockMvc
        .perform(get("/api/courses/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Advanced Programming"));

    CourseRequest updateRequest = new CourseRequest("PROG4", "Advanced Programming II", 6);

    mockMvc
        .perform(
            put("/api/courses/" + id)
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Advanced Programming II"))
        .andExpect(jsonPath("$.credits").value(6));

    mockMvc.perform(delete("/api/courses/" + id).with(csrf())).andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/courses/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));
  }

  @Test
  @WithMockUser(roles = "ADMINISTRATOR")
  void should_reject_duplicate_course_reference() throws Exception {
    CourseRequest request = new CourseRequest("PROG5", "Databases", 5);

    mockMvc
        .perform(
            post("/api/courses")
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/courses")
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "STUDENT")
  void should_forbid_non_administrator_from_creating_a_course() throws Exception {
    CourseRequest request = new CourseRequest("PROG6", "Networks", 4);

    mockMvc
        .perform(
            post("/api/courses")
                .with(csrf())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_reject_anonymous_access() throws Exception {
    mockMvc.perform(get("/api/courses")).andExpect(status().isUnauthorized());
  }
}
