package com.bilibili.discovery;

import com.bilibili.discovery.event.DiscoveryEvent;

public interface DiscoveryListener {
    void onEvent(DiscoveryEvent event);
}