package com.bilibili.discovery;

import com.bilibili.discovery.client.RequestClient;
import com.bilibili.discovery.config.DiscoveryConfig;
import com.bilibili.discovery.model.InstanceInfo;
import com.bilibili.discovery.model.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Discovery {
    private static final Logger LOGGER = LoggerFactory.getLogger("discovery");
    private RequestClient requestClient;

    public Discovery(DiscoveryConfig config) {
        requestClient = new RequestClient(config.getApps(), config.getNodeUrl());
        requestClient.start();
    }

    public Map<String, ServiceInfo> getServiceInfo() {
        return Collections.unmodifiableMap(requestClient.getServiceInfo());
    }

    public List<InstanceInfo> getInstanceInfo(String app, String zone) {
        try {
            return Collections.unmodifiableList(requestClient.getServiceInfo().get(app).getZoneInstances().get(zone));
        } catch (Exception e) {
            LOGGER.error("discovery client get instance info error.", e);
            return null;
        }
    }
}
