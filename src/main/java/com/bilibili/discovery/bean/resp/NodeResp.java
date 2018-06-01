package com.bilibili.discovery.bean.resp;

import com.bilibili.discovery.bean.Node;

import java.util.List;

public class NodeResp {
    private Integer code;
    private String message;
    private List<Node> data;

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

    public List<Node> getData() {
        return data;
    }

    public void setData(List<Node> data) {
        this.data = data;
    }
}
