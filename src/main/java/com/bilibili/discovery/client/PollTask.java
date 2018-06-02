package com.bilibili.discovery.client;

import com.bilibili.discovery.config.ENVConfig;
import com.bilibili.discovery.exception.DiscoveryException;
import com.bilibili.discovery.exception.DiscoveryRequestException;
import com.bilibili.discovery.model.DiscoveryInfo;
import com.bilibili.discovery.model.PollsResp;
import com.bilibili.discovery.model.ServiceInfo;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class PollTask extends BaseTask {

    private int idx;
    private Thread currentThread;
    private List<String> apps;
    private List<DiscoveryInfo> discoveryInfos;
    private Map<String, Long> lastUpdateMap = new HashMap<>();
    private volatile Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

    PollTask(List<String> apps, List<DiscoveryInfo> discoveryInfos) {
        this.apps = apps;
        this.discoveryInfos = discoveryInfos;
        for (String app : this.apps) {
            lastUpdateMap.put(app, 0L);
        }
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
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
                try {
                    fetch();
                } catch (Exception e) {
                    last = currentTimestamp();
                    getLogger().warn("polls error", e);
                }
            }
        }, "discovery-polls");
    }

    void stop() {
        setRunning(false);
        currentThread.interrupt();
    }

    Map<String, ServiceInfo> getServiceInfoMap() {
        return serviceInfoMap;
    }

    private void fetch() throws Exception {
        Map<String, ServiceInfo> infoMap = request();
        if (infoMap == null || infoMap.size() == 0) {
            return;
        }
        for (String app : apps) {
            lastUpdateMap.put(app, infoMap.get(app).getLastUpdateNanos());
        }
        // TODO return value
        serviceInfoMap = infoMap;
    }

    private Map<String, ServiceInfo> request() throws DiscoveryRequestException, DiscoveryException {
        DiscoveryInfo node = getDiscoveryNode(idx);
        if (node == null) {
            throw new DiscoveryException("can not find available node");
        }
        HttpUrl.Builder newBuilder = HttpUrl.parse(String.format("http://%s/discovery/polls?", node.getAddr())).newBuilder();
        for (Map.Entry<String, Long> entry : lastUpdateMap.entrySet()) {
            newBuilder.addQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
        }
        HttpUrl httpUrl = newBuilder
                .addQueryParameter("env", ENVConfig.getENV())
                .addQueryParameter("hostname", ENVConfig.getHOSTNAME())
                .build();
        Request request = new Request.Builder().url(httpUrl).get().build();
        boolean isSuccess = false;
        try (Response response = getHttpClient().newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                PollsResp instantResp = getGson().fromJson(json, PollsResp.class);
                // -304 not modified, response data field is null
                if (instantResp.getCode() == 0 || instantResp.getCode() == -304) {
                    isSuccess = true;
                    return instantResp.getData();
                }
                getLogger().debug("polls request {} response: {}", httpUrl.toString(), json);
                throw new DiscoveryException(String.format("discovery server error(code:%d, msg:%s)", instantResp.getCode(), instantResp.getMessage()));
            } else {
                throw new DiscoveryException(String.format("discovery request error(http status:%d, msg:%s)", response.code(), response.message()));
            }
        } catch (DiscoveryException e) {
            throw e;
        } catch (Exception e) {
            throw new DiscoveryException(e);
        } finally {
            if (!isSuccess) {
                idx++;
                idx = idx > Integer.MAX_VALUE - 1 ? 0 : idx;
            }
        }
    }

    private DiscoveryInfo getDiscoveryNode(int idx) {
        return discoveryInfos.get(idx);
    }
}
