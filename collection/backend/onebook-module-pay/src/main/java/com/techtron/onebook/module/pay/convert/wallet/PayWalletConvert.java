package com.techtron.onebook.module.pay.convert.wallet;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.pay.controller.admin.wallet.vo.wallet.PayWalletRespVO;
import com.techtron.onebook.module.pay.controller.app.wallet.vo.wallet.AppPayWalletRespVO;
import com.techtron.onebook.module.pay.dal.dataobject.wallet.PayWalletDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayWalletConvert {

    PayWalletConvert INSTANCE = Mappers.getMapper(PayWalletConvert.class);

    AppPayWalletRespVO convert(PayWalletDO bean);

    PayWalletRespVO convert02(PayWalletDO bean);

    PageResult<PayWalletRespVO> convertPage(PageResult<PayWalletDO> page);

}
