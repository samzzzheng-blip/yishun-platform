package com.techtron.onebook.module.app.service.auction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuctionProductSnapshotTest {
    static ObjectNode fixture() throws Exception {
        return (ObjectNode) new ObjectMapper().readTree("""
            {"product_id":1746190442089157,"title":"拍品","price":40000,"stock":1,"sold":0,
             "product_status":51,"publish_status":0,"online_time":1788758206,"offline_time":0,"sold_time":0,
             "publish_shop":[{"item_id":1080821222235,"images":["http://img.alicdn.com/a.jpg","https://img.alicdn.com/a.jpg"]}]}
            """);
    }
    @Test void separatesIdsAndNormalizesPhotosWithoutInferringStatus51() throws Exception {
        var result = AuctionProductSnapshot.parse("1746190442089157", "1080821222235", fixture());
        assertEquals("1080821222235", result.itemId());
        assertEquals(java.util.List.of("https://img.alicdn.com/a.jpg"), result.images());
        assertFalse(result.needsConfirmation());
        assertEquals(40000, new ObjectMapper().readTree(result.json()).path("price").asInt());
    }
    @Test void rejectsWrongProductAndItemIds() throws Exception {
        var data = fixture();
        assertThrows(IllegalArgumentException.class, () -> AuctionProductSnapshot.parse("12345678", null, data));
        assertThrows(IllegalArgumentException.class, () -> AuctionProductSnapshot.parse("1746190442089157", "1079935170548", data));
    }
    @Test void refusesAmbiguousShopsWithoutExpectedItem() throws Exception {
        var data = fixture();
        data.withArray("publish_shop").addObject().put("item_id", "1079935170548");
        assertThrows(IllegalArgumentException.class, () -> AuctionProductSnapshot.parse("1746190442089157", null, data));
        assertEquals("1080821222235", AuctionProductSnapshot.parse("1746190442089157", "1080821222235", data).itemId());
    }
    @Test void explicitOfflineOrSoldInformationRequiresHumanConfirmation() throws Exception {
        var data = fixture();
        data.put("offline_time", 1788758306);
        assertTrue(AuctionProductSnapshot.parse("1746190442089157", null, data).needsConfirmation());
        data.put("offline_time", 0).put("sold", 1);
        assertTrue(AuctionProductSnapshot.parse("1746190442089157", null, data).needsConfirmation());
    }
    @Test void olderOfflineTimeDoesNotCloseRelistedProduct() throws Exception {
        var data = fixture().put("offline_time", 1788758000);
        assertFalse(AuctionProductSnapshot.parse("1746190442089157", null, data).needsConfirmation());
    }
}
