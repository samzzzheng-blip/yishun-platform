package com.techtron.onebook.module.trade.convert.aftersale;

import com.techtron.onebook.module.trade.dal.dataobject.aftersale.AfterSaleLogDO;
import com.techtron.onebook.module.trade.service.aftersale.bo.AfterSaleLogCreateReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AfterSaleLogConvert {

    AfterSaleLogConvert INSTANCE = Mappers.getMapper(AfterSaleLogConvert.class);

    AfterSaleLogDO convert(AfterSaleLogCreateReqBO bean);

}
