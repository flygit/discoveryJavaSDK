package com.bilibili.discovery;

import com.bilibili.discovery.event.DiscoveryEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class Dispatcher {

    private List<DiscoveryListener> listeners = new CopyOnWriteArrayList<>();
    private ThreadPoolExecutor dispatcherPool = new ThreadPoolExecutor(0,
            Runtime.getRuntime().availableProcessors() * 2,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new ThreadFactory() {
        private AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, String.format("discovery-dispatcher-#%d", counter.getAndIncrement()));
        }
    });

    void dispatchEvent(DiscoveryEvent e) {
        listeners.forEach(l -> dispatcherPool.submit(() -> l.onEvent(e)));
    }

    public void addListener(DiscoveryListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DiscoveryListener listener) {
        listeners.remove(listener);
    }
}