package com.techtron.onebook.module.app.controller.app.goofish.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoofishCallbackRespVO {

    private String result;
    private String msg;

    public static GoofishCallbackRespVO success() {
        return new GoofishCallbackRespVO("success", "接收成功");
    }

    public static GoofishCallbackRespVO failure(String message) {
        return new GoofishCallbackRespVO("fail", message);
    }
}
