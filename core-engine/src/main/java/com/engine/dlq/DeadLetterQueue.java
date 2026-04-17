package com.engine.dlq;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DeadLetterQueue {
    private final Queue<FailedTaskRecord> dlq;
    private final Path dlqLogFile;
    private final SimpleDateFormat dateFormat;

    public static class FailedTaskRecord {
        public String taskId;
        public String taskName;
        public String errorMessage;
        public int retryAttempts;
        public long failedAt;
        public String stackTrace;

        public FailedTaskRecord(String taskId, String taskName, String errorMessage, int retries, String stackTrace) {
            this.taskId = taskId;
            this.taskName = taskName;
            this.errorMessage = errorMessage;
            this.retryAttempts = retries;
            this.failedAt = System.currentTimeMillis();
            this.stackTrace = stackTrace;
        }

        @Override
        public String toString() {
            return String.format(
                    "TaskID=%s | Name=%s | Error=%s | Retries=%d | Time=%d | StackTrace=%s",
                    taskId, taskName, errorMessage, retryAttempts, failedAt,
                    stackTrace != null ? stackTrace.substring(0, Math.min(100, stackTrace.length())) : "N/A");
        }
    }

    public DeadLetterQueue(String logFilePath) throws IOException {
        this.dlq = new ConcurrentLinkedQueue<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.dlqLogFile = Paths.get(logFilePath);

        // Create parent directories if they don't exist
        Files.createDirectories(dlqLogFile.getParent());

        // Create or append to log file
        if (!Files.exists(dlqLogFile)) {
            Files.createFile(dlqLogFile);
            writeHeader();
        }
    }

    private void writeHeader() {
        try (FileWriter fw = new FileWriter(dlqLogFile.toFile(), true);
                BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write("========== Dead Letter Queue Log ==========\n");
            bw.write("Timestamp: " + dateFormat.format(new Date()) + "\n");
            bw.write("==========================================\n\n");
        } catch (IOException e) {
            System.err.println("Failed to write DLQ header: " + e.getMessage());
        }
    }

    public void recordFailedTask(String taskId, String taskName, String errorMessage, int retries, String stackTrace) {
        FailedTaskRecord record = new FailedTaskRecord(taskId, taskName, errorMessage, retries, stackTrace);
        dlq.add(record);
        persistToFile(record);
    }

    private void persistToFile(FailedTaskRecord record) {
        try (FileWriter fw = new FileWriter(dlqLogFile.toFile(), true);
                BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write("[" + dateFormat.format(new Date(record.failedAt)) + "] ");
            bw.write(record.toString());
            bw.write("\n");
            bw.flush();
        } catch (IOException e) {
            System.err.println("Failed to persist task to DLQ: " + e.getMessage());
        }
    }

    public List<FailedTaskRecord> getFailedTasks() {
        return new ArrayList<>(dlq);
    }

    public int getFailedTaskCount() {
        return dlq.size();
    }

    public void clearDLQ() {
        dlq.clear();
    }

    public void printDLQReport() {
        System.out.println("\n===== Dead Letter Queue Report =====");
        System.out.println("Total Failed Tasks: " + dlq.size());
        if (dlq.isEmpty()) {
            System.out.println("No failed tasks.");
        } else {
            dlq.forEach(record -> System.out.println("  - " + record));
        }
        System.out.println("=====================================\n");
    }
}
