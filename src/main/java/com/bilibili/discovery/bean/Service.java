package com.bilibili.discovery.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Service {
    @SerializedName("zone_instances")
    private Map<String, List<Instance>> zoneInstances;

    @SerializedName("latest_timestamp")
    private long lastUpdateNanos;

    public Map<String, List<Instance>> getZoneInstances() {
        return zoneInstances;
    }

    public void setZoneInstances(Map<String, List<Instance>> zoneInstances) {
        this.zoneInstances = zoneInstances;
    }

    public long getLastUpdateNanos() {
        return lastUpdateNanos;
    }

    public void setLastUpdateNanos(long lastUpdateNanos) {
        this.lastUpdateNanos = lastUpdateNanos;
    }
}