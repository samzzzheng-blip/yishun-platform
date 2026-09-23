-- Future deployment only. Back up and inspect the selected database first.
-- Not executed against any production database. Apply once to the existing workflow schema.
ALTER TABLE grading_job ADD COLUMN template_schema LONGTEXT,
 ADD COLUMN template_values LONGTEXT, ADD COLUMN label_text LONGTEXT;
CREATE TABLE grading_label_template (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, version BIGINT,
 name VARCHAR(80) NOT NULL, fields_json LONGTEXT, archived BIT NOT NULL,
 updated_by VARCHAR(255), updated_at BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE grading_batch_template (
 batch VARCHAR(100) NOT NULL PRIMARY KEY, version BIGINT, schema_json LONGTEXT,
 updated_by VARCHAR(255), updated_at BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
