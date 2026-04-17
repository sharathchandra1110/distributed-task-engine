package com.engine.worker;

import com.engine.model.Worker;
import com.engine.model.Task;
import com.engine.scheduler.PriorityScheduler;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WorkerManager {
    private final Map<String, Worker> workers;
    private final Map<String, Task> activeAssignments; // workerId -> Task
    private final PriorityScheduler scheduler;
    private final long timeoutThreshold;
    private final ScheduledExecutorService monitor;

    public WorkerManager(PriorityScheduler scheduler, long timeoutThresholdMs) {
        this.workers = new ConcurrentHashMap<>();
        this.activeAssignments = new ConcurrentHashMap<>();
        this.scheduler = scheduler;
        this.timeoutThreshold = timeoutThresholdMs;
        this.monitor = Executors.newSingleThreadScheduledExecutor();
        startMonitoring();
    }

    public void registerWorker(Worker worker) {
        workers.put(worker.getId(), worker);
    }

    public void updateHeartbeat(String workerId) {
        Worker worker = workers.get(workerId);
        if (worker != null) {
            worker.updateHeartbeat();
            worker.setHealthy(true);
        }
    }

    public void assignTask(String workerId, Task task) {
        activeAssignments.put(workerId, task);
        task.setAssignedWorkerId(workerId);
    }

    private void startMonitoring() {
        monitor.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (Worker worker : workers.values()) {
                if (now - worker.getLastHeartbeat() > timeoutThreshold) {
                    handleWorkerFailure(worker);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private synchronized void handleWorkerFailure(Worker worker) {
        if (!worker.isHealthy())
            return;

        worker.setHealthy(false);
        System.out.println("Worker failed: " + worker.getId() + ". Reassigning tasks...");

        Task assignedTask = activeAssignments.remove(worker.getId());
        if (assignedTask != null) {
            scheduler.reassignTask(assignedTask);
        }
    }

    public void stop() {
        monitor.shutdown();
    }

    public java.util.List<Worker> getWorkers() {
        return new java.util.ArrayList<>(workers.values());
    }
}
