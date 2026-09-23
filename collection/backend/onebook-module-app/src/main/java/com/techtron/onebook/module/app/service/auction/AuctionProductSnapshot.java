package com.techtron.onebook.module.app.service.auction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;

record AuctionProductSnapshot(String itemId, List<String> images, String json, boolean needsConfirmation) {
    static AuctionProductSnapshot parse(String productId, String expectedItemId, JsonNode data) {
        if (data == null || !productId.equals(data.path("product_id").asText()))
            throw new IllegalArgumentException("闲管家返回的商品编号不匹配");
        List<JsonNode> shops = new ArrayList<>();
        data.path("publish_shop").forEach(shop -> {
            if (shop.path("item_id").asText().matches("[0-9]{8,20}") &&
                    (expectedItemId == null || expectedItemId.isBlank() || expectedItemId.equals(shop.path("item_id").asText()))) shops.add(shop);
        });
        if (shops.size() != 1) throw new IllegalArgumentException("无法唯一匹配闲鱼商品，请核对product_id与已绑定闲鱼链接");
        JsonNode shop = shops.get(0);
        List<String> images = new ArrayList<>();
        shop.path("images").forEach(image -> {
            String url = image.asText("").trim();
            if (url.startsWith("http://")) url = "https://" + url.substring(7);
            if (url.startsWith("https://") && images.size() < 9 && !images.contains(url)) images.add(url);
        });
        ObjectNode snapshot = new ObjectMapper().createObjectNode();
        for (String field : List.of("product_id", "title", "price", "stock", "sold", "product_status", "publish_status", "online_time", "offline_time", "sold_time", "update_time")) {
            if (data.has(field)) snapshot.set(field, data.get(field));
        }
        snapshot.put("item_id", shop.path("item_id").asText());
        snapshot.set("images", new ObjectMapper().valueToTree(images));
        // 基础商品价格不是竞拍价；仅明确时间/已售信息触发人工核对，不自动结算。
        boolean terminal = data.path("sold").asLong(0) > 0 || data.path("sold_time").asLong(0) > 0
                || (data.path("offline_time").asLong(0) > 0 && data.path("offline_time").asLong() >= data.path("online_time").asLong(0));
        return new AuctionProductSnapshot(shop.path("item_id").asText(), images, snapshot.toString(), terminal);
    }
}
