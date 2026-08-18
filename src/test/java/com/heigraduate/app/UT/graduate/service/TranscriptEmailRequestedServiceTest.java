package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.heigraduate.app.endpoint.event.model.TranscriptEmailRequested;
import com.heigraduate.app.file.bucket.BucketComponent;
import com.heigraduate.app.graduate.dto.AnnualAverageResult;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.model.Transcript;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.StudentRepository;
import com.heigraduate.app.graduate.repository.TranscriptRepository;
import com.heigraduate.app.graduate.repository.UserRepository;
import com.heigraduate.app.graduate.service.AcademicAverageService;
import com.heigraduate.app.graduate.service.TranscriptService;
import com.heigraduate.app.mail.Email;
import com.heigraduate.app.mail.Mailer;
import com.heigraduate.app.service.event.TranscriptEmailRequestedService;
import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
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
  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private AcademicAverageService academicAverageService;
  @Mock private TranscriptRepository transcriptRepository;
  @Mock private BucketComponent bucketComponent;
  @Mock private Mailer mailer;

  @InjectMocks private TranscriptEmailRequestedService consumer;

  private Student student;
  private User user;
  private AcademicYear academicYear;
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

    academicYear =
        AcademicYear.builder()
            .id(academicYearId)
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 6, 30))
            .level("L2")
            .build();

    event =
        TranscriptEmailRequested.builder()
            .studentId(studentId)
            .academicYearId(academicYearId)
            .build();
  }

  @Test
  void accept_shouldGeneratePdf_uploadToS3_persistTranscript_thenSendEmailWithPresignedLink()
      throws Exception {
    byte[] fakePdf = "%PDF-fake-content".getBytes();
    URI presignedUri = URI.create("https://bucket.example.com/releves/signed-link");
    AnnualAverageResult summary =
        new AnnualAverageResult(
            student.getId(),
            academicYear.getId(),
            new BigDecimal("13.56"),
            9,
            9,
            List.of(UUID.randomUUID()),
            List.of(),
            List.of());

    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(academicYearRepository.findById(academicYear.getId()))
        .thenReturn(Optional.of(academicYear));
    when(transcriptService.generateTranscript(event.getStudentId(), event.getAcademicYearId()))
        .thenReturn(fakePdf);
    when(academicAverageService.computeAnnualAverage(
            event.getStudentId(), event.getAcademicYearId()))
        .thenReturn(summary);
    when(bucketComponent.presign(anyString(), any(Duration.class)))
        .thenReturn(presignedUri.toURL());
    when(transcriptRepository.save(any(Transcript.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    consumer.accept(event);

    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    verify(bucketComponent).upload(any(File.class), keyCaptor.capture());
    assertThat(keyCaptor.getValue()).contains("STD24049");
    assertThat(keyCaptor.getValue()).startsWith("releves/");

    verify(bucketComponent).presign(keyCaptor.getValue(), Duration.ofDays(7));

    ArgumentCaptor<Transcript> transcriptCaptor = ArgumentCaptor.forClass(Transcript.class);
    verify(transcriptRepository).save(transcriptCaptor.capture());
    Transcript saved = transcriptCaptor.getValue();
    assertThat(saved.getStudent()).isEqualTo(student);
    assertThat(saved.getAcademicYear()).isEqualTo(academicYear);
    assertThat(saved.getSemester()).isNull();
    assertThat(saved.getType()).isEqualTo("COMPLET");
    assertThat(saved.getUrlS3()).isEqualTo(keyCaptor.getValue());
    assertThat(saved.getAverageGrade()).isEqualByComparingTo("13.56");
    assertThat(saved.getObtainedCredits()).isEqualTo(9);
    assertThat(saved.getExpectedCredits()).isEqualTo(9);
    assertThat(saved.getYearValidated()).isTrue();
    assertThat(saved.getEmailSent()).isTrue();
    assertThat(saved.getEmailSentAt()).isNotNull();

    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());

    Email sentEmail = emailCaptor.getValue();
    assertThat(sentEmail.to().getAddress()).isEqualTo("nomena@heigraduate.mg");
    assertThat(sentEmail.htmlBody()).contains(presignedUri.toString());
    assertThat(sentEmail.htmlBody()).contains("Nomena");
  }

  @Test
  void accept_shouldMarkProvisoire_whenAGradeIsMissing() throws Exception {
    AnnualAverageResult summary =
        new AnnualAverageResult(
            student.getId(),
            academicYear.getId(),
            new BigDecimal("14.50"),
            5,
            9,
            List.of(UUID.randomUUID()),
            List.of(),
            List.of(UUID.randomUUID()));

    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(academicYearRepository.findById(academicYear.getId()))
        .thenReturn(Optional.of(academicYear));
    when(transcriptService.generateTranscript(event.getStudentId(), event.getAcademicYearId()))
        .thenReturn("%PDF-fake".getBytes());
    when(academicAverageService.computeAnnualAverage(
            event.getStudentId(), event.getAcademicYearId()))
        .thenReturn(summary);
    when(bucketComponent.presign(anyString(), any(Duration.class)))
        .thenReturn(URI.create("https://bucket.example.com/x").toURL());
    when(transcriptRepository.save(any(Transcript.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    consumer.accept(event);

    ArgumentCaptor<Transcript> transcriptCaptor = ArgumentCaptor.forClass(Transcript.class);
    verify(transcriptRepository).save(transcriptCaptor.capture());
    assertThat(transcriptCaptor.getValue().getType()).isEqualTo("PROVISOIRE");
    assertThat(transcriptCaptor.getValue().getYearValidated()).isFalse();
  }

  @Test
  void accept_shouldThrow_whenStudentNotFound() {
    when(studentRepository.findById(event.getStudentId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> consumer.accept(event)).isInstanceOf(ResourceNotFoundException.class);

    verify(mailer, never()).accept(any());
    verify(bucketComponent, never()).upload(any(), anyString());
    verify(transcriptRepository, never()).save(any());
  }

  @Test
  void accept_shouldThrow_whenUserNotFound() {
    when(studentRepository.findById(event.getStudentId())).thenReturn(Optional.of(student));
    when(userRepository.findById(student.getUserId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> consumer.accept(event)).isInstanceOf(ResourceNotFoundException.class);

    verify(mailer, never()).accept(any());
    verify(transcriptRepository, never()).save(any());
  }

  @Test
  void accept_shouldThrow_whenAcademicYearNotFound() {
    when(studentRepository.findById(event.getStudentId())).thenReturn(Optional.of(student));
    when(userRepository.findById(student.getUserId())).thenReturn(Optional.of(user));
    when(academicYearRepository.findById(event.getAcademicYearId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> consumer.accept(event)).isInstanceOf(ResourceNotFoundException.class);

    verify(mailer, never()).accept(any());
    verify(bucketComponent, never()).upload(any(), anyString());
    verify(transcriptRepository, never()).save(any());
  }
}
