package com.techtron.onebook.module.app.convert.getback;

import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.getback.GetbackItemDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface GetbackConvert {
    GetbackConvert INSTANCE = Mappers.getMapper(GetbackConvert.class);

    @Mappings({
            @Mapping(source = "stock", target = "amount"),
            @Mapping(source = "id", target = "collectionId"),
            @Mapping(target = "id",  ignore = true)
    })
    GetbackItemDO convert0(CollectionDO bean);

    List<GetbackItemDO> convert(List<CollectionDO> list);
}
