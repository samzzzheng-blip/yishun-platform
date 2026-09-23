package com.techtron.onebook.module.app.enums.stonerecord;

import com.techtron.onebook.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum StoneRecordTypeEnum implements ArrayValuable<Integer> {

    EXCHANGE_STONE(1, "兑换能量石"),
    EXCHANGE_COLLECTION(2, "兑换藏品"),
    ADMIN_ADD(3, "管理员"),
    BUY_CAPACITY(4, "购买容量"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(StoneRecordTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;

    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
