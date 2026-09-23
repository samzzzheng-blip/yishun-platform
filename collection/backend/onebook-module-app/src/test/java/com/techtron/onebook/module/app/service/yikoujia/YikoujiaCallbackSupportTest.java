package com.techtron.onebook.module.app.service.yikoujia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YikoujiaCallbackSupportTest {

    @Test
    void shouldGenerateDocumentedGoofishSignature() {
        String sign = YikoujiaServiceImpl.generateGoofishSign(
                203413189371893L,
                "o9wl81dncmvby3ijpq7eur456zhgtaxs",
                1636087298L,
                "{\"product_id\":\"219530767978565\"}");

        assertEquals("c26c8a48809141f3dd80bd9b9ddb41ea", sign);
    }

    @Test
    void shouldTreatSoldStatusOrZeroStockAsSold() {
        assertTrue(YikoujiaServiceImpl.isGoofishSold(4, 1));
        assertTrue(YikoujiaServiceImpl.isGoofishSold(3, 0));
        assertFalse(YikoujiaServiceImpl.isGoofishSold(3, 1));
        assertFalse(YikoujiaServiceImpl.isGoofishSold(-1, null));
    }
}
