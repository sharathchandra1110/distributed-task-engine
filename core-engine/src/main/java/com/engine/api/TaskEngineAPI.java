package com.engine.api;

import java.util.*;

/**
 * REST API Documentation for Task Engine
 * 
 * Note: This is a reference implementation. In production, use Spring Boot or
 * similar framework.
 * 
 * The following endpoints are available:
 * 
 * POST /api/tasks/submit
 * - Submit a single task
 * - Body: {"name": "Task Name", "priority": 1, "timeoutMs": 5000}
 * - Response: {"status": "success", "taskId": "...", "message": "..."}
 * 
 * POST /api/tasks/submit-batch
 * - Submit multiple tasks
 * - Body: [{"name": "Task 1", "priority": 1, "timeoutMs": 5000}, ...]
 * - Response: {"status": "success", "taskIds": [...], "count": 2, "message":
 * "..."}
 * 
 * GET /api/tasks/status/{taskId}
 * - Get status of a specific task
 * - Response: {"id": "...", "name": "...", "status": "READY", "priority": 1,
 * ...}
 * 
 * GET /api/tasks/all
 * - Get all tasks
 * - Response: {"total": 10, "queued": 5, "tasks": [...]}
 * 
 * GET /api/tasks/metrics
 * - Get system metrics
 * - Response: {"total_tasks_processed": 100, "success_rate_percent": "95.00%",
 * ...}
 * 
 * POST /api/tasks/complete/{taskId}
 * - Mark task as completed
 * - Response: {"status": "success", "message": "..."}
 * 
 * POST /api/tasks/fail/{taskId}
 * - Mark task as failed
 * - Body: {"message": "Error reason"}
 * - Response: {"status": "success", "message": "..."}
 * 
 * GET /api/tasks/workers
 * - Get worker information
 * - Response: {"total": 2, "workers": [...]}
 * 
 * GET /api/tasks/health
 * - Health check
 * - Response: {"status": "healthy", "timestamp": "..."}
 */
public class TaskEngineAPI {

    public static class TaskRequest {
        private String name;
        private int priority;
        private long timeoutMs;
        private List<String> dependencies;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        public List<String> getDependencies() {
            return dependencies;
        }

        public void setDependencies(List<String> dependencies) {
            this.dependencies = dependencies;
        }
    }

    public static class ErrorRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static void printAPIDocumentation() {
        System.out.println("\n========================================");
        System.out.println("Task Engine REST API Documentation");
        System.out.println("========================================\n");
        System.out.println("POST /api/tasks/submit");
        System.out.println("  Submit a single task");
        System.out.println("  Body: {\"name\": \"Task Name\", \"priority\": 1, \"timeoutMs\": 5000}\n");

        System.out.println("POST /api/tasks/submit-batch");
        System.out.println("  Submit multiple tasks");
        System.out.println("  Body: [{\"name\": \"Task 1\", \"priority\": 1}, ...]\n");

        System.out.println("GET /api/tasks/status/{taskId}");
        System.out.println("  Get task status\n");

        System.out.println("GET /api/tasks/all");
        System.out.println("  Get all tasks\n");

        System.out.println("GET /api/tasks/metrics");
        System.out.println("  Get system metrics\n");

        System.out.println("GET /api/tasks/workers");
        System.out.println("  Get worker information\n");

        System.out.println("GET /api/tasks/health");
        System.out.println("  Health check\n");

        System.out.println("========================================\n");
    }
}
