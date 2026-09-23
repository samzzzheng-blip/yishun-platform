package com.techtron.onebook.module.app.service.logistics;

import lombok.Data;
import java.util.List;

@Data
public class LogisticsResult {
    private String availability;
    private String message;
    private String status;
    private String carrier;
    private String trackingNumber;
    private String queriedAt;
    private List<Node> nodes = List.of();

    public record Node(String time, String description) {}
    public static LogisticsResult unavailable(String availability, String message) {
        LogisticsResult result = new LogisticsResult();
        result.setAvailability(availability);
        result.setMessage(message);
        return result;
    }
}
