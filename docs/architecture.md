# System Architecture

## Overview
The system follows a Master-Worker architecture where the Scheduler acts as the central coordinator.

## Components

### 1. Priority Scheduler (Java)
- **Data Structure**: Min-Heap (via `PriorityBlockingQueue`).
- **Complexity**: O(log n) for enqueue/dequeue.
- **Cycle Detection**: Kahn's Algorithm / DFS to ensure DAG integrity.

### 2. Worker Manager
- **Health Checks**: Heartbeat mechanism.
- **Timeout**: 30 seconds (configurable).
- **State Management**: Tracks active assignments to prevent task loss.

### 3. Execution Engine (Python)
- **Concurrency**: `ThreadPoolExecutor` for parallel IO/Inference.
- **Error Handling**: Custom retry logic with exponential backoff.
- **Metadata**: Structured failure reports captured in the Dead Letter Queue.

## Data Flow
1. **Submission**: Task submitted with priority and dependencies.
2. **Validation**: Dependency graph checked for cycles.
3. **Queueing**: Task waits in `PENDING` until dependencies are met.
4. **Dispatch**: `READY` tasks moved to `PriorityQueue`.
5. **Execution**: Workers poll for tasks and execute.
6. **Completion**: Worker signals completion; Scheduler unlocks dependent tasks.
