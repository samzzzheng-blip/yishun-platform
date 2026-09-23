-- 执行前检查列是否已存在；每个环境仅执行一次。
ALTER TABLE app_ads ADD COLUMN target_product_id BIGINT NULL COMMENT '广告跳转的一口价商品编号';
-- 保留旧小程序中广告 8 的既有跳转配置。
UPDATE app_ads SET target_product_id = 672 WHERE id = 8 AND target_product_id IS NULL
  AND EXISTS (SELECT 1 FROM app_yikoujia WHERE id = 672 AND deleted = 0);
