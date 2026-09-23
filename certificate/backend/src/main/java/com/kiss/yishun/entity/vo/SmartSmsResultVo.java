package com.kiss.yishun.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class SmartSmsResultVo {
    private List<SendStatusVo> SendStatusSet;
    private String RequestId;

    @Data
    public static class SendStatusVo {
        private String SerialNo;
        private String PhoneNumber;
        private int Fee;
        private String SessionContext;
        private String Code;
        private String Message;
        private String IsoCode;
    }
}
