package com.bilibili.discovery.event;

public abstract class DiscoveryEvent<E> {
    private E message;

    public E getMessage() {
        return message;
    }

    public void setMessage(E message) {
        this.message = message;
    }
}