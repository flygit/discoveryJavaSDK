package com.bilibili.discovery.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

public class ENVConfig {
    private static final String ENV;
    private static final String ZONE;
    private static final String HOSTNAME;

    static {
        ZONE = Optional.ofNullable(System.getenv("ZONE"))
                .orElse(System.getProperty("zone"));
        ENV = Optional.ofNullable(System.getenv("DEPLOY_ENV"))
                .orElse(System.getProperty("deploy_env"));
        HOSTNAME = Optional.ofNullable(System.getenv("HOSTNAME"))
                .orElseGet(() -> {
                    try {
                        return InetAddress.getLocalHost().getHostName();
                    } catch (UnknownHostException e) {
                        return "";
                    }
                });
    }

    public static String getHOSTNAME() {
        return HOSTNAME;
    }

    public static String getENV() {
        return ENV;
    }

    public static String getZONE() {
        return ZONE;
    }
}