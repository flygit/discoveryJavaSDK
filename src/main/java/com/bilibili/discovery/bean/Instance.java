package com.bilibili.discovery.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Instance {
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getTreeid() {
        return treeid;
    }

    public void setTreeid(String treeid) {
        this.treeid = treeid;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getAddrs() {
        return addrs;
    }

    public void setAddrs(List<String> addrs) {
        this.addrs = addrs;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Map getMetadata() {
        return metadata;
    }

    public void setMetadata(Map metadata) {
        this.metadata = metadata;
    }

    public Long getRegTimestamp() {
        return regTimestamp;
    }

    public void setRegTimestamp(Long regTimestamp) {
        this.regTimestamp = regTimestamp;
    }

    public Long getUpTimestamp() {
        return upTimestamp;
    }

    public void setUpTimestamp(Long upTimestamp) {
        this.upTimestamp = upTimestamp;
    }

    public Long getRenewTimestamp() {
        return renewTimestamp;
    }

    public void setRenewTimestamp(Long renewTimestamp) {
        this.renewTimestamp = renewTimestamp;
    }

    public Long getDirtyTimestamp() {
        return dirtyTimestamp;
    }

    public void setDirtyTimestamp(Long dirtyTimestamp) {
        this.dirtyTimestamp = dirtyTimestamp;
    }

    public Long getLatestTimestamp() {
        return latestTimestamp;
    }

    public void setLatestTimestamp(Long latestTimestamp) {
        this.latestTimestamp = latestTimestamp;
    }
}

