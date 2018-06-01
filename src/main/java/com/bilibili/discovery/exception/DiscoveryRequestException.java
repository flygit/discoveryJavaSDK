package com.bilibili.discovery.exception;

public class DiscoveryRequestException extends Exception {
    private int code;

    public DiscoveryRequestException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}