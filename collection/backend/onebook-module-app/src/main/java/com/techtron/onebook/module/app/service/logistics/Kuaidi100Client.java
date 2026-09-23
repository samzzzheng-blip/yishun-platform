package com.techtron.onebook.module.app.service.logistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class Kuaidi100Client {
    static final String URL = "https://poll.kuaidi100.com/poll/query.do";
    private final LogisticsProperties config;
    private final ObjectMapper json = new ObjectMapper();
    private final RestTemplate http;
    public Kuaidi100Client(LogisticsProperties config) {
        this.config = config;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(8000);
        http = new RestTemplate(factory);
    }
    RestTemplate http() { return http; }
    public boolean available() {
        return config.isEnabled() && !config.getCustomer().isBlank() && !config.getKey().isBlank();
    }
    public LogisticsResult query(String carrier, String number, String phone) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("com", carrier);
        params.put("num", number);
        params.put("phone", phone == null ? "" : phone);
        params.put("order", "desc");
        String param = json.writeValueAsString(params);
        String sign = DigestUtils.md5DigestAsHex((param + config.getKey() + config.getCustomer())
            .getBytes(StandardCharsets.UTF_8)).toUpperCase(Locale.ROOT);
        var body = new LinkedMultiValueMap<String, String>();
        body.add("customer", config.getCustomer()); body.add("sign", sign); body.add("param", param);
        HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String raw = http.postForObject(URL, new HttpEntity<>(body, headers), String.class);
        return parse(raw, carrier, number);
    }
    LogisticsResult parse(String raw, String carrier, String number) throws Exception {
        JsonNode response = json.readTree(raw);
        if (response == null) throw new IllegalArgumentException("Empty response");
        LogisticsResult result;
        String code = response.path("status").asText(response.path("returnCode").asText());
        if ("200".equals(code) && response.path("data").isArray()) {
            List<LogisticsResult.Node> nodes = new ArrayList<>();
            response.path("data").forEach(node -> {
                String description = node.path("context").asText("");
                if (!description.isBlank()) nodes.add(new LogisticsResult.Node(
                    node.path("ftime").asText(node.path("time").asText("")), description));
            });
            result = LogisticsResult.unavailable(nodes.isEmpty() ? "EMPTY" : "OK",
                nodes.isEmpty() ? "快递公司暂无轨迹，请稍后查看。" : "");
            result.setNodes(nodes);
            result.setStatus(switch (response.path("state").asText()) {
                case "0" -> "运输中"; case "1" -> "已揽收"; case "2" -> "运输异常";
                case "3" -> "已签收"; case "4" -> "退签"; case "5" -> "派送中";
                case "6" -> "退回中"; case "7" -> "转投"; case "8" -> "清关中";
                case "14" -> "已拒签"; default -> "物流更新";
            });
        } else if ("500".equals(code)) {
            result = LogisticsResult.unavailable("EMPTY", "快递公司暂无轨迹，请核对单号或稍后查看。");
        } else if ("408".equals(code)) {
            result = LogisticsResult.unavailable("PHONE_MISMATCH", "快递手机号核验未通过，请联系客服核对收件手机号。");
        } else {
            // Do not expose vendor errors, account identifiers or request credentials.
            result = LogisticsResult.unavailable("UNAVAILABLE", "暂时无法查询物流，可复制单号到快递官方渠道查询。");
        }
        result.setCarrier(carrier); result.setTrackingNumber(number);
        result.setQueriedAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return result;
    }
}
