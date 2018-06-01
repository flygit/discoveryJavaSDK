package com.bilibili.discovery;

import com.bilibili.discovery.bean.Node;
import com.bilibili.discovery.bean.Service;
import com.bilibili.discovery.config.Config;
import com.bilibili.discovery.event.PollsEvent;
import com.bilibili.discovery.exception.DiscoveryException;
import com.bilibili.discovery.exception.DiscoveryRequestException;
import com.bilibili.discovery.bean.resp.InstantResp;
import com.bilibili.discovery.bean.resp.NodeResp;
import com.google.gson.Gson;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

class DiscoveryClient {

    private final Logger logger = LoggerFactory.getLogger(DiscoveryClient.class);

    private static Config config;

    private static final Gson gson = new Gson();

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(61, TimeUnit.SECONDS)
            .connectTimeout(3, TimeUnit.SECONDS)
            .build();

    private Repository repository = new Repository();
    private NodeThread nodeThread;
    private PollsThread pollsThread;

    private static final String ENV;
    private static final String ZONE;
    private static final String HOST_NAME;

    static {
        ZONE = Optional.ofNullable(System.getenv("ZONE"))
                .orElse(System.getProperty("zone"));
        ENV = Optional.ofNullable(System.getenv("DEPLOY_ENV"))
                .orElse(System.getProperty("deploy_env"));
        HOST_NAME = Optional.ofNullable(System.getenv("HOSTNAME"))
                .orElseGet(() -> {
                    try {
                        return InetAddress.getLocalHost().getHostName();
                    } catch (UnknownHostException e) {
                        return "";
                    }
                });
    }

    DiscoveryClient(List<String> apps, Dispatcher dispatcher) {
        nodeThread = new NodeThread(repository);
        pollsThread = new PollsThread(repository, dispatcher, apps);
    }

    void start() {
        try {
            nodeThread.startWorker();
            pollsThread.startWorker();
        } catch (Exception e) {
            if (nodeThread.running) {
                nodeThread.stopWorker();
            }
            if (pollsThread.running) {
                pollsThread.stopWorker();
            }
            logger.error("error occurred when starting discovery client", e);
            throw e;
        }
    }

    void stop() {
        nodeThread.stopWorker();
        pollsThread.stopWorker();
    }


    Map<String, Service> getInstances() {
        return repository.getServices();
    }

    private static long currentTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }

    static class PollsThread extends Thread {
        private final Logger logger = LoggerFactory.getLogger(PollsThread.class);
        private boolean running = false;

        private int idx;

        private Repository repository;
        private Dispatcher dispatcher;
        private List<String> apps;
        private Map<String, Long> lastUpdateMap = new HashMap<>();

        public PollsThread(Repository repository, Dispatcher dispatcher, List<String> apps) {
            super("discovery-polls");
            this.repository = repository;
            this.dispatcher = dispatcher;
            this.apps = apps;
            for (String app : this.apps) {
                lastUpdateMap.put(app, 0L);
            }
        }

        void startWorker() {
            try {
                fetch();
            } catch (Exception e) {
                // panic
                throw new RuntimeException(e);
            }
            running = true;
            start();
        }

        void stopWorker() {
            running = false;
            interrupt();
        }

        @Override
        public void run() {
            long last = 0;
            while (running) {
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
                    logger.warn("polls error", e);
                }
            }
        }

        void fetch() throws Exception {
            Map<String, Service> serviceMap = request();
            if (serviceMap == null) {
                return;
            }
            for (String app : apps) {
                lastUpdateMap.put(app, serviceMap.get(app).getLastUpdateNanos());
            }
            repository.onPollsResponse(serviceMap);
            dispatcher.dispatchEvent(new PollsEvent(serviceMap));
        }

        Map<String, Service> request() throws DiscoveryRequestException, DiscoveryException {

            List<String> appParams = new ArrayList<>();
            List<String> lastTimestampParams = new ArrayList<>();
            for (Map.Entry<String, Long> entry : lastUpdateMap.entrySet()) {
                appParams.add(entry.getKey());
                lastTimestampParams.add(String.valueOf(entry.getValue()));
            }
            Node node = repository.getDiscoveryNodes(idx);
            if (node == null) {
                throw new DiscoveryException("can not find available node");
            }
            HttpUrl httpUrl = HttpUrl.parse(String.format("http://%s/discovery/polls", node.getAddr()))
                    .newBuilder()
                    .addQueryParameter("appid", String.join(",", appParams))
                    .addQueryParameter("env", ENV)
                    .addQueryParameter("hostname", HOST_NAME)
                    .addQueryParameter("latest_timestamp", String.join(",", lastTimestampParams))
                    .build();
            Request request = new Request.Builder().url(httpUrl).get().build();
            boolean isSuccess = false;
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    InstantResp instantResp = gson.fromJson(json, InstantResp.class);
                    if (instantResp.getCode() == 0) {
                        isSuccess = true;
                        return instantResp.getData();
                    } else if (instantResp.getCode() == -304) {
                        // not modified, nothing to save
                        return null;
                    } else {
                        logger.debug("polls request {} response: {}", httpUrl.toString(), json);
                        throw new DiscoveryRequestException(instantResp.getCode(), instantResp.getMessage());
                    }
                } else {
                    throw new DiscoveryRequestException(response.code(), response.message());
                }
            } catch (DiscoveryRequestException e) {
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
    }

    static class NodeThread extends Thread {
        private final Logger logger = LoggerFactory.getLogger(NodeThread.class);
        private static final Request nodeRequest = new Request.Builder().url(config.getNodeUrl()).build();

        private boolean running = false;

        private Repository repository;

        public NodeThread(Repository repository) {
            super("discovery-nodes");
            this.repository = repository;
        }

        void startWorker() {
            try {
                fetch();
            } catch (Exception e) {
                // panic
                throw new RuntimeException(e);
            }
            running = true;
            start();
        }

        void stopWorker() {
            running = false;
            interrupt();
        }

        @Override
        public void run() {
            long last = 0;
            while (running) {
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
                    logger.warn("nodes error", e);
                    last = currentTimestamp();
                }
            }
        }

        void fetch() throws Exception {
            List<Node> all = request();
            String zone = ZONE;
            List<Node> current = new ArrayList<>();
            List<Node> other = new ArrayList<>();
            for (Node node : all) {
                if (zone != null && zone.equals(node.getZone())) {
                    current.add(node);
                } else {
                    other.add(node);
                }
            }
            List<Node> nodes = new ArrayList<>(all.size());
            Collections.shuffle(current);
            Collections.shuffle(other);
            nodes.addAll(current);
            nodes.addAll(other);
            repository.onNodeResponse(nodes);
        }

        List<Node> request() throws DiscoveryRequestException, DiscoveryException {
            logger.info("request:{}", nodeRequest.url().toString());
            try (Response response = httpClient.newCall(nodeRequest).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    logger.info("nodes response: {}", json);
                    NodeResp nodeResp = gson.fromJson(json, NodeResp.class);
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
}
