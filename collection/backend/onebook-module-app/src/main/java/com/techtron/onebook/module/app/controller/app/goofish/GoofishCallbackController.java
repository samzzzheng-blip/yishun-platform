package com.techtron.onebook.module.app.controller.app.goofish;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtron.onebook.module.app.controller.app.goofish.vo.GoofishCallbackRespVO;
import com.techtron.onebook.module.app.service.yikoujia.GoofishCallbackService;
import com.techtron.onebook.module.app.service.yikoujia.YikoujiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "闲管家 - 推送回调")
@RestController
@RequestMapping("/app/goofish/callback")
@Slf4j
public class GoofishCallbackController {

    @Resource
    private YikoujiaService yikoujiaService;

    @Resource
    private GoofishCallbackService callbackService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/order")
    @Operation(summary = "接收闲管家订单状态推送")
    @PermitAll
    public ResponseEntity<GoofishCallbackRespVO> receiveOrder(
            @RequestParam("appid") String appId,
            @RequestParam("timestamp") long timestamp,
            @RequestParam("sign") String sign,
            @RequestBody String rawBody) {
        if (!yikoujiaService.verifyGoofishCallback(appId, timestamp, sign, rawBody)) {
            log.warn("[receiveOrder][拒绝签名无效的闲管家订单回调]");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GoofishCallbackRespVO.failure("签名校验失败"));
        }
        try {
            JsonNode payload = objectMapper.readTree(rawBody);
            String orderNo = text(payload, "order_no");
            String productId = text(payload, "product_id");
            if ((orderNo == null || orderNo.isBlank()) && (productId == null || productId.isBlank())) {
                return ResponseEntity.badRequest().body(GoofishCallbackRespVO.failure("缺少订单或商品编号"));
            }
            callbackService.processOrder(orderNo, productId);
            return ResponseEntity.ok(GoofishCallbackRespVO.success());
        } catch (Exception ex) {
            log.warn("[receiveOrder][闲管家订单回调报文无法解析]", ex);
            return ResponseEntity.badRequest().body(GoofishCallbackRespVO.failure("报文格式错误"));
        }
    }

    @PostMapping("/product")
    @Operation(summary = "接收闲管家商品状态推送")
    @PermitAll
    public ResponseEntity<GoofishCallbackRespVO> receiveProduct(
            @RequestParam("appid") String appId,
            @RequestParam("timestamp") long timestamp,
            @RequestParam("sign") String sign,
            @RequestBody String rawBody) {
        if (!yikoujiaService.verifyGoofishCallback(appId, timestamp, sign, rawBody)) {
            log.warn("[receiveProduct][拒绝签名无效的闲管家商品回调]");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GoofishCallbackRespVO.failure("签名校验失败"));
        }
        try {
            String productId = text(objectMapper.readTree(rawBody), "product_id");
            if (productId == null || productId.isBlank()) {
                return ResponseEntity.badRequest().body(GoofishCallbackRespVO.failure("缺少商品编号"));
            }
            callbackService.processProduct(productId);
            return ResponseEntity.ok(GoofishCallbackRespVO.success());
        } catch (Exception ex) {
            log.warn("[receiveProduct][闲管家商品回调报文无法解析]", ex);
            return ResponseEntity.badRequest().body(GoofishCallbackRespVO.failure("报文格式错误"));
        }
    }

    private String text(JsonNode payload, String field) {
        JsonNode value = payload.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }
}
