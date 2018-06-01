package com.bilibili.discovery;

import com.bilibili.discovery.bean.Node;
import com.bilibili.discovery.bean.Service;

import java.util.List;
import java.util.Map;

class Repository {

    private volatile Map<String, Service> services;
    private volatile List<Node> discoveryNodes;


    void onPollsResponse(Map<String, Service> polls) {
        this.services = polls;
    }

    void onNodeResponse(List<Node> nodes) {
        this.discoveryNodes = nodes;
    }

    public List<Node> getDiscoveryNodes() {
        return discoveryNodes;
    }

    public Node getDiscoveryNodes(int idx) {
        return discoveryNodes.get(idx % discoveryNodes.size());
    }

    public Map<String, Service> getServices() {
        return services;
    }

}