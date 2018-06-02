package com.bilibili.discovery.client;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

class BaseTask {
    private final Logger LOGGER = LoggerFactory.getLogger("discovery");
    private final Gson GSON = new Gson();
    private final OkHttpClient HTTPCLIENT = new OkHttpClient.Builder()
            .readTimeout(61, TimeUnit.SECONDS)
            .connectTimeout(3, TimeUnit.SECONDS)
            .build();

    private boolean running;

    Logger getLogger() {
        return LOGGER;
    }

    Gson getGson() {
        return GSON;
    }

    OkHttpClient getHttpClient() {
        return HTTPCLIENT;
    }

    long currentTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }

    boolean isRunning() {
        return running;
    }

    void setRunning(boolean running) {
        this.running = running;
    }
}
