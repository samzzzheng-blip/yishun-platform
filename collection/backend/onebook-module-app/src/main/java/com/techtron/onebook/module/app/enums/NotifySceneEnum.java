package com.techtron.onebook.module.app.enums;

import cn.hutool.core.util.ArrayUtil;
import com.techtron.onebook.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 批量交易站内信模板
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum NotifySceneEnum implements ArrayValuable<Integer> {

    BUY_NOTIFY(1, "buy_notify", "买单成交通知"),
    SELL_NOTIFY(2, "sell_notify", "卖单成交通知"),
    YKJ_NOTIFY(3, "ykj_notify", "一口价成交通知"),
    FAST_NOTIFY(4, "fast_notify", "快速变现成交通知"),
    EXCHANGE_NOTIFY(5, "exchange_notify", "兑换成交通知"),
    GETBACK_NOTIFY(6, "getback_notify", "取回发货通知"),
    TRANSFER_NOTIFY(7, "transfer_notify", "转移成功通知"),
    GET_TRANSFER_NOTIFY(8, "get_transfer_notify", "接收转移通知"),
    EXCHANGE_DELIVER_NOTIFY(9, "exchange_deliver_notify", "兑换发货通知"),
    STORAGE_EXPIRE_NOTIFY(10, "storage_expire_notify", "存储套餐即将过期通知"),;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(NotifySceneEnum::getScene).toArray(Integer[]::new);

    /**
     * 验证场景的编号
     */
    private final Integer scene;
    /**
     * 模版编码
     */
    private final String templateCode;
    /**
     * 描述
     */
    private final String description;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static NotifySceneEnum getCodeByScene(Integer scene) {
        return ArrayUtil.firstMatch(sceneEnum -> sceneEnum.getScene().equals(scene),
                values());
    }

}
