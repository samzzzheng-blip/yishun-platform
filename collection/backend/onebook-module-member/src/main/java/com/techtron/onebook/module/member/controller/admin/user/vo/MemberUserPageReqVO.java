package com.techtron.onebook.module.member.controller.admin.user.vo;

import com.techtron.onebook.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

import static com.techtron.onebook.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 会员用户分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MemberUserPageReqVO extends PageParam {

    @Schema(description = "手机号", example = "15601691300")
    private String mobile;

    @Schema(description = "用户昵称", example = "李四")
    private String nickname;

    @Schema(description = "最后登录时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] loginDate;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "会员标签编号列表", example = "[1, 2]")
    private List<Long> tagIds;

    @Schema(description = "会员等级编号", example = "1")
    private Long levelId;

    @Schema(description = "用户分组编号", example = "1")
    private Long groupId;

    @Schema(description = "能量石排序方向：asc-升序，desc-降序", example = "desc")
    @Pattern(regexp = "asc|desc", message = "能量石排序方向必须为 asc 或 desc")
    private String energyStoneOrder;

    // TODO 天呈：注册用户类型；

    // TODO 天呈：登录用户类型；

}
