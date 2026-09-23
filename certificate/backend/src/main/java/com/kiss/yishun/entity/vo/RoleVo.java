package com.kiss.yishun.entity.vo;

import lombok.Data;

@Data
public class RoleVo {
    private String addPermission;
    private String updatePermission;
    private String deletePermission;
    private String viewPermission;
    private String exportPermission;
    private String name;
    private String code;
    private int level;
    private Long id;
}
