package com.kiss.yishun.entity.enums;

public enum SmartAuditEnum {
    WAIT(0,"待审核"),
    PASS(1,"已通过"),
    REJECT(2,"已拒绝");

    private int code;
    private String remark;

    SmartAuditEnum(int code, String remark) {
        this.code = code;
        this.remark = remark;
    }

    public int getCode() {
        return code;
    }

    public String getRemark() {
        return remark;
    }
}
