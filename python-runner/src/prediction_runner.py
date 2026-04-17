import time
import random
import uuid
import concurrent.futures
from dataclasses import dataclass, field
from typing import List, Dict
import logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

@dataclass(order=True)
class PredictionTask:
    priority: int
    task_id: str = field(default_factory=lambda: str(uuid.uuid4()))
    model_name: str = "resnet50"
    data_payload: dict = field(default_factory=dict)
    retries: int = 0

class DistributedPredictionRunner:
    def __init__(self, num_workers=8):
        self.num_workers = num_workers
        self.max_retries = 3
        self.dead_letter_queue = []

    def simulate_inference(self, task: PredictionTask):
        """Simulates ML inference with potential for failure and backoff."""
        time.sleep(random.uniform(0.1, 0.5)) # Simulate processing time
        
        # Simulate 5% failure rate
        if random.random() < 0.05:
            raise Exception("Inference Engine Timeout")
        
        return f"Prediction result for {task.task_id}"

    def run_batch(self, tasks: List[PredictionTask]):
        start_time = time.time()
        logging.info(f"Starting batch prediction for {len(tasks)} tasks with {self.num_workers} workers")
        
        results = []
        pending_tasks = tasks
        
        with concurrent.futures.ThreadPoolExecutor(max_workers=self.num_workers) as executor:
            while pending_tasks:
                future_to_task = {executor.submit(self.simulate_inference, t): t for t in pending_tasks}
                pending_tasks = []
                
                for future in concurrent.futures.as_completed(future_to_task):
                    task = future_to_task[future]
                    try:
                        result = future.result()
                        results.append(result)
                    except Exception as e:
                        logging.warning(f"Task {task.task_id} failed: {e}")
                        if task.retries < self.max_retries:
                            task.retries += 1
                            # Exponential backoff
                            wait_time = (2 ** task.retries) + random.random()
                            logging.info(f"Retrying task {task.task_id} in {wait_time:.2f}s (Attempt {task.retries})")
                            time.sleep(0.1) # Short sleep for simulation
                            pending_tasks.append(task)
                        else:
                            logging.error(f"Task {task.task_id} moved to Dead Letter Queue")
                            self.dead_letter_queue.append({
                                "task_id": task.task_id,
                                "error": str(e),
                                "attempts": task.retries,
                                "timestamp": time.time()
                            })

        end_time = time.time()
        duration = end_time - start_time
        logging.info(f"Batch completed in {duration:.2f} seconds")
        logging.info(f"Successful: {len(results)}, Failed: {len(self.dead_letter_queue)}")
        return results

if __name__ == "__main__":
    runner = DistributedPredictionRunner(num_workers=8)
    
    # Create 100 sample tasks
    sample_tasks = [PredictionTask(priority=random.randint(1, 5)) for _ in range(100)]
    
    runner.run_batch(sample_tasks)
