package com.techtron.onebook.module.member.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Schema(description = "用户 APP - 修改密码 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppMemberUserUpdatePasswordReqVO {

    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "buzhidao")
    @NotEmpty(message = "新密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;

    @Schema(description = "旧密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "buzhidao")
    @NotEmpty(message = "旧密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String oldPassword;

}
