package com.bilibili.discovery.client;

import com.bilibili.discovery.config.ENVConfig;
import com.bilibili.discovery.exception.DiscoveryException;
import com.bilibili.discovery.exception.DiscoveryRequestException;
import com.bilibili.discovery.model.DiscoveryInfo;
import com.bilibili.discovery.model.NodeResp;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

class NodeTask extends BaseTask {
    private Request nodeRequest;

    private volatile List<DiscoveryInfo> discoveryInfos = new ArrayList<>();
    private Thread currentThread;

    NodeTask(String nodeUrl) {
        nodeRequest = new Request.Builder().url(nodeUrl).build();
    }

    void start() {
        currentThread = new Thread(() -> {
            long last = 0;
            while (isRunning()) {
                // TODO backoff
                long duration = currentTimestamp() - last;
                if (duration < 30L) {
                    try {
                        TimeUnit.SECONDS.sleep(30L - duration);
                    } catch (InterruptedException ignored) {
                        continue;
                    }
                }
                try {
                    fetch();
                    TimeUnit.MINUTES.sleep(3);
                } catch (InterruptedException ignored) {
                } catch (Exception e) {
                    getLogger().warn("nodes error", e);
                    last = currentTimestamp();
                }
            }
        }, "discovery-nodes");
        currentThread.start();
    }

    void stop() {
        setRunning(false);
        currentThread.interrupt();
    }

    List<DiscoveryInfo> getDiscoveryInfos() {
        return discoveryInfos;
    }

    private void fetch() throws Exception {
        List<DiscoveryInfo> all = request();
        String zone = ENVConfig.getZONE();
        List<DiscoveryInfo> current = new ArrayList<>();
        List<DiscoveryInfo> other = new ArrayList<>();
        for (DiscoveryInfo node : all) {
            if (zone != null && zone.equals(node.getZone())) {
                current.add(node);
            } else {
                other.add(node);
            }
        }
        List<DiscoveryInfo> nodes = new ArrayList<>(all.size());
        Collections.shuffle(current);
        Collections.shuffle(other);
        nodes.addAll(current);
        nodes.addAll(other);

        discoveryInfos = nodes;
    }

    private List<DiscoveryInfo> request() throws DiscoveryRequestException, DiscoveryException {
        getLogger().info("request:{}", nodeRequest.url().toString());
        try (Response response = getHttpClient().newCall(nodeRequest).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                getLogger().info("nodes response: {}", json);
                NodeResp nodeResp = getGson().fromJson(json, NodeResp.class);
                if (nodeResp.getCode() == 0) {
                    return nodeResp.getData();
                }
                throw new DiscoveryRequestException(nodeResp.getCode(), nodeResp.getMessage());
            }
            throw new DiscoveryRequestException(500, response.toString());
        } catch (DiscoveryRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new DiscoveryException(e);
        }
    }
}
