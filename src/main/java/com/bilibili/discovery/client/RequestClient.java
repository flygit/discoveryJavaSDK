package com.bilibili.discovery.client;

import com.bilibili.discovery.model.ServiceInfo;

import java.util.List;
import java.util.Map;

public class RequestClient {
    private NodeTask nodeTask;
    private PollTask pollTask;

    public RequestClient(List<String> apps, String nodeUrl) {
        this.nodeTask = new NodeTask(nodeUrl);
        this.pollTask = new PollTask(apps, nodeTask.getDiscoveryInfos());
    }

    public void start() {
        nodeTask.start();
        pollTask.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    private void close() {
        nodeTask.stop();
        pollTask.stop();
    }

    public Map<String, ServiceInfo> getServiceInfo() {
        return pollTask.getServiceInfoMap();
    }
}
