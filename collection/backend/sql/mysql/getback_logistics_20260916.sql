-- Execute once before deploying the new backend. Existing data is preserved.
ALTER TABLE app_getback ADD COLUMN express_company VARCHAR(32) NULL COMMENT '快递100公司编码';
