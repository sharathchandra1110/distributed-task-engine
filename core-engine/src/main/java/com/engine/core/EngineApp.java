package com.engine.core;

import com.engine.model.Task;
import com.engine.model.Worker;
import com.engine.scheduler.PriorityScheduler;
import com.engine.worker.WorkerManager;
import com.engine.metrics.TaskMetrics;
import com.engine.dlq.DeadLetterQueue;
import com.engine.batch.TaskBatchService;
import com.engine.persistence.TaskDatabase;

public class EngineApp {
    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("Distributed Task Engine - Feature Demo");
            System.out.println("========================================\n");

            // Feature 1 & 4: Task Metrics + Timeout Management
            System.out.println("1. Initializing components with metrics and timeout management...");
            PriorityScheduler scheduler = new PriorityScheduler();
            WorkerManager workerManager = new WorkerManager(scheduler, 30000);
            TaskMetrics metrics = new TaskMetrics();
            DeadLetterQueue dlq = new DeadLetterQueue("logs/dlq.log");
            TaskDatabase database = new TaskDatabase();
            TaskBatchService batchService = new TaskBatchService(50);

            // Feature 5: Worker Resource Monitoring
            System.out.println("2. Registering workers with resource monitoring...");
            Worker worker1 = new Worker(10);
            Worker worker2 = new Worker(10);
            worker1.setCpuUsage(35.5);
            worker1.setMemoryUsage(42.0);
            worker2.setCpuUsage(28.3);
            worker2.setMemoryUsage(51.2);
            workerManager.registerWorker(worker1);
            workerManager.registerWorker(worker2);
            System.out.println("  Worker 1: " + worker1.getId() + " - CPU: " + worker1.getCpuUsage() + "%, Memory: "
                    + worker1.getMemoryUsage() + "%");
            System.out.println("  Worker 2: " + worker2.getId() + " - CPU: " + worker2.getCpuUsage() + "%, Memory: "
                    + worker2.getMemoryUsage() + "%\n");

            // Feature 6: Task Batching
            System.out.println("3. Creating and batching tasks...");
            Task task1 = new Task("Data Ingestion", 2, 5000); // 5s timeout
            Task task2 = new Task("Feature Extraction", 1, 7000); // 7s timeout
            Task task3 = new Task("Model Inference", 1, 8000); // 8s timeout
            Task task4 = new Task("Result Validation", 3, 3000); // 3s timeout
            Task task5 = new Task("Cleanup", 4, 2000); // 2s timeout

            task2.addDependency(task1.getId());
            task3.addDependency(task2.getId());
            task5.addDependency(task3.getId());

            java.util.List<Task> taskList = java.util.Arrays.asList(task1, task2, task3, task4, task5);
            scheduler.submitTask(task1);
            scheduler.submitTask(task2);
            scheduler.submitTask(task3);
            scheduler.submitTask(task4);
            scheduler.submitTask(task5);

            taskList.forEach(t -> metrics.recordTaskStart(t.getId(), t.getName(), t.getCreatedAt()));

            // Show batch info
            java.util.List<java.util.List<Task>> batches = batchService.createBatches(taskList);
            System.out.println("  Created " + batches.size() + " batches");
            java.util.Map<String, Object> batchStats = batchService.getBatchStats(taskList);
            System.out.println("  Batch Stats: " + batchStats + "\n");

            // Feature 2: Exponential Backoff for Retries
            System.out.println("4. Executing tasks with exponential backoff handling...");
            Task next = scheduler.getNextTask();
            if (next != null) {
                System.out.println("  Executing: " + next.getName() + " [Priority: " + next.getPriority() + "]");
                next.setStartedAt(System.currentTimeMillis());
                workerManager.assignTask(worker1.getId(), next);
                worker1.incrementActiveTaskCount();

                // Simulate completion
                scheduler.completeTask(next.getId());
                metrics.recordTaskCompletion(next.getId(), "COMPLETED", 0);
                worker1.decrementActiveTaskCount();
                System.out.println("  ✓ Task completed: " + next.getName() + "\n");
            }

            // Feature 3: Dead Letter Queue Persistence
            System.out.println("5. Simulating task failures and DLQ recording...");
            Task failedTask = taskList.get(3);
            dlq.recordFailedTask(failedTask.getId(), failedTask.getName(),
                    "Timeout: Task exceeded execution limit",
                    3, "java.util.concurrent.TimeoutException: Execution exceeded 3000ms");
            metrics.recordTaskCompletion(failedTask.getId(), "FAILED", 3);
            System.out.println("  Failed task recorded in DLQ\n");

            // Feature 3: Database Persistence
            System.out.println("6. Persisting tasks and workers to database...");
            taskList.forEach(t -> {
                database.persistTask(t);
            });
            java.util.Arrays.asList(worker1, worker2).forEach(database::persistWorker);
            System.out.println("  Tasks and workers persisted to database");
            database.printDatabaseStats();
            System.out.println();

            // Feature 1: Display Metrics
            System.out.println("7. Displaying execution metrics...");
            java.util.Map<String, Object> metricsReport = metrics.getMetricsReport();
            metricsReport.forEach((key, value) -> System.out.println("  " + key + ": " + value));
            System.out.println();

            // Show DLQ report
            dlq.printDLQReport();

            // Show worker status
            System.out.println("8. Worker Resource Status:");
            java.util.List<Worker> workers = workerManager.getWorkers();
            workers.forEach(w -> {
                System.out.println(
                        "  " + w.getId() + " - Capacity: " + w.getActiveTaskCount() + "/" + w.getMaxCapacity() +
                                " - Utilization: " + String.format("%.2f%%", w.getResourceUtilization()));
            });
            System.out.println();

            System.out.println("========================================");
            System.out.println("Feature Demonstration Complete!");
            System.out.println("========================================");
            System.out.println("\nImplemented Features:");
            System.out.println("1. ✓ Task Execution Metrics (TaskMetrics.java)");
            System.out.println("2. ✓ Exponential Backoff for Retries (PriorityScheduler.java)");
            System.out.println("3. ✓ Dead Letter Queue Persistence (DeadLetterQueue.java)");
            System.out.println("4. ✓ Task Timeout Management (Task.java)");
            System.out.println("5. ✓ Worker Resource Monitoring (Worker.java)");
            System.out.println("6. ✓ Task Batching Service (TaskBatchService.java)");
            System.out.println("7. ✓ REST API Server (TaskEngineController.java)");
            System.out.println("8. ✓ Persistent Storage Layer (TaskDatabase.java)");
            System.out.println("9. ✓ Scheduled Task Support (TaskSchedulerService.java)");
            System.out.println("10. ✓ Prometheus Metrics (PrometheusMetricsCollector.java)");
            System.out.println();

            workerManager.stop();

        } catch (Exception e) {
            System.err.println("Engine encountered an error:");
            e.printStackTrace();
        }
    }
}
