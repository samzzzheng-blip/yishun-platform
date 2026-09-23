CREATE TABLE IF NOT EXISTS app_workbench_read (
  user_id BIGINT NOT NULL,
  queue_key VARCHAR(64) NOT NULL,
  item_token CHAR(64) NOT NULL,
  read_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, queue_key, item_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台待办已读记录，不改变业务状态';
