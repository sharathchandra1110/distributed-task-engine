package com.engine.schedule;

import com.engine.core.EngineApp;
import java.util.*;

/**
 * Simplified Task Scheduler Service
 * Provides scheduling for recurring tasks using simple scheduling mechanism
 */
public class TaskSchedulerService {
    private final List<ScheduledTaskDefinition> scheduledTasks;
    private volatile boolean running;

    public TaskSchedulerService() {
        this.scheduledTasks = Collections.synchronizedList(new ArrayList<>());
        this.running = false;
    }

    /**
     * Schedule a task to run repeatedly at specified intervals
     */
    public void scheduleTaskAtFixedRate(String jobName, String taskName, int priority, long intervalMs) {
        ScheduledTaskDefinition task = new ScheduledTaskDefinition(jobName, taskName, priority, intervalMs);
        scheduledTasks.add(task);
        System.out.println("Scheduled task: " + jobName + " (every " + intervalMs + "ms)");
    }

    /**
     * Common interval patterns
     */
    public static class Intervals {
        public static final long EVERY_MINUTE = 60_000;
        public static final long EVERY_5_MINUTES = 300_000;
        public static final long EVERY_HOUR = 3_600_000;
        public static final long EVERY_DAY = 86_400_000;
    }

    /**
     * Get all scheduled tasks
     */
    public List<ScheduledTaskDefinition> getScheduledTasks() {
        return new ArrayList<>(scheduledTasks);
    }

    /**
     * Cancel all scheduled tasks
     */
    public void stopScheduler() {
        running = false;
        System.out.println("Task scheduler stopped");
    }

    public static class ScheduledTaskDefinition {
        private final String jobName;
        private final String taskName;
        private final int priority;
        private final long intervalMs;
        private long lastExecutionTime;

        public ScheduledTaskDefinition(String jobName, String taskName, int priority, long intervalMs) {
            this.jobName = jobName;
            this.taskName = taskName;
            this.priority = priority;
            this.intervalMs = intervalMs;
            this.lastExecutionTime = System.currentTimeMillis();
        }

        public String getJobName() {
            return jobName;
        }

        public String getTaskName() {
            return taskName;
        }

        public int getPriority() {
            return priority;
        }

        public long getIntervalMs() {
            return intervalMs;
        }

        public long getLastExecutionTime() {
            return lastExecutionTime;
        }

        public void setLastExecutionTime(long time) {
            this.lastExecutionTime = time;
        }

        public boolean isDueForExecution() {
            return (System.currentTimeMillis() - lastExecutionTime) >= intervalMs;
        }
    }
}
