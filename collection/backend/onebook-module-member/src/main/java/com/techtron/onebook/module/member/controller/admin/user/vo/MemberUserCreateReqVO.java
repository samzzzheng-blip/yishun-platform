package com.techtron.onebook.module.member.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 会员用户更新 Request VO")
@Data
public class MemberUserCreateReqVO {

    @Schema(description = "账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23788")
    @NotNull(message = "账号不能为空")
    private String mobile;

    private String password;

}
