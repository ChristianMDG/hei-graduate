package com.heigraduate.app.IT.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.heigraduate.app.IT.AbstractIntegrationTest;
import com.heigraduate.app.graduate.dto.ParcoursRequest;
import com.heigraduate.app.graduate.dto.ParcoursResponse;
import com.heigraduate.app.graduate.repository.ParcoursRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ParcoursControllerIT extends AbstractIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private ParcoursRepository parcoursRepository;

  @AfterEach
  void cleanUp() {
    parcoursRepository.deleteAll();
  }

  @Test
  void shouldCreateThenRetrieveParcours() {
    ParcoursRequest request = new ParcoursRequest("EL", "Ecosysteme Logiciel");

    ResponseEntity<ParcoursResponse> createResponse =
        restTemplate.postForEntity("/api/parcours", request, ParcoursResponse.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    ParcoursResponse created = createResponse.getBody();
    assertThat(created).isNotNull();
    assertThat(created.code()).isEqualTo("EL");
    assertThat(created.active()).isTrue();

    ResponseEntity<ParcoursResponse> getResponse =
        restTemplate.getForEntity("/api/parcours/" + created.id(), ParcoursResponse.class);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().code()).isEqualTo("EL");
  }

  @Test
  void shouldRejectDuplicateCodeWithConflict() {
    restTemplate.postForEntity(
        "/api/parcours",
        new ParcoursRequest("TN", "Transformation Numerique"),
        ParcoursResponse.class);

    ResponseEntity<String> secondAttempt =
        restTemplate.postForEntity(
            "/api/parcours", new ParcoursRequest("tn", "Doublon"), String.class);

    assertThat(secondAttempt.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void shouldRejectBlankCodeWithBadRequest() {
    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/api/parcours", new ParcoursRequest("", "Sans code"), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldReturnNotFoundForUnknownId() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/api/parcours/" + UUID.randomUUID(), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldDeactivateThenExcludeFromActiveOnlyListing() {
    ResponseEntity<ParcoursResponse> created =
        restTemplate.postForEntity(
            "/api/parcours",
            new ParcoursRequest("EL", "Ecosysteme Logiciel"),
            ParcoursResponse.class);
    UUID id = created.getBody().id();

    restTemplate.exchange(
        "/api/parcours/" + id + "/deactivate",
        HttpMethod.PATCH,
        new HttpEntity<>(null),
        ParcoursResponse.class);

    ResponseEntity<List<ParcoursResponse>> activeOnly =
        restTemplate.exchange(
            "/api/parcours?activeOnly=true",
            HttpMethod.GET,
            null,
            new org.springframework.core.ParameterizedTypeReference<List<ParcoursResponse>>() {});

    assertThat(activeOnly.getBody()).extracting(ParcoursResponse::id).doesNotContain(id);
  }
}
