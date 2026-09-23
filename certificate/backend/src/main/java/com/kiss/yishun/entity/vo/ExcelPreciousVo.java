package com.kiss.yishun.entity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ExcelPreciousVo {

    @ExcelProperty(value="编号")
    private String certNumber;
    @ExcelProperty(value="载体")
    private String itemType;
    @ExcelProperty(value="姓名")
    private String signer;
    @ExcelProperty(value="时间")
    private String publishTime;
    @ExcelProperty(value="地点")
    private String publishCity;
    @ExcelProperty(value="活动")
    private String publishActivity;
}
