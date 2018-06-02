package com.bilibili.discovery.exception;

public class DiscoveryException extends Exception {

    public DiscoveryException(Throwable cause) {
        super(cause);
    }

    public DiscoveryException(String message) {
        super(message);
    }
}