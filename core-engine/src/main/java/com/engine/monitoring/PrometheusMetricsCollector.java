package com.engine.monitoring;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simplified Prometheus-style Metrics Collector
 * Tracks system metrics for monitoring and observability
 */
public class PrometheusMetricsCollector {
    private final AtomicInteger activeTasksGauge;
    private final AtomicInteger completedTasksCounter;
    private final AtomicInteger failedTasksCounter;
    private final AtomicLong totalExecutionTimeMs;
    private final AtomicInteger workerCount;

    public PrometheusMetricsCollector() {
        this.activeTasksGauge = new AtomicInteger(0);
        this.completedTasksCounter = new AtomicInteger(0);
        this.failedTasksCounter = new AtomicInteger(0);
        this.totalExecutionTimeMs = new AtomicLong(0);
        this.workerCount = new AtomicInteger(0);
    }

    public void recordTaskStart() {
        activeTasksGauge.incrementAndGet();
    }

    public void recordTaskCompletion(long executionTimeMs) {
        activeTasksGauge.decrementAndGet();
        completedTasksCounter.incrementAndGet();
        totalExecutionTimeMs.addAndGet(executionTimeMs);
    }

    public void recordTaskFailure() {
        activeTasksGauge.decrementAndGet();
        failedTasksCounter.incrementAndGet();
    }

    public void recordWorkerRegistration() {
        workerCount.incrementAndGet();
    }

    public int getActiveTaskCount() {
        return activeTasksGauge.get();
    }

    public int getCompletedTaskCount() {
        return completedTasksCounter.get();
    }

    public int getFailedTaskCount() {
        return failedTasksCounter.get();
    }

    public int getWorkerCount() {
        return workerCount.get();
    }

    public double getAverageExecutionTime() {
        int completed = completedTasksCounter.get();
        if (completed == 0)
            return 0;
        return (double) totalExecutionTimeMs.get() / completed;
    }

    public void printMetricsReport() {
        System.out.println("\n===== Prometheus Metrics Report =====");
        System.out.println("Active Tasks: " + getActiveTaskCount());
        System.out.println("Completed Tasks: " + getCompletedTaskCount());
        System.out.println("Failed Tasks: " + getFailedTaskCount());
        System.out.println("Worker Count: " + getWorkerCount());
        System.out.println("Average Execution Time (ms): " + String.format("%.2f", getAverageExecutionTime()));
        System.out.println("=======================================\n");
    }
}
