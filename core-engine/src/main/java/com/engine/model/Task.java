package com.engine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Task implements Comparable<Task> {
    private final String id;
    private final String name;
    private final int priority; // Lower value = Higher priority
    private final List<String> dependencies;
    private TaskStatus status;
    private String assignedWorkerId;
    private int retryCount;
    private final long createdAt;
    private long startedAt;
    private long timeoutMs; // Task timeout in milliseconds, 0 = no timeout
    private String executionContext; // Additional metadata for task execution

    public Task(String name, int priority) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.priority = priority;
        this.dependencies = new ArrayList<>();
        this.status = TaskStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = System.currentTimeMillis();
        this.timeoutMs = 0; // Default: no timeout
    }

    public Task(String name, int priority, long timeoutMs) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.priority = priority;
        this.dependencies = new ArrayList<>();
        this.status = TaskStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = System.currentTimeMillis();
        this.timeoutMs = timeoutMs;
    }

    public void addDependency(String taskId) {
        dependencies.add(taskId);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getAssignedWorkerId() {
        return assignedWorkerId;
    }

    public void setAssignedWorkerId(String assignedWorkerId) {
        this.assignedWorkerId = assignedWorkerId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(String executionContext) {
        this.executionContext = executionContext;
    }

    public boolean isTimedOut() {
        if (timeoutMs <= 0)
            return false;
        if (startedAt == 0)
            return false;
        return (System.currentTimeMillis() - startedAt) > timeoutMs;
    }

    @Override
    public int compareTo(Task other) {
        if (this.priority != other.priority) {
            return Integer.compare(this.priority, other.priority);
        }
        return Long.compare(this.createdAt, other.createdAt);
    }
}
