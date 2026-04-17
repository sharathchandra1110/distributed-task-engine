package com.engine.metrics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TaskMetrics {
    private final Map<String, TaskExecutionStats> taskStats;
    private final AtomicInteger totalTasksProcessed;
    private final AtomicInteger totalTasksFailed;
    private final AtomicLong totalExecutionTime;
    private final long startTime;

    public static class TaskExecutionStats {
        private String taskId;
        private String taskName;
        private long startTime;
        private long endTime;
        private long duration;
        private String status;
        private int retryCount;
        private long createdAt;

        public TaskExecutionStats(String taskId, String taskName, long createdAt) {
            this.taskId = taskId;
            this.taskName = taskName;
            this.createdAt = createdAt;
            this.startTime = System.currentTimeMillis();
        }

        public void markComplete(String status, int retryCount) {
            this.endTime = System.currentTimeMillis();
            this.duration = this.endTime - this.startTime;
            this.status = status;
            this.retryCount = retryCount;
        }

        public String getTaskId() {
            return taskId;
        }

        public String getTaskName() {
            return taskName;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public long getDuration() {
            return duration;
        }

        public String getStatus() {
            return status;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }

    public TaskMetrics() {
        this.taskStats = new ConcurrentHashMap<>();
        this.totalTasksProcessed = new AtomicInteger(0);
        this.totalTasksFailed = new AtomicInteger(0);
        this.totalExecutionTime = new AtomicLong(0);
        this.startTime = System.currentTimeMillis();
    }

    public void recordTaskStart(String taskId, String taskName, long createdAt) {
        taskStats.put(taskId, new TaskExecutionStats(taskId, taskName, createdAt));
    }

    public void recordTaskCompletion(String taskId, String status, int retryCount) {
        TaskExecutionStats stats = taskStats.get(taskId);
        if (stats != null) {
            stats.markComplete(status, retryCount);
            totalExecutionTime.addAndGet(stats.duration);
            totalTasksProcessed.incrementAndGet();
            if ("FAILED".equals(status)) {
                totalTasksFailed.incrementAndGet();
            }
        }
    }

    public double getAverageExecutionTime() {
        int processed = totalTasksProcessed.get();
        if (processed == 0)
            return 0;
        return (double) totalExecutionTime.get() / processed;
    }

    public double getSuccessRate() {
        int processed = totalTasksProcessed.get();
        if (processed == 0)
            return 0;
        return ((double) (processed - totalTasksFailed.get()) / processed) * 100;
    }

    public long getSystemUptime() {
        return System.currentTimeMillis() - startTime;
    }

    public Map<String, Object> getMetricsReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("total_tasks_processed", totalTasksProcessed.get());
        report.put("total_tasks_failed", totalTasksFailed.get());
        report.put("success_rate_percent", String.format("%.2f%%", getSuccessRate()));
        report.put("average_execution_time_ms", String.format("%.2f", getAverageExecutionTime()));
        report.put("total_execution_time_ms", totalExecutionTime.get());
        report.put("system_uptime_ms", getSystemUptime());
        report.put("active_tasks", taskStats.size());
        return report;
    }

    public Map<String, TaskExecutionStats> getAllTaskStats() {
        return new LinkedHashMap<>(taskStats);
    }

    public TaskExecutionStats getTaskStats(String taskId) {
        return taskStats.get(taskId);
    }
}
