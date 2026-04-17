package com.engine.scheduler;

import com.engine.model.Task;
import com.engine.model.TaskStatus;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class PriorityScheduler {
    private final PriorityBlockingQueue<Task> readyQueue;
    private final Map<String, Task> allTasks;
    private final Map<String, List<String>> dependencyGraph; // taskId -> list of tasks that depend on it
    private final Map<String, Integer> dependencyCount; // taskId -> number of unfinished dependencies
    private final Map<String, Long> retryTimestamps; // taskId -> timestamp when retry should happen

    public PriorityScheduler() {
        this.readyQueue = new PriorityBlockingQueue<>();
        this.allTasks = new ConcurrentHashMap<>();
        this.dependencyGraph = new ConcurrentHashMap<>();
        this.dependencyCount = new ConcurrentHashMap<>();
        this.retryTimestamps = new ConcurrentHashMap<>();
    }

    public synchronized void submitTask(Task task) throws Exception {
        if (detectCycle(task)) {
            throw new Exception("Circular dependency detected for task: " + task.getId());
        }

        allTasks.put(task.getId(), task);
        int deps = task.getDependencies().size();
        dependencyCount.put(task.getId(), deps);

        for (String depId : task.getDependencies()) {
            dependencyGraph.computeIfAbsent(depId, k -> new ArrayList<>()).add(task.getId());
        }

        if (deps == 0) {
            task.setStatus(TaskStatus.READY);
            readyQueue.add(task);
        }
    }

    private boolean detectCycle(Task task) {
        // Simple DFS for cycle detection in DAG
        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();
        return hasCycle(task, visited, stack);
    }

    private boolean hasCycle(Task task, Set<String> visited, Set<String> stack) {
        if (stack.contains(task.getId()))
            return true;
        if (visited.contains(task.getId()))
            return false;

        visited.add(task.getId());
        stack.add(task.getId());

        for (String depId : task.getDependencies()) {
            Task depTask = allTasks.get(depId);
            if (depTask != null && hasCycle(depTask, visited, stack))
                return true;
        }

        stack.remove(task.getId());
        return false;
    }

    public Task getNextTask() {
        // Check for tasks that are ready and have passed their retry delay
        Task task = null;
        while ((task = readyQueue.peek()) != null) {
            Long retryTime = retryTimestamps.get(task.getId());
            if (retryTime == null || System.currentTimeMillis() >= retryTime) {
                readyQueue.poll();
                retryTimestamps.remove(task.getId());
                return task;
            } else {
                // Task still in backoff period, wait a bit
                break;
            }
        }
        return null;
    }

    public synchronized void completeTask(String taskId) {
        Task task = allTasks.get(taskId);
        if (task == null)
            return;

        task.setStatus(TaskStatus.COMPLETED);
        List<String> dependents = dependencyGraph.getOrDefault(taskId, new ArrayList<>());

        for (String depId : dependents) {
            int remaining = dependencyCount.get(depId) - 1;
            dependencyCount.put(depId, remaining);

            if (remaining == 0) {
                Task dependentTask = allTasks.get(depId);
                dependentTask.setStatus(TaskStatus.READY);
                readyQueue.add(dependentTask);
            }
        }
    }

    public void handleTaskFailure(String taskId, int maxRetries) {
        Task task = allTasks.get(taskId);
        if (task == null)
            return;

        if (task.getRetryCount() < maxRetries) {
            task.incrementRetryCount();
            task.setStatus(TaskStatus.READY);

            // Calculate exponential backoff: 2^retryCount seconds with jitter
            long backoffMs = (long) ((Math.pow(2, task.getRetryCount())) * 1000 + Math.random() * 1000);
            long retryTime = System.currentTimeMillis() + backoffMs;
            retryTimestamps.put(taskId, retryTime);

            readyQueue.add(task);
        } else {
            task.setStatus(TaskStatus.DEAD_LETTER);
        }
    }

    public void reassignTask(Task task) {
        task.setStatus(TaskStatus.READY);
        retryTimestamps.remove(task.getId());
        readyQueue.add(task);
    }

    public Map<String, Task> getAllTasks() {
        return new HashMap<>(allTasks);
    }

    public int getQueueSize() {
        return readyQueue.size();
    }

    public int getTotalTasksSubmitted() {
        return allTasks.size();
    }
}
