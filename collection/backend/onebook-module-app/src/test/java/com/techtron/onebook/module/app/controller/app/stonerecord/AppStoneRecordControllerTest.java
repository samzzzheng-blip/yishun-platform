package com.techtron.onebook.module.app.controller.app.stonerecord;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.security.core.util.SecurityFrameworkUtils;
import com.techtron.onebook.module.app.controller.app.stonerecord.vo.AppStoneRecordPageReqVO;
import com.techtron.onebook.module.app.dal.dataobject.stonerecord.StoneRecordDO;
import com.techtron.onebook.module.app.dal.mysql.stonerecord.StoneRecordMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppStoneRecordControllerTest {
    @Test void usesAuthenticatedUserAndPreservesExpense() {
        var mapper = mock(StoneRecordMapper.class);
        var controller = new AppStoneRecordController();
        ReflectionTestUtils.setField(controller, "stoneRecordMapper", mapper);
        var req = new AppStoneRecordPageReqVO();
        req.setDirection("expense");
        when(mapper.selectUserPage(42L, req)).thenReturn(new PageResult<>(List.of(
                new StoneRecordDO().setId(1L).setUserId(42L).setType(2).setAmount(-20).setBalance(80)
                        .setExchangeLogId(9L).setExchangeName("生天目仁美").setExchangeQuantity(1)), 1L));
        try (var security = mockStatic(SecurityFrameworkUtils.class)) {
            security.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(42L);
            var result = controller.page(req).getData();
            assertEquals(1L, result.getTotal());
            assertEquals(-20, result.getList().get(0).getAmount());
            assertEquals(80, result.getList().get(0).getBalance());
            assertEquals("生天目仁美", result.getList().get(0).getExchangeName());
            assertEquals(9L, result.getList().get(0).getExchangeLogId());
            assertEquals(1, result.getList().get(0).getExchangeQuantity());
            verify(mapper).selectUserPage(42L, req);
        }
    }

    @Test void rejectsUnknownFilter() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var req = new AppStoneRecordPageReqVO();
            req.setDirection("invalid");
            assertFalse(factory.getValidator().validate(req).isEmpty());
            for (String direction : List.of("all", "income", "expense")) {
                req.setDirection(direction);
                assertTrue(factory.getValidator().validate(req).isEmpty());
            }
        }
    }

    @Test void missingIdentityNeverQueriesAllUsers() {
        var mapper = mock(StoneRecordMapper.class, CALLS_REAL_METHODS);
        assertThrows(NullPointerException.class, () -> mapper.selectUserPage(null, new AppStoneRecordPageReqVO()));
    }
}
