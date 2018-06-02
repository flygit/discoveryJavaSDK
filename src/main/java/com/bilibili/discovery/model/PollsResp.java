package com.bilibili.discovery.model;

import lombok.Data;

import java.util.Map;

@Data
public class PollsResp {
    private Integer code;
    private String message;
    private Map<String, ServiceInfo> data;
}
