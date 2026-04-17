package com.engine.model;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker {
    private final String id;
    private long lastHeartbeat;
    private boolean isHealthy;
    private double cpuUsage;
    private double memoryUsage;
    private final AtomicInteger activeTaskCount;
    private final int maxCapacity;

    public Worker() {
        this.id = "worker-" + UUID.randomUUID().toString().substring(0, 8);
        this.lastHeartbeat = System.currentTimeMillis();
        this.isHealthy = true;
        this.cpuUsage = 0.0;
        this.memoryUsage = 0.0;
        this.activeTaskCount = new AtomicInteger(0);
        this.maxCapacity = 10; // Default max concurrent tasks
    }

    public Worker(int maxCapacity) {
        this.id = "worker-" + UUID.randomUUID().toString().substring(0, 8);
        this.lastHeartbeat = System.currentTimeMillis();
        this.isHealthy = true;
        this.cpuUsage = 0.0;
        this.memoryUsage = 0.0;
        this.activeTaskCount = new AtomicInteger(0);
        this.maxCapacity = maxCapacity;
    }

    public String getId() {
        return id;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public void setHealthy(boolean healthy) {
        isHealthy = healthy;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public int getActiveTaskCount() {
        return activeTaskCount.get();
    }

    public void incrementActiveTaskCount() {
        activeTaskCount.incrementAndGet();
    }

    public void decrementActiveTaskCount() {
        activeTaskCount.decrementAndGet();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public boolean canAcceptTask() {
        return activeTaskCount.get() < maxCapacity;
    }

    public double getResourceUtilization() {
        return (cpuUsage + memoryUsage) / 2.0;
    }
}
