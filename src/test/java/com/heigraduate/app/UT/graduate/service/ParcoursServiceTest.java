package com.heigraduate.app.UT.graduate.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.heigraduate.app.graduate.dto.ParcoursRequest;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.Parcours;
import com.heigraduate.app.graduate.repository.ParcoursRepository;
import com.heigraduate.app.graduate.service.ParcoursService;
import com.heigraduate.app.graduate.validator.ParcoursValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParcoursServiceTest {

    @Mock private ParcoursRepository parcoursRepository;


    private ParcoursValidator parcoursValidator;

    @InjectMocks private ParcoursService parcoursService;

    @BeforeEach
    void setUp() {
        parcoursValidator = new ParcoursValidator(parcoursRepository);
        parcoursService = new ParcoursService(parcoursRepository, parcoursValidator);
    }

    @Test
    void create_shouldPersistParcoursWithUppercaseCodeAndActiveTrue() {
        ParcoursRequest request = new ParcoursRequest("el", "Ecosysteme Logiciel");
        when(parcoursRepository.findByCodeIgnoreCase("el")).thenReturn(Optional.empty());
        when(parcoursRepository.save(any(Parcours.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Parcours result = parcoursService.create(request);

        assertThat(result.getCode()).isEqualTo("EL");
        assertThat(result.getLabel()).isEqualTo("Ecosysteme Logiciel");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void create_shouldRejectDuplicateCode() {
        ParcoursRequest request = new ParcoursRequest("EL", "Ecosysteme Logiciel");
        Parcours existing = Parcours.builder().id(UUID.randomUUID()).code("EL").active(true).build();
        when(parcoursRepository.findByCodeIgnoreCase("EL")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> parcoursService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(parcoursRepository, times(0)).save(any());
    }

    @Test
    void update_shouldAllowKeepingItsOwnCode() {
        UUID id = UUID.randomUUID();
        Parcours existing = Parcours.builder().id(id).code("EL").label("Old label").active(true).build();
        when(parcoursRepository.findById(id)).thenReturn(Optional.of(existing));
        when(parcoursRepository.findByCodeIgnoreCase("EL")).thenReturn(Optional.of(existing));
        when(parcoursRepository.save(any(Parcours.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Parcours result = parcoursService.update(id, new ParcoursRequest("EL", "New label"));

        assertThat(result.getLabel()).isEqualTo("New label");
    }

    @Test
    void update_shouldRejectCodeAlreadyUsedByAnotherParcours() {
        UUID id = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        Parcours existing = Parcours.builder().id(id).code("EL").active(true).build();
        Parcours other = Parcours.builder().id(otherId).code("TN").active(true).build();
        when(parcoursRepository.findById(id)).thenReturn(Optional.of(existing));
        when(parcoursRepository.findByCodeIgnoreCase("TN")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> parcoursService.update(id, new ParcoursRequest("TN", "Transformation Numerique")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(parcoursRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parcoursService.getById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAll_shouldFilterOnActiveOnlyFlag() {
        Parcours active = Parcours.builder().id(UUID.randomUUID()).code("EL").active(true).build();
        when(parcoursRepository.findByActiveTrue()).thenReturn(List.of(active));

        List<Parcours> result = parcoursService.getAll(true);

        assertThat(result).containsExactly(active);
        verify(parcoursRepository, times(0)).findAll();
    }

    @Test
    void deactivate_shouldSetActiveFalse() {
        UUID id = UUID.randomUUID();
        Parcours existing = Parcours.builder().id(id).code("EL").active(true).build();
        when(parcoursRepository.findById(id)).thenReturn(Optional.of(existing));
        when(parcoursRepository.save(any(Parcours.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Parcours result = parcoursService.deactivate(id);

        assertThat(result.isActive()).isFalse();
    }
}