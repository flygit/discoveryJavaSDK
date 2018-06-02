package com.bilibili.discovery.model;

import lombok.Data;

import java.util.List;

@Data
public class NodeResp {
    private Integer code;
    private String message;
    private List<DiscoveryInfo> data;
}
