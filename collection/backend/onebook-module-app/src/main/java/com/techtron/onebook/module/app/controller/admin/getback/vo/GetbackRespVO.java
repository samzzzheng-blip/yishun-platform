package com.techtron.onebook.module.app.controller.admin.getback.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.techtron.onebook.framework.excel.core.annotations.DictFormat;
import com.techtron.onebook.framework.excel.core.convert.DictConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 取回 Response VO")
@Data
@ExcelIgnoreUnannotated
public class GetbackRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31577")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "19406")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "藏品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30081")
    @ExcelProperty("藏品id")
    private Long collectionId;

    @Schema(description = "数量")
    @ExcelProperty("数量")
    private Integer amount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("collection_deliver_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "收件人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "天呈")
    @ExcelProperty("收件人名称")
    private String receiverName;

    @Schema(description = "收件人手机", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("收件人手机")
    private String receiverMobile;

    @Schema(description = "收件人地区", example = "24519")
    @ExcelProperty("收件人地区")
    private String receiverAreaName;

    @Schema(description = "收件人详细地址")
    @ExcelProperty("收件人详细地址")
    private String receiverDetailAddress;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    private String picUrl;

    private String deliverCode;

    private String expressCompany;

    private List<GetbackItemVO> items;

}
