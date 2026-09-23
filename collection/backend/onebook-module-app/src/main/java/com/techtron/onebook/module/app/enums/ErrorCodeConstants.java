package com.techtron.onebook.module.app.enums;
import com.techtron.onebook.framework.common.exception.ErrorCode;
/**
 * App 错误码枚举类
 *
 * app 系统，使用 2-001-000-000 段
 */
public interface ErrorCodeConstants {
    // 藏品
    ErrorCode COLLECTION_NOT_EXISTS = new ErrorCode(2_001_000_000, "藏品登记不存在");
    ErrorCode USER_NOT_EXISTS = new ErrorCode(2_001_000_001, "账号不存在");
    // 藏品分类
    ErrorCode COLLECTION_CATEGORY_NOT_EXISTS = new ErrorCode(2_001_001_000, "藏品分类不存在");
    ErrorCode COLLECTION_CATEGORY_CANT_DELETE = new ErrorCode(2_001_001_001, "该分类还有藏品，不能删除");
    // 一口价订单
    ErrorCode YIKOUJIA_NOT_EXISTS = new ErrorCode(2_001_002_000, "订单状态有误，操作失败");
    ErrorCode GOLD_FISH_ERROR = new ErrorCode(2_001_002_001, "闲鱼定时任务失败");
    ErrorCode CREATE_PRODUCT_ERROR = new ErrorCode(2_001_002_002, "创建商品失败");
    ErrorCode UP_PRODUCT_ERROR = new ErrorCode(2_001_002_011, "闲鱼商品上架失败，请核对闲鱼商品状态后重试");
    ErrorCode GOOFISH_PRODUCT_ID_INVALID = new ErrorCode(2_001_002_003, "无法从链接中识别商品ID，请输入闲管家product_id");
    ErrorCode GOOFISH_PRODUCT_QUERY_ERROR = new ErrorCode(2_001_002_004, "读取闲鱼商品失败，请确认店铺已授权且商品ID正确");
    ErrorCode GOOFISH_PRODUCT_ALREADY_IMPORTED = new ErrorCode(2_001_002_005, "该闲鱼商品已导入，请勿重复操作");
    ErrorCode GOOFISH_MEMBER_REQUIRED = new ErrorCode(2_001_002_006, "非自营商品必须选择所属会员");
    ErrorCode GOOFISH_PRODUCT_NOT_AUTHORIZED = new ErrorCode(2_001_002_007, "未在当前授权闲鱼店铺中找到该商品");
    ErrorCode YIKOUJIA_DELIST_FORBIDDEN = new ErrorCode(2_001_002_008, "无权下架该商品");
    ErrorCode YIKOUJIA_DELIST_STATUS_INVALID = new ErrorCode(2_001_002_009, "该商品当前状态不能下架");
    ErrorCode YIKOUJIA_DELIST_ORDER_PENDING = new ErrorCode(2_001_002_010, "该商品存在待处理的付款订单，暂时不能下架");
    // 快速变现
    ErrorCode FAST_TRADE_NOT_EXISTS = new ErrorCode(2_001_003_000, "快速变现订单不存在");
    // 批量交易
    ErrorCode BATCH_TRADE_NOT_EXISTS = new ErrorCode(2_001_004_000, "批量交易订单不存在");
    ErrorCode SELL_NO_COLLECTION = new ErrorCode(2_001_004_001, "该分类下没有藏品");
    ErrorCode SELL_NO_ENOUGH_COLLECTION = new ErrorCode(2_001_004_002, "能量石数量不足");
    ErrorCode ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR = new ErrorCode(2_001_004_003, "交易订单更新支付状态失败，支付单编号不匹配");
    ErrorCode ORDER_NOT_FOUND = new ErrorCode(2_001_004_004, "交易订单不存在");
    ErrorCode ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS = new ErrorCode(2_001_004_005, "交易订单更新支付状态失败，支付单状态不是【支付成功】状态");
    ErrorCode ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH = new ErrorCode(2_001_004_006, "交易订单更新支付状态失败，支付单金额不匹配");
    ErrorCode AMOUNT_NOT_TEN = new ErrorCode(2_001_004_007, "交易数量必须是10的倍数");
    ErrorCode DEAL_ORDER_NOT_EXISTS  = new ErrorCode(2_001_004_008, "成交记录不存在");
    ErrorCode ORDER_NOT_EXISTS = new ErrorCode(2_001_004_009, "订单不存在");
    // 转移
    ErrorCode TRANSFER_COLLECTION_NOT_EXISTS = new ErrorCode(2_001_005_000, "该藏品不存在");
    ErrorCode CANT_TRANSFER_SELF = new ErrorCode(2_001_005_001, "不能转移给自己");
    ErrorCode UERT_DOSENT_EXSIT = new ErrorCode(2_001_005_002, "用户不存在");
    ErrorCode TRANSFER_NO_ENOUGH_COLLECTION= new ErrorCode(2_001_005_002, "藏品数量不足");
    // 取回
    ErrorCode GETBACK_EXPRESS_REQUIRED = new ErrorCode(2_001_006_007, "请选择快递公司并填写有效单号");
    ErrorCode GETBACK_NOT_EXISTS = new ErrorCode(2_001_006_000, "取回订单不存在");
    ErrorCode GETBACK_COLLECTION_NOT_EXISTS = new ErrorCode(2_001_006_001, "该藏品不存在");
    ErrorCode GETBACK_NO_ENOUGH_COLLECTION= new ErrorCode(2_001_006_002, "藏品数量不足");
    ErrorCode GETBACK_EXIST= new ErrorCode(2_001_006_003, "存在在途订单，不能重复取回");
    ErrorCode GETBACK_FAIL= new ErrorCode(2_001_006_003, "确认收货失败");
    ErrorCode GETBACK_STATUS_INVALID = new ErrorCode(2_001_006_004, "该取回申请当前状态不能执行此操作");
    ErrorCode GETBACK_CANCEL_COLLECTION_INVALID = new ErrorCode(2_001_006_005, "藏品状态已变化，无法取消取回，请刷新后重试");
    ErrorCode GETBACK_COLLECTION_STATUS_INVALID = new ErrorCode(2_001_006_006, "藏品当前状态不能取回");
    // ========== 客服会话 1-013-019-000 ==========
    ErrorCode KEFU_CONVERSATION_NOT_EXISTS = new ErrorCode(2_001_007_000, "客服会话不存在");

    // ========== 客服消息 1-013-020-000 ==========
    ErrorCode KEFU_MESSAGE_NOT_EXISTS = new ErrorCode(2_001_008_000, "客服消息不存在");
    // ========== 分销提现 模块 1-011-008-000 ==========
    ErrorCode BROKERAGE_WITHDRAW_NOT_EXISTS = new ErrorCode(2_001_009_000, "提现记录不存在");
    ErrorCode BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING = new ErrorCode(2_001_009_001, "提现记录状态不是审核中");
    ErrorCode BROKERAGE_WITHDRAW_MIN_PRICE = new ErrorCode(2_001_009_002, "提现金额不能低于 {} 元");
    ErrorCode BROKERAGE_WITHDRAW_USER_BALANCE_NOT_ENOUGH = new ErrorCode(2_001_009_003, "您当前最多可提现 {} 元");
    ErrorCode BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_ID_ERROR = new ErrorCode(2_001_009_005, "提现单更新转账状态失败，转账单不匹配");
    ErrorCode BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_TRANSFER_STATUS_NOT_SUCCESS_OR_CLOSED = new ErrorCode(2_001_009_006, "提现单更新转账状态失败，转账单状态不是成功或关闭状态");
    ErrorCode BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_PRICE_NOT_MATCH = new ErrorCode(2_001_009_007, "提现单更新转账状态失败，转账单金额不匹配");
    ErrorCode BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_MERCHANT_EXISTS = new ErrorCode(2_001_009_008, "提现单更新转账状态失败，转账单的商户订单不匹配");
    ErrorCode BROKERAGE_WITHDRAW_UPDATE_STATUS_FAIL_PAY_CHANNEL_NOT_MATCH = new ErrorCode(2_001_009_009, "提现单更新转账状态失败，转账渠道不匹配");
    // 广告
    ErrorCode ADS_NOT_EXISTS = new ErrorCode(2_001_010_000, "广告不存在");
    ErrorCode ARTICLE_NOT_EXISTS = new ErrorCode(2_001_011_000, "文章不存在");
    // 兑换
    ErrorCode EXCHANGE_NOT_EXISTS = new ErrorCode(2_001_011_000, "兑换品不存在");
    ErrorCode EXCHANGE_LOG_NOT_EXISTS = new ErrorCode(2_001_011_001, "兑换记录不存在");
    ErrorCode EXCHANGE_NO_ENOUGH_COLLECTION= new ErrorCode(2_001_011_002, "能量石不足");
    // 能量石
    ErrorCode STONE_EXCHANGE_NOT_EXISTS = new ErrorCode(2_001_012_000, "能量石兑换记录不存在");
    ErrorCode STONE_COLLECTION_NOT_EXISTS = new ErrorCode(2_001_012_001, "该藏品不存在");

    ErrorCode YKJ_ORDER_NOT_EXISTS = new ErrorCode(2_001_013_000, "一口价买单不存在");
    ErrorCode YKJ_COLLECTION_NOT_EXISTS = new ErrorCode(2_001_013_001, "该商品不存在");

    ErrorCode OFF_LINE = new ErrorCode(2_001_014_000, "暂未开放");

    ErrorCode STORAGE_PLAN_NOT_EXISTS = new ErrorCode(2_001_015_000, "寄存容量套餐不存在");
    ErrorCode ENERGY_STONE_NOT_ENOUGH = new ErrorCode(2_001_015_001, "能量石不足");
    ErrorCode STORAGE_PLAN_TYPE_ERROR = new ErrorCode(2_001_015_002, "套餐类型错误");


    ErrorCode BUY_ORDER_NOT_EXISTS = new ErrorCode(2_001_016_000, "批量交易买单不存在");
    ErrorCode SELL_ORDER_NOT_EXISTS = new ErrorCode(2_001_016_001, "批量交易卖单不存在");

    ErrorCode AUCTION_NOT_EXISTS = new ErrorCode(2_001_017_000, "竞价商品不存在");
    ErrorCode AUCTION_COLLECTION_INVALID = new ErrorCode(2_001_017_001, "藏品不属于您、未通过审核或已在交易中");
    ErrorCode AUCTION_ACTIVE_LIMIT = new ErrorCode(2_001_017_002, "同一账户最多同时上架10个竞价商品");
    ErrorCode AUCTION_STATUS_INVALID = new ErrorCode(2_001_017_003, "当前竞价状态不允许此操作");
    ErrorCode AUCTION_ENDED = new ErrorCode(2_001_017_004, "竞价已结束");
    ErrorCode AUCTION_SELF_BID = new ErrorCode(2_001_017_005, "不能对自己的商品出价");
    ErrorCode AUCTION_BID_TOO_LOW = new ErrorCode(2_001_017_006, "出价不能低于 {} 分");
    ErrorCode AUCTION_CANCEL_FORBIDDEN = new ErrorCode(2_001_017_007, "已有人出价，不能取消上架");
    ErrorCode AUCTION_END_TIME_INVALID = new ErrorCode(2_001_017_008, "截止时间必须在当前时间的5分钟之后");
    ErrorCode AUCTION_SINGLE_ITEM_REQUIRED = new ErrorCode(2_001_017_009, "竞价只支持数量为1的单件藏品");
    ErrorCode AUCTION_PRICE_LIMIT = new ErrorCode(2_001_017_010, "竞价金额已达上限");
    ErrorCode AUCTION_COLLECTION_TYPE_INVALID = new ErrorCode(2_001_017_011, "仅自定义类型或签名商品可以上架竞价");
    ErrorCode AUCTION_DELIST_ALREADY_PENDING = new ErrorCode(2_001_017_012, "该竞拍商品已提交下架申请，请等待后台审核");
    ErrorCode AUCTION_DELIST_REVIEW_INVALID = new ErrorCode(2_001_017_013, "该竞拍商品没有待处理的下架申请");
    ErrorCode AUCTION_GOOFISH_NOT_AUCTION = new ErrorCode(2_001_017_014, "该闲鱼商品不是竞拍，请确认后重新录入链接");
    ErrorCode AUCTION_GOOFISH_ALREADY_BOUND = new ErrorCode(2_001_017_015, "该闲鱼竞拍已绑定其他申请");
    ErrorCode AUCTION_MANUAL_BIND_FAILED = new ErrorCode(2_001_017_016, "录入闲鱼竞拍失败，请刷新后重试");
    ErrorCode AUCTION_FEE_NOT_CONFIGURED = new ErrorCode(2_001_017_017, "竞拍手续费未正确配置，请先设置参数 auctionfee（0-99）");
    ErrorCode AUCTION_SETTLEMENT_EXISTS = new ErrorCode(2_001_017_018, "该竞拍已经生成结算单，请勿重复确认");
    ErrorCode AUCTION_GOOFISH_ITEM_ID_INVALID = new ErrorCode(2_001_017_021,
            "无法识别闲鱼商品ID，请输入闲管家拍卖中页面显示的闲鱼商品ID");
    ErrorCode AUCTION_WITHDRAW_INVALID = new ErrorCode(2_001_017_019, "竞拍提现信息不匹配或已有待处理打款单");
    ErrorCode AUCTION_WITHDRAW_SOURCE_REQUIRED = new ErrorCode(2_001_017_020,
            "存在未完成的竞拍结算，请从‘我的竞拍’中选择对应商品申请打款");
}
