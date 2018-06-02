package com.bilibili.discovery.config;

import lombok.Data;

import java.util.List;

@Data
public class DiscoveryConfig {
    private String nodeUrl;
    private List<String> apps;
}
