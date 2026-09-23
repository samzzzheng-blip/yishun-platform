package com.techtron.onebook.module.app.convert.stoneexchange;

import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.stoneexchange.StoneExchangeItemDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface StoneExchangeConvert {
    StoneExchangeConvert INSTANCE = Mappers.getMapper(StoneExchangeConvert.class);

    @Mappings({
            @Mapping(source = "id", target = "collectionId"),
            @Mapping(target = "id",  ignore = true)
    })
    StoneExchangeItemDO convert0(CollectionDO bean);

    List<StoneExchangeItemDO> convert(List<CollectionDO> list);
}
