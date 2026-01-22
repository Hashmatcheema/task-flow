package erdem.taskflow.service;

import erdem.taskflow.dto.TaskRequestDTO;
import erdem.taskflow.dto.TaskResponseDTO;
import erdem.taskflow.dto.TaskStatsDTO;
import erdem.taskflow.dto.StatusChangeDTO;
import erdem.taskflow.model.Priority;
import erdem.taskflow.model.Status;
import erdem.taskflow.model.Task;
import erdem.taskflow.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {
        this.repo = repo;
        logger.debug("TaskService initialized");
    }

    public List<TaskResponseDTO> getAll(String sortBy, String sortOrder) {
        logger.debug("getAll() called with sortBy={}, sortOrder={}", sortBy, sortOrder);
        List<Task> tasks = repo.findAllByOrderByPriorityDescDueDateAsc();
        logger.debug("Retrieved {} tasks from repository", tasks.size());
        tasks = applySorting(tasks, sortBy, sortOrder);
        logger.debug("After sorting: {} tasks", tasks.size());
        List<TaskResponseDTO> result = tasks.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        logger.debug("Converted to {} DTOs", result.size());
        return result;
    }

    public TaskResponseDTO getById(UUID id) {
        Task task = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return toResponseDTO(task);
    }

    public TaskResponseDTO create(TaskRequestDTO requestDTO) {
        logger.debug("create() called for task: {}", requestDTO.getTitle());
        Task task = new Task();
        task.setTitle(requestDTO.getTitle());
        task.setDescription(requestDTO.getDescription());
        task.setPriority(requestDTO.getPriority() != null ? requestDTO.getPriority() : Priority.MEDIUM);
        task.setDueDate(requestDTO.getDueDate());
        task.setStatus(Status.OPEN);
        task.setCreatedAt(Instant.now());
        task.setStatusUpdatedAt(Instant.now());
        
        logger.debug("Saving task to repository: title={}, priority={}, dueDate={}", 
                task.getTitle(), task.getPriority(), task.getDueDate());
        Task saved = repo.save(task);
        logger.debug("Task saved with ID: {}", saved.getId());
        logger.debug("Status history after creation: {} entries", saved.getStatusHistory() != null ? saved.getStatusHistory().size() : 0);
        if (saved.getStatusHistory() != null && !saved.getStatusHistory().isEmpty()) {
            logger.debug("Status history entries: {}", saved.getStatusHistory());
        }
        return toResponseDTO(saved);
    }

    public TaskResponseDTO update(UUID id, TaskRequestDTO requestDTO) {
        Task task = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        task.setTitle(requestDTO.getTitle());
        task.setDescription(requestDTO.getDescription());
        task.setPriority(requestDTO.getPriority() != null ? requestDTO.getPriority() : task.getPriority());
        task.setDueDate(requestDTO.getDueDate());

        Task saved = repo.save(task);
        return toResponseDTO(saved);
    }

    public TaskResponseDTO updateStatus(UUID id, Status newStatus) {
        logger.debug("updateStatus() called for task {} to status {}", id, newStatus);
        Task task = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        Status oldStatus = task.getStatus();
        logger.debug("Current status: {}, New status: {}", oldStatus, newStatus);
        
        task.updateStatus(newStatus);
        logger.debug("Status history after update: {} entries", task.getStatusHistory() != null ? task.getStatusHistory().size() : 0);
        
        Task saved = repo.save(task);
        logger.debug("Task saved with status history: {} entries", saved.getStatusHistory() != null ? saved.getStatusHistory().size() : 0);
        return toResponseDTO(saved);
    }

    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        repo.deleteById(id);
    }

    public List<TaskResponseDTO> filterTasks(Status status, Priority priority, 
                                             LocalDate dueDateFrom, LocalDate dueDateTo, 
                                             String searchTerm, String sortBy, String sortOrder) {
        logger.debug("filterTasks() called with: status={}, priority={}, dueDateFrom={}, dueDateTo={}, searchTerm={}, sortBy={}, sortOrder={}", 
                status, priority, dueDateFrom, dueDateTo, searchTerm, sortBy, sortOrder);
        LocalDate today = LocalDate.now();
        logger.debug("Today's date: {}", today);
        logger.debug("Checking overdue filter: dueDateTo < today = {}", dueDateTo != null && dueDateTo.isBefore(today));
        
        List<Task> tasks = repo.findWithFilters(status, priority, dueDateFrom, dueDateTo, searchTerm, today);
        logger.debug("Repository returned {} tasks after filtering", tasks.size());
        
        tasks = applySorting(tasks, sortBy, sortOrder);
        logger.debug("After sorting: {} tasks", tasks.size());
        
        List<TaskResponseDTO> result = tasks.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        logger.debug("Converted to {} DTOs", result.size());
        return result;
    }

    public TaskStatsDTO getStatistics() {
        logger.debug("getStatistics() called");
        long openTasks = repo.countByStatus(Status.OPEN);
        long inProgressTasks = repo.countByStatus(Status.IN_PROGRESS);
        long completedTasks = repo.countByStatus(Status.COMPLETED);
        long overdueTasks = repo.countOverdueTasks(LocalDate.now());
        long totalTasks = repo.count();
        
        logger.debug("Statistics calculated - Total: {}, Open: {}, InProgress: {}, Completed: {}, Overdue: {}", 
                totalTasks, openTasks, inProgressTasks, completedTasks, overdueTasks);

        return new TaskStatsDTO(openTasks, inProgressTasks, completedTasks, overdueTasks, totalTasks);
    }

    public List<TaskResponseDTO> getOverdueTasks() {
        List<Task> overdueTasks = repo.findByDueDateBefore(LocalDate.now());
        return overdueTasks.stream()
                .filter(task -> task.getStatus() != Status.COMPLETED)
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<TaskResponseDTO> getTasksDueToday() {
        LocalDate today = LocalDate.now();
        return filterTasks(null, null, today, today, null, null, null);
    }

    private List<Task> applySorting(List<Task> tasks, String sortBy, String sortOrder) {
        logger.debug("applySorting() called with sortBy={}, sortOrder={}, tasks count={}", sortBy, sortOrder, tasks.size());
        
        if (sortBy == null || sortBy.isEmpty()) {
            logger.debug("No sorting applied - using default order");
            return tasks;
        }

        boolean ascending = sortOrder == null || !sortOrder.equalsIgnoreCase("DESC");
        logger.debug("Sort order: {}", ascending ? "ASC" : "DESC");
        Comparator<Task> comparator = null;

        switch (sortBy.toLowerCase()) {
            case "priority":
                logger.debug("Sorting by Priority");
                comparator = Comparator.comparing((Task t) -> {
                    if (t.getPriority() == null) return 4;
                    return switch (t.getPriority()) {
                        case HIGH -> 1;
                        case MEDIUM -> 2;
                        case LOW -> 3;
                    };
                });
                if (!ascending) comparator = comparator.reversed();
                break;

            case "duedate":
                logger.debug("Sorting by Due Date");
                comparator = Comparator.comparing(
                    (Task t) -> t.getDueDate() == null ? LocalDate.MAX : t.getDueDate(),
                    ascending ? Comparator.naturalOrder() : Comparator.reverseOrder()
                );
                break;

            case "title":
                logger.debug("Sorting by Title");
                comparator = Comparator.comparing(
                    (Task t) -> t.getTitle() == null ? "" : t.getTitle().toLowerCase(),
                    ascending ? Comparator.naturalOrder() : Comparator.reverseOrder()
                );
                break;

            case "createdat":
                logger.debug("Sorting by Created Date");
                comparator = Comparator.comparing(
                    (Task t) -> t.getCreatedAt() == null ? Instant.MIN : t.getCreatedAt(),
                    ascending ? Comparator.naturalOrder() : Comparator.reverseOrder()
                );
                break;

            default:
                logger.warn("Unknown sortBy value: {}, using default order", sortBy);
                return tasks;
        }

        List<Task> sorted = tasks.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
        logger.debug("Sorting completed - {} tasks sorted", sorted.size());
        return sorted;
    }

    private TaskResponseDTO toResponseDTO(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());
        dto.setStatus(task.getStatus());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setStatusUpdatedAt(task.getStatusUpdatedAt());
        
        // Parse status history
        List<StatusChangeDTO> history = null;
        if (task.getStatusHistory() != null && !task.getStatusHistory().isEmpty()) {
            history = task.getStatusHistory().stream()
                    .map(this::parseStatusHistoryEntry)
                    .collect(Collectors.toList());
            logger.debug("Parsed {} status history entries for task {}", history.size(), task.getId());
        }
        dto.setStatusHistory(history);
        
        return dto;
    }

    private StatusChangeDTO parseStatusHistoryEntry(String entry) {
        if (entry == null || entry.isEmpty()) {
            return new StatusChangeDTO("UNKNOWN", Instant.now());
        }
        
        String[] parts = entry.split("\\|");
        if (parts.length == 2) {
            try {
                return new StatusChangeDTO(parts[0], Instant.parse(parts[1]));
            } catch (Exception e) {
                logger.warn("Failed to parse status history entry: {}", entry, e);
                return new StatusChangeDTO(parts[0], Instant.now());
            }
        }
        // Fallback: if format is wrong, treat entire string as status
        logger.warn("Status history entry has unexpected format: {}", entry);
        return new StatusChangeDTO(entry, Instant.now());
    }
}
