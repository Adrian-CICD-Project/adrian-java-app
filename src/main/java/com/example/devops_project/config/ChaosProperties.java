package com.example.devops_project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "coffee.chaos")
public class ChaosProperties {

    /** Globalny włącznik endpointów /api/chaos/** */
    private boolean enabled = true;

    /** Maksymalne opóźnienie dla slow-brew (ms) */
    private long maxDelayMs = 10000;

    /** Maksymalna łączna ilość zaalokowanej pamięci (MB) */
    private int maxMemoryMb = 200;

    /** Maksymalny czas palenia CPU (s) */
    private int maxCpuSeconds = 60;

    /** Maksymalna liczba wątków palących CPU */
    private int maxCpuThreads = 4;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getMaxDelayMs() {
        return maxDelayMs;
    }

    public void setMaxDelayMs(long maxDelayMs) {
        this.maxDelayMs = maxDelayMs;
    }

    public int getMaxMemoryMb() {
        return maxMemoryMb;
    }

    public void setMaxMemoryMb(int maxMemoryMb) {
        this.maxMemoryMb = maxMemoryMb;
    }

    public int getMaxCpuSeconds() {
        return maxCpuSeconds;
    }

    public void setMaxCpuSeconds(int maxCpuSeconds) {
        this.maxCpuSeconds = maxCpuSeconds;
    }

    public int getMaxCpuThreads() {
        return maxCpuThreads;
    }

    public void setMaxCpuThreads(int maxCpuThreads) {
        this.maxCpuThreads = maxCpuThreads;
    }
}
