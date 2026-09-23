package com.kiss.yishun.utils;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimestampConverter implements Converter<Long> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Long.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    /**
     * Excel -> Java（导入时使用）
     */
    @Override
    public Long convertToJavaData(CellData cellData,
                                  ExcelContentProperty contentProperty,
                                  GlobalConfiguration globalConfiguration) {

        if (cellData == null || cellData.getStringValue() == null) {
            return null;
        }

        LocalDateTime dateTime = LocalDateTime.parse(
                cellData.getStringValue(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        return dateTime.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    /**
     * Java -> Excel（导出时使用）
     */
    @Override
    public CellData convertToExcelData(Long value,
                                       ExcelContentProperty contentProperty,
                                       GlobalConfiguration globalConfiguration) {

        if (value == null) {
            return new CellData("");
        }

        String time = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(value),
                        ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return new CellData(time);
    }
}