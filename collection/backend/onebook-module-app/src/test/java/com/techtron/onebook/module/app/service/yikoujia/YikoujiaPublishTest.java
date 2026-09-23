package com.techtron.onebook.module.app.service.yikoujia;

import com.sun.net.httpserver.HttpServer;
import com.techtron.onebook.framework.common.exception.ServiceException;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class YikoujiaPublishTest {
    private HttpServer server;
    private Object originalDomain;
    private final AtomicInteger details = new AtomicInteger();
    private final AtomicInteger publishes = new AtomicInteger();
    private int initialStatus = 31, finalStatus = 31, detailCode = 0;
    private String publishResponse = "{\"code\":0,\"data\":{}}";
    @BeforeEach void setup() throws Exception {
        originalDomain = ReflectionTestUtils.getField(YikoujiaServiceImpl.class, "domain");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/open/product/detail", exchange -> {
            int status = details.incrementAndGet() == 1 ? initialStatus : finalStatus;
            byte[] bytes = ("{\"code\":" + detailCode + ",\"data\":{\"product_status\":" + status + "}}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        server.createContext("/api/open/product/publish", exchange -> {
            publishes.incrementAndGet();
            byte[] bytes = publishResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        server.start();
        ReflectionTestUtils.setField(YikoujiaServiceImpl.class, "domain", "http://127.0.0.1:" + server.getAddress().getPort());
    }
    @AfterEach void cleanup() {
        ReflectionTestUtils.setField(YikoujiaServiceImpl.class, "domain", originalDomain);
        server.stop(0);
    }
    private void publish() { ReflectionTestUtils.invokeMethod(new YikoujiaServiceImpl(), "upProduct", "1757566546642629"); }
    @Test void liveListingSkipsPublish() { initialStatus = 22; publish(); assertEquals(0, publishes.get()); }
    @Test void offShelfListingPublishes() { publish(); assertEquals(1, publishes.get()); }
    @Test void alreadyLiveRaceIsVerified() {
        finalStatus = 22; publishResponse = "{\"code\":100001,\"msg\":\"当前商品已在销售中，无需重复操作\"}";
        publish(); assertEquals(2, details.get()); assertEquals(1, publishes.get());
    }
    @Test void sameErrorCodeWithoutLiveStateStillFails() {
        publishResponse = "{\"code\":100001,\"msg\":\"其他业务错误\"}";
        assertThrows(ServiceException.class, this::publish); assertEquals(2, details.get());
    }
    @Test void unrelatedErrorIsNotAcceptedEvenIfStateLaterChanges() {
        finalStatus = 22; publishResponse = "{\"code\":12345}";
        assertThrows(ServiceException.class, this::publish); assertEquals(1, details.get());
    }
    @Test void missingResponseCodeFailsClosed() {
        publishResponse = "{}"; assertThrows(ServiceException.class, this::publish);
    }
    @Test void detailFailureDoesNotPublish() {
        detailCode = 100001; assertThrows(ServiceException.class, this::publish); assertEquals(0, publishes.get());
    }
}
