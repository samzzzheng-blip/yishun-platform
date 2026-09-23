package com.techtron.onebook.module.app.convert.collectiontransfer;

import com.techtron.onebook.module.app.dal.dataobject.collection.CollectionDO;
import com.techtron.onebook.module.app.dal.dataobject.collectiontransfer.CollectionTransferItemDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CollectionTransferConvert {
    CollectionTransferConvert INSTANCE = Mappers.getMapper(CollectionTransferConvert.class);

    @Mappings({
            @Mapping(source = "stock", target = "amount"),
            @Mapping(source = "id", target = "collectionId"),
            @Mapping(target = "id",  ignore = true)
    })
    CollectionTransferItemDO convert0(CollectionDO bean);

    List<CollectionTransferItemDO> convert(List<CollectionDO> list);
}
