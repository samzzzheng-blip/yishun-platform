package com.techtron.onebook.module.app.convert.fastrade;

import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.fasttrade.FastTradeItemDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface FastTradeConvert {
    FastTradeConvert INSTANCE = Mappers.getMapper(FastTradeConvert.class);

    @Mappings({
            @Mapping(source = "stock", target = "amount"),
            @Mapping(source = "id", target = "collectionId"),
            @Mapping(target = "id",  ignore = true)
    })
    FastTradeItemDO convert0(CollectionDO bean);

    List<FastTradeItemDO> convert(List<CollectionDO> list);
}
