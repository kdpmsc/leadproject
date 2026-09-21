package com.leadproject.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CampaignCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    private Long playbookVersionId;

    private List<Long> leadIds;

    private CallSchedule callSchedule;
    private RetryPolicy retryPolicy;

    @Data
    public static class CallSchedule {
        private String timezone = "Asia/Dubai";
        private List<String> days;
        private String start;
        private String end;
    }

    @Data
    public static class RetryPolicy {
        private Integer maxAttempts = 2;
        private Integer minimumHoursBetweenAttempts = 24;
    }
}
