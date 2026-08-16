package com.heigraduate.app.graduate.contract;

import java.util.List;
import java.util.UUID;

public interface CourseRequirementQuery {

  List<UUID> getMandatoryCourseIds(UUID parcoursId, UUID academicYearId);
}
