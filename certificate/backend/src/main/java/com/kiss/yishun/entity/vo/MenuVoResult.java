package com.kiss.yishun.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuVoResult {

    private Data data;

    @lombok.Data
    class Data {
        private List<MenuVo> list;
    }
}
