package com.bilibili.discovery;

import com.bilibili.discovery.bean.Instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Discovery {

    private DiscoveryClient client;
    private Dispatcher dispatcher = new Dispatcher();

    public Discovery(String apps) {
        client = new DiscoveryClient(Arrays.asList(apps.split(",")), dispatcher);
    }

    public Discovery(Set<String> apps) {
        client = new DiscoveryClient(new ArrayList<>(apps), dispatcher);
    }

    public void start() {
        client.start();
    }

    public void stop() {
        client.stop();
    }

    public void addListener(DiscoveryListener l) {
        dispatcher.addListener(l);
    }

    public void removeListener(DiscoveryListener l) {
        dispatcher.removeListener(l);
    }

    public List<Instance> getInstance(String app, String zone) {
        try {
            return Collections.unmodifiableList(client.getInstances().get(app).getZoneInstances().get(zone));
        } catch (Exception e) {
            return null;
        }
    }
}
