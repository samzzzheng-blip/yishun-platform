package com.techtron.onebook.module.app.convert.buyorder.yikoujia;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Mapper
public interface YikoujiaConvert {
    YikoujiaConvert INSTANCE = Mappers.getMapper(YikoujiaConvert.class);

    @Mappings({
            @Mapping(source = "stock", target = "amount"),
            @Mapping(source = "id", target = "collectionId"),
            @Mapping(target = "id",  ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "picUrl", expression = "java(resolvePicUrls(bean))")
    })
    YikoujiaDO convert0(CollectionDO bean);

    List<YikoujiaDO> convert(List<CollectionDO> list);

    /**
     * 藏品登记已升级为 picUrls 多图字段，一口价创建时优先复制该字段。
     * picUrl 是历史单图字段，仅作为旧数据兼容。
     */
    default List<String> resolvePicUrls(CollectionDO bean) {
        if (bean == null) {
            return Collections.emptyList();
        }
        if (bean.getPicUrls() != null && !bean.getPicUrls().isEmpty()) {
            return bean.getPicUrls().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(url -> !url.isEmpty())
                    .toList();
        }
        return convertStringToList(bean.getPicUrl());
    }

    default List<String> convertStringToList(String picUrl) {
        if (picUrl == null || picUrl.isEmpty()) {
            return Collections.emptyList();
        }
        // 如果存储的是 JSON 数组格式：["url1","url2"]
        try {
            return new ObjectMapper().readValue(picUrl, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // 如果存储的是逗号分隔格式：url1,url2
            return Arrays.asList(picUrl.split(","));
        }
    }
}
