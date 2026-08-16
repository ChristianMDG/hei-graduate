-- V42_15 renamed course_track.mandatory to obligatory, but the Java code
-- (CourseTrack entity, DTOs, mapper, repository derived query
-- findByTrackIdAndAcademicYearIdAndMandatoryTrue, service, and the
-- CourseRequirementQuery contract) was never updated to match.
-- This broke Hibernate schema validation on every cold start (production outage).
-- Reverting the column name to match the code, since V42_15 is already applied
-- and must not be edited.

ALTER TABLE course_track RENAME COLUMN obligatory TO mandatory;
