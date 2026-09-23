-- Review and back up before any production deployment. Not executed against production.
-- Existing records remain unnumbered; existing certificate numbers are not changed.
ALTER TABLE grading_job ADD COLUMN batch_number BIGINT NULL;
CREATE UNIQUE INDEX uk_grading_batch_number ON grading_job (batch, batch_number);
CREATE TABLE grading_batch_sequence (
  batch VARCHAR(100) NOT NULL PRIMARY KEY,
  last_number BIGINT NOT NULL
);
-- Batch comparison/collation must match grading_job.batch.
