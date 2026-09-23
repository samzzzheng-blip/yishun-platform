package com.kiss.yishun.entity.enums;

public enum OperationEnum {
    /**
     * 查看
     */
    View("view","查看"),
    /**
     * 新增
     */
    Add("add","新增"),
    /**
     * 更新
     */
    Update("update","更新"),
    /**
     * 删除
     */
    Delete("delete","删除"),
    /**
     * 导出
     */
    Export("export","导出");

    private final String operation;
    private final String name;

    OperationEnum(String operation,String name) {
        this.operation = operation;
        this.name = name;
    }

    public String getOperation() {
        return operation;
    }

    public String getName() {
        return name;
    }
}
