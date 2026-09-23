package com.techtron.onebook.module.app.service.logistics;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class Kuaidi100ClientTest {
    private Kuaidi100Client client() {
        return new Kuaidi100Client(new LogisticsProperties().setEnabled(true).setCustomer("test-account").setKey("test-key"));
    }
    @Test void mapsRealStatesAndPreservesTimesAndLocations() throws Exception {
        var client = client();
        for (var entry : Map.of("0", "运输中", "1", "已揽收", "5", "派送中", "3", "已签收").entrySet()) {
            var result = client.parse("{\"status\":\"200\",\"state\":\"" + entry.getKey() + "\",\"data\":[{\"ftime\":\"2026-09-16 10:00:00\",\"context\":\"快件已到达杭州转运中心\"}]}", "shunfeng", "SF123");
            assertEquals(entry.getValue(), result.getStatus());
            assertEquals("OK", result.getAvailability());
            assertEquals("2026-09-16 10:00:00", result.getNodes().get(0).time());
            assertEquals("快件已到达杭州转运中心", result.getNodes().get(0).description());
        }
    }
    @Test void distinguishesEmptyPhoneVerificationAndVendorFailures() throws Exception {
        var c = client();
        assertEquals("EMPTY", c.parse("{\"status\":\"500\"}", "shunfeng", "SF1").getAvailability());
        assertEquals("PHONE_MISMATCH", c.parse("{\"status\":\"408\"}", "shunfeng", "SF1").getAvailability());
        assertEquals("EMPTY", c.parse("{\"status\":\"200\",\"data\":[]}", "shunfeng", "SF1").getAvailability());
        var failure = c.parse("{\"returnCode\":\"503\",\"message\":\"secret info\"}", "shunfeng", "SF1");
        assertEquals("UNAVAILABLE", failure.getAvailability());
        assertFalse(failure.getMessage().contains("secret"));
        assertThrows(Exception.class, () -> c.parse("invalid json", "shunfeng", "SF1"));
    }
    @Test void sendsSignedFormOverHttps() throws Exception {
        var c = client();
        var server = MockRestServiceServer.bindTo(c.http()).build();
        server.expect(requestTo(Kuaidi100Client.URL)).andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(request -> {
                String body = ((org.springframework.mock.http.client.MockClientHttpRequest) request).getBodyAsString();
                String decoded = java.net.URLDecoder.decode(body, java.nio.charset.StandardCharsets.UTF_8);
                assertTrue(decoded.contains("customer=test-account"));
                assertTrue(decoded.contains("\"phone\":\"13800138000\""));
                String param = decoded.substring(decoded.indexOf("param=") + 6);
                String expected = org.springframework.util.DigestUtils.md5DigestAsHex((param + "test-keytest-account").getBytes(java.nio.charset.StandardCharsets.UTF_8)).toUpperCase();
                assertTrue(decoded.contains("sign=" + expected));
                assertFalse(decoded.contains("test-key"));
            }).andRespond(withSuccess("{\"status\":\"200\",\"data\":[]}", MediaType.APPLICATION_JSON));
        c.query("shunfeng", "SF123", "13800138000"); server.verify();
    }
}
