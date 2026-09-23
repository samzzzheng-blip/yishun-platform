-- Local implementation only; review/back up before future production deployment.
ALTER TABLE grading_batch_sequence ADD COLUMN start_number VARCHAR(18) NULL;
-- Legacy stages are displayed as COLLECTING (unpublished) or DONE (already formal).
-- Historical certificate numbers are not rewritten automatically.
