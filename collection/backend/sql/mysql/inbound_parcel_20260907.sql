-- Additive migration. Rollback keeps these tables and all parcel records.
CREATE TABLE IF NOT EXISTS app_inbound_parcel (
 id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id bigint NOT NULL,
 carrier varchar(60) NOT NULL,
 tracking_no varchar(64) NOT NULL,
 remark varchar(500) NOT NULL DEFAULT '',
 status int NOT NULL DEFAULT 0 COMMENT '0 registered, 1 received, 2 completed',
 version int NOT NULL DEFAULT 0,
 create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 received_time datetime NULL,
 UNIQUE KEY uk_tracking (carrier, tracking_no),
 KEY idx_user (user_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS app_inbound_item (
 collection_id bigint NOT NULL PRIMARY KEY,
 parcel_id bigint NOT NULL,
 status int NOT NULL DEFAULT 0 COMMENT '0 pending, 1 verified, 2 exception',
 note varchar(500) NOT NULL DEFAULT '',
 evidence varchar(2000) NOT NULL DEFAULT '',
 KEY idx_parcel (parcel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS app_inbound_event (
 id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
 parcel_id bigint NOT NULL,
 actor_id bigint NOT NULL,
 actor_type varchar(10) NOT NULL,
 detail text NOT NULL,
 create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 KEY idx_inbound_event_parcel (parcel_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
