package com.bilibili.discovery.event;

import com.bilibili.discovery.bean.Service;

import java.util.Map;

public class PollsEvent extends DiscoveryEvent<Map<String, Service>> {

    public PollsEvent(Map<String, Service> map) {
        setMessage(map);
    }


}