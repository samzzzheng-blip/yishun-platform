package com.techtron.onebook.module.app.service.logistics;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.techtron.onebook.module.app.controller.app.getback.AppGetbackController;
import com.techtron.onebook.module.app.controller.admin.getback.vo.GetbackUpdateReqVO;
import com.techtron.onebook.module.app.service.getback.GetbackServiceImpl;
import com.techtron.onebook.module.app.dal.mysql.getback.GetbackMapper;
import com.techtron.onebook.module.app.dal.dataobject.getback.GetbackDO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetbackLogisticsBoundaryTest {
    @Test void memberCannotMarkOrderShipped() {
        var controller = new AppGetbackController();
        for (Integer status : new Integer[] { null, 0, 1, 3 }) {
            var request = new GetbackUpdateReqVO().setId(1L).setStatus(status);
            assertThrows(com.techtron.onebook.framework.common.exception.ServiceException.class, () -> controller.updateGetback(request));
        }
    }
    @Test void shippingRequiresKnownCarrierAndNonemptyWaybill() {
        var mapper = mock(GetbackMapper.class);
        var service = new GetbackServiceImpl();
        ReflectionTestUtils.setField(service, "getbackMapper", mapper);
        when(mapper.selectByIdForUpdate(1L)).thenReturn(new GetbackDO().setId(1L).setStatus(0));
        for (var request : new GetbackUpdateReqVO[] {
            new GetbackUpdateReqVO().setId(1L).setStatus(1).setDeliverCode("SF123"),
            new GetbackUpdateReqVO().setId(1L).setStatus(1).setExpressCompany("shunfeng").setDeliverCode(" "),
            new GetbackUpdateReqVO().setId(1L).setStatus(1).setExpressCompany("unknown").setDeliverCode("123")
        }) assertThrows(com.techtron.onebook.framework.common.exception.ServiceException.class, () -> service.updateGetback(request));
    }
}
