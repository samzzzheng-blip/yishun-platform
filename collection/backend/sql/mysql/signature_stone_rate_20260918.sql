-- Public signature-card category: 5 stones per stock unit. Previous rate was NULL (default 1).
UPDATE app_category SET exchange_rate = 5 WHERE id = 680 AND user_id = 0 AND name = '签名卡砖' AND deleted = 0;
