package com.engine.batch;

import com.engine.model.Task;
import java.util.*;
import java.util.stream.Collectors;

public class TaskBatchService {
    private static final int DEFAULT_BATCH_SIZE = 50;
    private final int batchSize;

    public TaskBatchService() {
        this.batchSize = DEFAULT_BATCH_SIZE;
    }

    public TaskBatchService(int batchSize) {
        this.batchSize = Math.max(1, batchSize);
    }

    /**
     * Group tasks by priority level
     */
    public Map<Integer, List<Task>> groupByPriority(List<Task> tasks) {
        return tasks.stream()
                .collect(Collectors.groupingBy(Task::getPriority));
    }

    /**
     * Create batches of tasks for efficient processing
     */
    public List<List<Task>> createBatches(List<Task> tasks) {
        List<List<Task>> batches = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, tasks.size());
            batches.add(new ArrayList<>(tasks.subList(i, end)));
        }
        return batches;
    }

    /**
     * Optimize batch execution order by priority and dependencies
     */
    public List<Task> optimizeBatchOrder(List<Task> batch) {
        return batch.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Filter tasks by status
     */
    public List<Task> filterByStatus(List<Task> tasks, String status) {
        return tasks.stream()
                .filter(task -> task.getStatus().toString().equals(status))
                .collect(Collectors.toList());
    }

    /**
     * Group tasks by assigned worker for locality optimization
     */
    public Map<String, List<Task>> groupByWorker(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getAssignedWorkerId() != null)
                .collect(Collectors.groupingBy(Task::getAssignedWorkerId));
    }

    /**
     * Calculate batch statistics
     */
    public Map<String, Object> getBatchStats(List<Task> batch) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total_tasks", batch.size());
        stats.put("avg_priority", batch.stream().mapToInt(Task::getPriority).average().orElse(0.0));
        stats.put("tasks_with_dependencies", batch.stream().filter(t -> !t.getDependencies().isEmpty()).count());
        stats.put("max_retries", batch.stream().mapToInt(Task::getRetryCount).max().orElse(0));
        return stats;
    }
}
