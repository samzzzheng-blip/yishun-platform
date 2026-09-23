-- Production schema only. No INSERT/UPDATE/DELETE, accounts, templates or sample records.
-- Run only after confirming no grading tables exist and taking a full backup.
CREATE TABLE grading_job (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, version BIGINT,
 job_number VARCHAR(48) NOT NULL UNIQUE, stage VARCHAR(32) NOT NULL,
 batch VARCHAR(100), batch_number BIGINT, deleted_at BIGINT,
 card_name VARCHAR(100), series VARCHAR(100), card_year VARCHAR(20), language VARCHAR(40),
 card_number VARCHAR(100), cert_number VARCHAR(100), rate_id BIGINT UNIQUE,
 front_photo VARCHAR(200), back_photo VARCHAR(200), finished_photo VARCHAR(200),
 surface VARCHAR(10), center VARCHAR(10), edge VARCHAR(10), corner VARCHAR(10), score VARCHAR(10),
 notes VARCHAR(1000), created_by VARCHAR(100), graded_by VARCHAR(100), confirmed_by VARCHAR(100),
 template_schema LONGTEXT, template_values LONGTEXT, label_text LONGTEXT,
 created_at BIGINT NOT NULL, updated_at BIGINT NOT NULL,
 UNIQUE KEY uk_grading_batch_number(batch,batch_number),
 INDEX idx_grading_status(stage), INDEX idx_grading_batch(batch)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE grading_event (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, job_id BIGINT, actor VARCHAR(100),
 action VARCHAR(40), stage VARCHAR(32), created_at BIGINT NOT NULL,
 INDEX idx_grading_event_job(job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE grading_label_template (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, version BIGINT,
 name VARCHAR(80) NOT NULL, fields_json LONGTEXT, archived BIT NOT NULL,
 updated_by VARCHAR(255), updated_at BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE grading_batch_template (
 batch VARCHAR(100) PRIMARY KEY, version BIGINT, schema_json LONGTEXT,
 updated_by VARCHAR(255), updated_at BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE grading_batch_sequence (
 batch VARCHAR(100) PRIMARY KEY, last_number BIGINT NOT NULL,
 created_by VARCHAR(100), created_at BIGINT NOT NULL,
 request_id VARCHAR(100) UNIQUE, start_number VARCHAR(18)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE grading_batch_day (
 day_actor VARCHAR(120) PRIMARY KEY, last_number BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- Preserve all rows and columns while enabling atomic publication/rollback.
ALTER TABLE rate ENGINE=InnoDB;
