package com.kiss.yishun.entity.enums;

public enum SmartPhoneCodeEnum {
    REGIST("1","注册"),
    MODIFY_PHONE("2","更换手机号");

    private String code;
    private String remark;

    SmartPhoneCodeEnum(String code, String remark) {
        this.code = code;
        this.remark = remark;
    }

    public String getCode() {
        return code;
    }

    public String getRemark() {
        return remark;
    }
}
