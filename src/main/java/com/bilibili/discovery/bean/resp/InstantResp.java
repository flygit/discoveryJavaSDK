package com.bilibili.discovery.bean.resp;

import com.bilibili.discovery.bean.Service;

import java.util.Map;

public class InstantResp {
    private Integer code;
    private String message;
    private Map<String, Service> data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Service> getData() {
        return data;
    }

    public void setData(Map<String, Service> data) {
        this.data = data;
    }
}
