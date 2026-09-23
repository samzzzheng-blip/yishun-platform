package com.techtron.onebook.module.app.controller.admin.yikoujia.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.techtron.onebook.framework.excel.core.annotations.DictFormat;
import com.techtron.onebook.framework.excel.core.convert.DictConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 一口价 Response VO")
@Data
@ExcelIgnoreUnannotated
public class YikoujiaRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "132")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED, example = "12449")
    @ExcelProperty("用户id")
    private Long userId;

    @Schema(description = "藏品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "29582")
    @ExcelProperty("藏品id")
    private Long collectionId;

    @Schema(description = "数量")
    @ExcelProperty("数量")
    private Integer amount;

    @Schema(description = "价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "18643")
    @ExcelProperty("价格，单位：分")
    private Integer price;

    @Schema(description = "状态（0待交易 1待结算 2已结算）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "状态（0待交易 1待结算 2已结算）", converter = DictConvert.class)
    @DictFormat("collection_trade_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    private List<String> picUrl;

    private String name;

    private String productId;

    private String introduction;

    private String collectionName;

    @Schema(description = "藏品分类名称")
    private String categoryName;

    @Schema(description = "实际成交渠道：MINIAPP/GOOFISH/CONFLICT")
    private String saleChannel;

    @Schema(description = "成交时间")
    private LocalDateTime soldAt;

    @Schema(description = "闲鱼最后商品状态")
    private Integer goofishStatus;

    @Schema(description = "最后同步时间")
    private LocalDateTime lastSyncTime;

    @Schema(description = "同步或冲突说明")
    private String syncRemark;

}
