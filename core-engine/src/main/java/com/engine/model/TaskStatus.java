package com.engine.model;

public enum TaskStatus {
    PENDING,
    READY,      // Dependencies met, ready for execution
    SCHEDULED,  // Assigned to a worker
    RUNNING,
    COMPLETED,
    FAILED,
    RETRYING,
    DEAD_LETTER
}
