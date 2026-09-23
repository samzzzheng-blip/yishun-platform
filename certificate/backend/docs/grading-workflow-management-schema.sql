-- Reference migration only. Not executed against production.
-- Back up the database; apply once after checking existing columns/indexes.
ALTER TABLE grading_job ADD COLUMN deleted_at BIGINT NULL;
ALTER TABLE grading_batch_sequence ADD COLUMN created_by VARCHAR(100) NULL;
ALTER TABLE grading_batch_sequence ADD COLUMN created_at BIGINT NOT NULL DEFAULT 0;
ALTER TABLE grading_batch_sequence ADD COLUMN request_id VARCHAR(100) NULL;
CREATE UNIQUE INDEX uk_grading_batch_request ON grading_batch_sequence(request_id);
CREATE TABLE grading_batch_day (
  day_actor VARCHAR(120) NOT NULL PRIMARY KEY,
  last_number BIGINT NOT NULL DEFAULT 0
);
-- Deleted workflow jobs retain their Rate reference, events and photos.
-- Recovery requires explicit approval and an exact job ID; clear deleted_at,
-- increment version and update updated_at. Never recreate the formal Rate.
