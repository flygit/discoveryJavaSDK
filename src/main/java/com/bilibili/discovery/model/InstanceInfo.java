package com.bilibili.discovery.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InstanceInfo {
    private String region;
    private String zone;
    private String env;
    private String appid;
    private String treeid;
    private String hostname;
    private String color;
    private String version;
    private List<String> addrs;
    private Integer status;
    private Map metadata;

    @SerializedName("reg_timestamp")
    private Long regTimestamp;
    @SerializedName("up_timestamp")
    private Long upTimestamp;
    @SerializedName("renew_timestamp")
    private Long renewTimestamp;
    @SerializedName("dirty_timestamp")
    private Long dirtyTimestamp;
    @SerializedName("latest_timestamp")
    private Long latestTimestamp;
}

