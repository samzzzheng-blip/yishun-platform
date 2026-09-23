package com.techtron.onebook.module.app.service.ads;

import com.techtron.onebook.module.app.controller.admin.ads.vo.AdsSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.ads.AdsDO;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.dal.mysql.ads.AdsMapper;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import com.techtron.onebook.framework.common.exception.ServiceException;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class AdsServiceTest {
    AdsMapper ads = mock(AdsMapper.class);
    YikoujiaMapper products = mock(YikoujiaMapper.class);
    AdsServiceImpl service = new AdsServiceImpl();
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "adsMapper", ads);
        ReflectionTestUtils.setField(service, "yikoujiaMapper", products);
        when(ads.selectById(8L)).thenReturn(AdsDO.builder().id(8L).targetProductId(672L).build());
    }
    AdsSaveReqVO request(Long target) {
        var request = new AdsSaveReqVO();
        request.setId(8L); request.setTargetProductId(target); request.setStatus(0);
        return request;
    }
    @Test void savesNewAvailableProduct() {
        when(products.selectById(700L)).thenReturn(YikoujiaDO.builder().id(700L).status(3).build());
        service.createAds(request(700L));
        var captured = ArgumentCaptor.forClass(AdsDO.class);
        verify(ads).insert(captured.capture());
        assertEquals(700L, captured.getValue().getTargetProductId());
    }
    @Test void replacesExistingTarget() {
        when(products.selectById(701L)).thenReturn(YikoujiaDO.builder().id(701L).status(3).build());
        service.updateAds(request(701L));
        var captured = ArgumentCaptor.forClass(AdsDO.class);
        verify(ads).updateById(captured.capture());
        assertEquals(701L, captured.getValue().getTargetProductId());
    }
    @Test void rejectsMissingOrUnavailableNewTarget() {
        assertThrows(ServiceException.class, () -> service.createAds(request(999L)));
        when(products.selectById(700L)).thenReturn(YikoujiaDO.builder().id(700L).status(4).build());
        assertThrows(ServiceException.class, () -> service.updateAds(request(700L)));
        verify(ads, never()).updateById(any(AdsDO.class));
        verify(ads, never()).insert(any(AdsDO.class));
    }
    @Test void allowsClearingTargetAndWritesNull() throws Exception {
        service.updateAds(request(null));
        var captured = ArgumentCaptor.forClass(AdsDO.class);
        verify(ads).updateById(captured.capture());
        assertNull(captured.getValue().getTargetProductId());
        assertEquals(FieldStrategy.ALWAYS, AdsDO.class.getDeclaredField("targetProductId")
                .getAnnotation(TableField.class).updateStrategy());
        verifyNoInteractions(products);
    }
    @Test void permitsEditingAdAfterExistingProductIsSold() {
        service.updateAds(request(672L));
        verify(ads).updateById(any(AdsDO.class));
        verifyNoInteractions(products);
    }
    @Test void permitsImageOnlyAd() {
        service.createAds(request(null));
        verify(ads).insert(any(AdsDO.class));
        verifyNoInteractions(products);
    }
}
