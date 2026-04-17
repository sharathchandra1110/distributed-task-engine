package com.engine.persistence;

import com.engine.model.Task;
import com.engine.model.Worker;
import java.sql.*;
import java.util.*;

public class TaskDatabase {
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    public TaskDatabase() {
        // H2 in-memory database configuration
        this.dbUrl = "jdbc:h2:mem:taskdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
        this.dbUser = "sa";
        this.dbPassword = "";
        initializeDatabase();
    }

    public TaskDatabase(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // Create tables
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS tasks (" +
                            "id VARCHAR(50) PRIMARY KEY," +
                            "name VARCHAR(255)," +
                            "priority INT," +
                            "status VARCHAR(20)," +
                            "created_at BIGINT," +
                            "started_at BIGINT," +
                            "completed_at BIGINT," +
                            "retry_count INT," +
                            "timeout_ms BIGINT," +
                            "assigned_worker_id VARCHAR(50)" +
                            ")");

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS workers (" +
                            "id VARCHAR(50) PRIMARY KEY," +
                            "created_at BIGINT," +
                            "cpu_usage DOUBLE," +
                            "memory_usage DOUBLE," +
                            "is_healthy BOOLEAN" +
                            ")");

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS task_results (" +
                            "id VARCHAR(50) PRIMARY KEY," +
                            "task_id VARCHAR(50)," +
                            "result TEXT," +
                            "error_message TEXT," +
                            "execution_time_ms BIGINT," +
                            "recorded_at BIGINT," +
                            "FOREIGN KEY (task_id) REFERENCES tasks(id)" +
                            ")");

            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public void persistTask(Task task) {
        String sql = "INSERT OR REPLACE INTO tasks VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getId());
            pstmt.setString(2, task.getName());
            pstmt.setInt(3, task.getPriority());
            pstmt.setString(4, task.getStatus().toString());
            pstmt.setLong(5, task.getCreatedAt());
            pstmt.setLong(6, task.getStartedAt());
            pstmt.setLong(7, System.currentTimeMillis());
            pstmt.setInt(8, task.getRetryCount());
            pstmt.setLong(9, task.getTimeoutMs());
            pstmt.setString(10, task.getAssignedWorkerId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to persist task: " + e.getMessage());
        }
    }

    public void persistWorker(Worker worker) {
        String sql = "INSERT OR REPLACE INTO workers VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, worker.getId());
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setDouble(3, worker.getCpuUsage());
            pstmt.setDouble(4, worker.getMemoryUsage());
            pstmt.setBoolean(5, worker.isHealthy());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to persist worker: " + e.getMessage());
        }
    }

    public void recordTaskResult(String taskId, String result, String errorMessage, long executionTimeMs) {
        String sql = "INSERT INTO task_results VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, taskId);
            pstmt.setString(3, result);
            pstmt.setString(4, errorMessage);
            pstmt.setLong(5, executionTimeMs);
            pstmt.setLong(6, System.currentTimeMillis());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to record task result: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getAllTasks() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> task = new LinkedHashMap<>();
                task.put("id", rs.getString("id"));
                task.put("name", rs.getString("name"));
                task.put("priority", rs.getInt("priority"));
                task.put("status", rs.getString("status"));
                task.put("retryCount", rs.getInt("retry_count"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve tasks: " + e.getMessage());
        }

        return tasks;
    }

    public void printDatabaseStats() {
        String sql = "SELECT COUNT(*) as count FROM tasks";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("Total tasks in database: " + rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get database stats: " + e.getMessage());
        }
    }
}
