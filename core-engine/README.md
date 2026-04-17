# Core Engine

This folder contains the Java implementation of the Distributed Task Engine core.

## Overview

The core engine includes:
- Task scheduling with priority and dependency management
- Exponential retry backoff for failures
- Task timeout detection
- Dead Letter Queue (DLQ) persistence
- Worker resource tracking
- Batch processing support
- Embedded persistence support via H2 database schema (driver required at runtime)
- Scheduled task support
- Metrics collection and reporting

## Project Structure

- `src/main/java/com/engine/api` - REST API contract and request/response DTOs
- `src/main/java/com/engine/batch` - task batching service
- `src/main/java/com/engine/core` - main entrypoint and service layer
- `src/main/java/com/engine/dlq` - dead letter queue implementation
- `src/main/java/com/engine/metrics` - task metrics collection
- `src/main/java/com/engine/model` - core Task and Worker models
- `src/main/java/com/engine/monitoring` - metrics collector
- `src/main/java/com/engine/persistence` - persistence and DB integration
- `src/main/java/com/engine/schedule` - scheduling support
- `src/main/java/com/engine/scheduler` - priority scheduler
- `src/main/java/com/engine/worker` - worker management
- `pom.xml` - Maven configuration file

## Build

The core engine compiles with standard Java tooling.

```bash
cd core-engine
javac -d target/classes -sourcepath src/main/java src/main/java/com/engine/**/*.java
```

## Run

```bash
cd core-engine
java -cp target/classes com.engine.core.EngineApp
```

## Notes

- The engine uses an H2 JDBC URL for persistence, but the H2 driver is not bundled into the runtime classpath by default. If you want fully working DB persistence, add the H2 JAR to the classpath or run via Maven.
- The application currently runs as a console demo. The API layer is documented in `src/main/java/com/engine/api/TaskEngineAPI.java`, but no Spring web server is started by default.
- DLQ entries are persisted in `core-engine/logs/dlq.log`.

## Features

- Priority-based task queueing
- DAG dependency enforcement and cycle detection
- Retry handling with exponential backoff
- Dead letter queue with persistent log storage
- Worker heartbeat and resource tracking
- Batch creation and optimization
- Timeout support for long-running tasks
- Simplified scheduler for recurring tasks
- Metrics reporting for task execution

## Troubleshooting

### No suitable driver found for H2

If you see this message:

```
No suitable driver found for jdbc:h2:mem:taskdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
```

then the H2 JDBC driver is not available on the runtime classpath. To fix this, run the project with the H2 driver included or use Maven:

```bash
mvn dependency:copy-dependencies
java -cp target/classes:target/dependency/* com.engine.core.EngineApp
```

## Useful files

- `core-engine/logs/dlq.log` — stored DLQ entries
- `FEATURES.md` — detailed feature documentation
- `IMPLEMENTATION_SUMMARY.md` — implementation overview
- `COMPLETION_REPORT.txt` — final delivery report
