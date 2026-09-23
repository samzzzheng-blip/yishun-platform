-- Review and back up the chosen database before a FUTURE deployment.
-- This migration has NOT been run on any production database.
CREATE TABLE IF NOT EXISTS grading_job (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 version BIGINT,
 job_number VARCHAR(48) NOT NULL UNIQUE,
 stage VARCHAR(32) NOT NULL,
 batch VARCHAR(100), card_name VARCHAR(100), series VARCHAR(100), card_year VARCHAR(20),
 language VARCHAR(40), card_number VARCHAR(100), cert_number VARCHAR(100), rate_id BIGINT UNIQUE,
 front_photo VARCHAR(200), back_photo VARCHAR(200), finished_photo VARCHAR(200),
 surface VARCHAR(10), center VARCHAR(10), edge VARCHAR(10), corner VARCHAR(10), score VARCHAR(10),
 notes VARCHAR(1000), created_by VARCHAR(100), graded_by VARCHAR(100), confirmed_by VARCHAR(100),
 created_at BIGINT NOT NULL, updated_at BIGINT NOT NULL,
 INDEX idx_grading_status(stage), INDEX idx_grading_batch(batch)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS grading_event (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 job_id BIGINT, actor VARCHAR(100), action VARCHAR(40), stage VARCHAR(32), created_at BIGINT NOT NULL,
 INDEX idx_grading_event_job(job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
