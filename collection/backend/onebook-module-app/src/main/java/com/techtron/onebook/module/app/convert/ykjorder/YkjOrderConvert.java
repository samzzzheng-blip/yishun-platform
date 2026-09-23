package com.techtron.onebook.module.app.convert.ykjorder;

import com.techtron.onebook.framework.common.enums.UserTypeEnum;
import com.techtron.onebook.module.app.dal.dataobject.ykjorder.YkjOrderDO;
import com.techtron.onebook.module.pay.api.order.dto.PayOrderCreateReqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Duration;

import static com.techtron.onebook.framework.common.util.date.LocalDateTimeUtils.addTime;
import static com.techtron.onebook.framework.common.util.servlet.ServletUtils.getClientIP;

@Mapper
public interface YkjOrderConvert {
    YkjOrderConvert INSTANCE = Mappers.getMapper(YkjOrderConvert.class);

    default PayOrderCreateReqDTO convert(YkjOrderDO order) {
        PayOrderCreateReqDTO createReqDTO = new PayOrderCreateReqDTO()
                .setAppKey("ykj").setUserIp(getClientIP())
                .setUserId(order.getUserId()).setUserType(UserTypeEnum.MEMBER.getValue());
        // 商户相关字段
        createReqDTO.setMerchantOrderId(String.valueOf(order.getId()));
        createReqDTO.setSubject("一口价");
        createReqDTO.setBody("一口价");
        createReqDTO.setSubject("一口价");
        // 订单相关字段
        createReqDTO.setPrice(order.getPrice()).setExpireTime(addTime(Duration.ofHours(2)));

        return createReqDTO;
    }
}
