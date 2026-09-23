package com.techtron.onebook.module.app.service.yikoujia.bo;

import com.techtron.onebook.framework.common.exception.ErrorCode;
import lombok.Data;

@Data
public class CreateProductBO {
    private Integer code;
    /**
     * 错误提示，用户可阅读
     *
     * @see ErrorCode#getMsg() ()
     */
    private String msg;
    /**
     * 返回数据
     */
    private ProductBO data;
}
