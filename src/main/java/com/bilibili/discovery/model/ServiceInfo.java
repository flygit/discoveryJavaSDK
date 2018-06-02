package com.bilibili.discovery.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ServiceInfo {
    @SerializedName("zone_instances")
    private Map<String, List<InstanceInfo>> zoneInstances;

    @SerializedName("latest_timestamp")
    private long lastUpdateNanos;
}