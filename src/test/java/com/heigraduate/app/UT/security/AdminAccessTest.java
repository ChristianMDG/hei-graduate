package com.heigraduate.app.UT.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.heigraduate.app.graduate.controller.GradeController;
import com.heigraduate.app.graduate.controller.StudentController;
import com.heigraduate.app.graduate.controller.TeacherController;
import com.heigraduate.app.graduate.controller.TranscriptController;
import com.heigraduate.app.graduate.model.User;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class AdminAccessTest {

  @Test
  void gradeController_findMyPublishedGrades_allowsAdmin() throws Exception {
    Method method = GradeController.class.getMethod("findMyPublishedGrades", User.class);
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
    assertThat(annotation).isNotNull();
    assertThat(annotation.value()).contains("ADMIN");
  }

  @Test
  void transcriptController_generateMine_allowsAdmin() throws Exception {
    Method method = TranscriptController.class.getMethod("generateMine", User.class);
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
    assertThat(annotation).isNotNull();
    assertThat(annotation.value()).contains("ADMIN");
  }

  @Test
  void transcriptController_sendMineByEmail_allowsAdmin() throws Exception {
    Method method = TranscriptController.class.getMethod("sendMineByEmail", User.class, UUID.class);
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
    assertThat(annotation).isNotNull();
    assertThat(annotation.value()).contains("ADMIN");
  }

  @Test
  void studentController_getCurrentStudent_allowsAdmin() throws Exception {
    Method method = StudentController.class.getMethod("getCurrentStudent", User.class);
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
    assertThat(annotation).isNotNull();
    assertThat(annotation.value()).contains("ADMIN");
  }

  @Test
  void teacherController_getCurrentTeacher_allowsAdmin() throws Exception {
    Method method = TeacherController.class.getMethod("getCurrentTeacher", User.class);
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
    assertThat(annotation).isNotNull();
    assertThat(annotation.value()).contains("ADMIN");
  }
}
