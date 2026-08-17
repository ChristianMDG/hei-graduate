package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.heigraduate.app.endpoint.event.model.TranscriptEmailRequested;
import com.heigraduate.app.file.bucket.BucketComponent;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.repository.StudentRepository;
import com.heigraduate.app.graduate.repository.UserRepository;
import com.heigraduate.app.graduate.service.TranscriptService;
import com.heigraduate.app.mail.Email;
import com.heigraduate.app.mail.Mailer;
import com.heigraduate.app.service.event.TranscriptEmailRequestedService;
import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranscriptEmailRequestedServiceTest {

  @Mock private TranscriptService transcriptService;
  @Mock private StudentRepository studentRepository;
  @Mock private UserRepository userRepository;
  @Mock private BucketComponent bucketComponent;
  @Mock private Mailer mailer;

  @InjectMocks private TranscriptEmailRequestedService consumer;

  private Student student;
  private User user;
  private TranscriptEmailRequested event;

  @BeforeEach
  void setUp() {
    UUID studentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID academicYearId = UUID.randomUUID();

    student =
        Student.builder()
            .id(studentId)
            .userId(userId)
            .studentNumber("STD24049")
            .lastName("Lahatra")
            .firstName("Nomena")
            .enrollmentDate(LocalDate.of(2023, 9, 1))
            .status("ACTIVE")
            .build();

    user = User.builder().id(userId).email("nomena@heigraduate.mg").build();

    event =
        TranscriptEmailRequested.builder()
            .studentId(studentId)
            .academicYearId(academicYearId)
            .build();
  }

  @Test
  void accept_shouldGeneratePdf_uploadToS3_thenSendEmailWithPresignedLink() throws Exception {
    byte[] fakePdf = "%PDF-fake-content".getBytes();
    URI presignedUri = URI.create("https://bucket.example.com/releves/signed-link");

    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(transcriptService.generateTranscript(event.getStudentId(), event.getAcademicYearId()))
        .thenReturn(fakePdf);
    when(bucketComponent.presign(anyString(), any(Duration.class)))
        .thenReturn(presignedUri.toURL());

    consumer.accept(event);

    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    verify(bucketComponent).upload(any(File.class), keyCaptor.capture());
    assertThat(keyCaptor.getValue()).contains("STD24049");
    assertThat(keyCaptor.getValue()).startsWith("releves/");

    verify(bucketComponent).presign(keyCaptor.getValue(), Duration.ofDays(7));

    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());

    Email sentEmail = emailCaptor.getValue();
    assertThat(sentEmail.to().getAddress()).isEqualTo("nomena@heigraduate.mg");
    assertThat(sentEmail.htmlBody()).contains(presignedUri.toString());
    assertThat(sentEmail.htmlBody()).contains("Nomena");
  }

  @Test
  void accept_shouldThrow_whenStudentNotFound() {
    when(studentRepository.findById(event.getStudentId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> consumer.accept(event)).isInstanceOf(ResourceNotFoundException.class);

    verify(mailer, never()).accept(any());
    verify(bucketComponent, never()).upload(any(), anyString());
  }

  @Test
  void accept_shouldThrow_whenUserNotFound() {
    when(studentRepository.findById(event.getStudentId())).thenReturn(Optional.of(student));
    when(userRepository.findById(student.getUserId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> consumer.accept(event)).isInstanceOf(ResourceNotFoundException.class);

    verify(mailer, never()).accept(any());
  }
}
