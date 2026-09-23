package com.kiss.yishun.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuVo {

    private String resName;
    private String resKey;
    private String resIcon;
    private Long id;
    private List<MenuVo> children;
}
