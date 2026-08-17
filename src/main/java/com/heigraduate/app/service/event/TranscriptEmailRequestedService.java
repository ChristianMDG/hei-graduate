package com.heigraduate.app.service.event;

import static java.io.File.createTempFile;

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
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranscriptEmailRequestedService implements Consumer<TranscriptEmailRequested> {

  private static final Duration LINK_VALIDITY = Duration.ofDays(7);

  private final TranscriptService transcriptService;
  private final StudentRepository studentRepository;
  private final UserRepository userRepository;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(TranscriptEmailRequested event) {
    Student student =
        studentRepository
            .findById(event.getStudentId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Student not found with id: " + event.getStudentId()));

    User user =
        userRepository
            .findById(student.getUserId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "User not found with id: " + student.getUserId()));

    byte[] pdf =
        transcriptService.generateTranscript(event.getStudentId(), event.getAcademicYearId());

    String bucketKey =
        "releves/" + student.getStudentNumber() + "-" + Instant.now().toEpochMilli() + ".pdf";
    File tempFile = createTempFile("releve-", ".pdf");
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write(pdf);
    }
    bucketComponent.upload(tempFile, bucketKey);

    var downloadUri = bucketComponent.presign(bucketKey, LINK_VALIDITY);

    var recipient = new InternetAddress(user.getEmail());
    var email =
        new Email(
            recipient,
            List.of(),
            List.of(),
            "Votre relevé de notes est disponible",
            "Bonjour "
                + student.getFirstName()
                + ",\n\nVotre relevé de notes est disponible via le lien suivant "
                + "(valable 7 jours) :\n"
                + downloadUri
                + "\n\nCordialement,\nHEI Graduate",
            List.of());
    mailer.accept(email);
  }
}
