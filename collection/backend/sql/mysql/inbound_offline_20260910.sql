-- Run once before deploying the new backend. Existing records remain courier.
-- Keep these additive changes when rolling back application code. Do not delete offline records.
ALTER TABLE app_inbound_parcel ADD COLUMN delivery_method varchar(16) NOT NULL DEFAULT 'courier';
ALTER TABLE app_inbound_parcel MODIFY COLUMN tracking_no varchar(64) NULL;
