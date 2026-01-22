package erdem.taskflow.controller;

import erdem.taskflow.dto.TaskRequestDTO;
import erdem.taskflow.dto.TaskResponseDTO;
import erdem.taskflow.dto.TaskStatsDTO;
import erdem.taskflow.model.Priority;
import erdem.taskflow.model.Status;
import erdem.taskflow.service.TaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
        logger.debug("TaskController initialized");
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        
        logger.debug("GET /api/tasks - Received request with params: status={}, priority={}, dueDateFrom={}, dueDateTo={}, searchTerm={}, sortBy={}, sortOrder={}", 
                status, priority, dueDateFrom, dueDateTo, searchTerm, sortBy, sortOrder);
        
        List<TaskResponseDTO> tasks;
        if (status != null || priority != null || dueDateFrom != null || dueDateTo != null || searchTerm != null) {
            logger.debug("Applying filters - calling filterTasks()");
            tasks = taskService.filterTasks(status, priority, dueDateFrom, dueDateTo, searchTerm, sortBy, sortOrder);
        } else {
            logger.debug("No filters applied - calling getAll()");
            tasks = taskService.getAll(sortBy, sortOrder);
        }
        
        logger.debug("Returning {} tasks", tasks.size());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable UUID id) {
        logger.debug("GET /api/tasks/{} - Fetching task by ID", id);
        try {
            TaskResponseDTO task = taskService.getById(id);
            logger.debug("Task found: {}", task.getTitle());
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            logger.warn("Task not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO requestDTO) {
        logger.debug("POST /api/tasks - Creating new task: title={}, priority={}, dueDate={}", 
                requestDTO.getTitle(), requestDTO.getPriority(), requestDTO.getDueDate());
        try {
            TaskResponseDTO created = taskService.create(requestDTO);
            logger.debug("Task created successfully with ID: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating task: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskRequestDTO requestDTO) {
        logger.debug("PUT /api/tasks/{} - Updating task: title={}, priority={}", 
                id, requestDTO.getTitle(), requestDTO.getPriority());
        try {
            TaskResponseDTO updated = taskService.update(id, requestDTO);
            logger.debug("Task updated successfully");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.warn("Task not found for update: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating task: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(
            @PathVariable UUID id,
            @RequestParam Status status) {
        logger.debug("PUT /api/tasks/{}/status - Updating status to: {}", id, status);
        try {
            TaskResponseDTO updated = taskService.updateStatus(id, status);
            logger.debug("Task status updated successfully");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.warn("Task not found for status update: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        logger.debug("DELETE /api/tasks/{} - Deleting task", id);
        try {
            taskService.delete(id);
            logger.debug("Task deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warn("Task not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<TaskStatsDTO> getStatistics() {
        logger.debug("GET /api/tasks/stats - Fetching statistics");
        TaskStatsDTO stats = taskService.getStatistics();
        logger.debug("Statistics retrieved successfully");
        return ResponseEntity.ok(stats);
    }
}
