package erdem.taskflow.service;

import erdem.taskflow.dto.TaskRequestDTO;
import erdem.taskflow.dto.TaskResponseDTO;
import erdem.taskflow.dto.TaskStatsDTO;
import erdem.taskflow.model.Priority;
import erdem.taskflow.model.Status;
import erdem.taskflow.model.Task;
import erdem.taskflow.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private UUID testTaskId;

    @BeforeEach
    void setUp() {
        testTaskId = UUID.randomUUID();
        testTask = new Task();
        testTask.setId(testTaskId);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setPriority(Priority.HIGH);
        testTask.setDueDate(LocalDate.now().plusDays(1));
        testTask.setStatus(Status.OPEN);
        testTask.setCreatedAt(Instant.now());
        testTask.setStatusUpdatedAt(Instant.now());
        testTask.setStatusHistory(new ArrayList<>());
    }

    @Test
    void testGetAll() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findAllByOrderByPriorityDescDueDateAsc()).thenReturn(tasks);

        List<TaskResponseDTO> result = taskService.getAll(null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTaskId, result.get(0).getId());
        verify(taskRepository).findAllByOrderByPriorityDescDueDateAsc();
    }

    @Test
    void testGetById_Success() {
        when(taskRepository.findById(testTaskId)).thenReturn(Optional.of(testTask));

        TaskResponseDTO result = taskService.getById(testTaskId);

        assertNotNull(result);
        assertEquals(testTaskId, result.getId());
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository).findById(testTaskId);
    }

    @Test
    void testGetById_NotFound() {
        when(taskRepository.findById(testTaskId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.getById(testTaskId));
        verify(taskRepository).findById(testTaskId);
    }

    @Test
    void testCreate() {
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("New Task");
        requestDTO.setDescription("New Description");
        requestDTO.setPriority(Priority.MEDIUM);
        requestDTO.setDueDate(LocalDate.now().plusDays(2));

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(testTaskId);
            return task;
        });

        TaskResponseDTO result = taskService.create(requestDTO);

        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        assertEquals(Priority.MEDIUM, result.getPriority());
        assertEquals(Status.OPEN, result.getStatus());
        assertNotNull(result.getCreatedAt());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void testCreate_DefaultPriority() {
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("New Task");
        requestDTO.setPriority(null);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(testTaskId);
            return task;
        });

        TaskResponseDTO result = taskService.create(requestDTO);

        assertEquals(Priority.MEDIUM, result.getPriority());
    }

    @Test
    void testUpdate_Success() {
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Updated Task");
        requestDTO.setDescription("Updated Description");
        requestDTO.setPriority(Priority.LOW);
        requestDTO.setDueDate(LocalDate.now().plusDays(3));

        when(taskRepository.findById(testTaskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskResponseDTO result = taskService.update(testTaskId, requestDTO);

        assertNotNull(result);
        verify(taskRepository).findById(testTaskId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void testUpdate_NotFound() {
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        when(taskRepository.findById(testTaskId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.update(testTaskId, requestDTO));
        verify(taskRepository).findById(testTaskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testUpdateStatus() {
        when(taskRepository.findById(testTaskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskResponseDTO result = taskService.updateStatus(testTaskId, Status.IN_PROGRESS);

        assertNotNull(result);
        verify(taskRepository).findById(testTaskId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void testDelete_Success() {
        when(taskRepository.existsById(testTaskId)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(testTaskId);

        taskService.delete(testTaskId);

        verify(taskRepository).existsById(testTaskId);
        verify(taskRepository).deleteById(testTaskId);
    }

    @Test
    void testDelete_NotFound() {
        when(taskRepository.existsById(testTaskId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> taskService.delete(testTaskId));
        verify(taskRepository).existsById(testTaskId);
        verify(taskRepository, never()).deleteById(testTaskId);
    }

    @Test
    void testFilterTasks() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findWithFilters(any(), any(), any(), any(), any(), any())).thenReturn(tasks);

        List<TaskResponseDTO> result = taskService.filterTasks(
                Status.OPEN, Priority.HIGH, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository).findWithFilters(eq(Status.OPEN), eq(Priority.HIGH), isNull(), isNull(), isNull(),
                any(LocalDate.class));
    }

    // @Test
    // void testGetStatistics() {
    // when(taskRepository.countByStatus(Status.OPEN)).thenReturn(5L);
    // when(taskRepository.countByStatus(Status.IN_PROGRESS)).thenReturn(3L);
    // when(taskRepository.countByStatus(Status.COMPLETED)).thenReturn(10L);
    // when(taskRepository.countOverdueTasks(any(LocalDate.class))).thenReturn(2L);
    // when(taskRepository.count()).thenReturn(18L);

    // TaskStatsDTO stats = taskService.getStatistics();

    // assertNotNull(stats);
    // assertEquals(5L, stats.getOpenTasks());
    // assertEquals(3L, stats.getInProgressTasks());
    // assertEquals(10L, stats.getCompletedTasks());
    // assertEquals(2L, stats.getOverdueTasks());
    // assertEquals(18L, stats.getTotalTasks());
    // }

    @Test
    void testGetOverdueTasks() {
        Task overdueTask = new Task();
        overdueTask.setId(UUID.randomUUID());
        overdueTask.setStatus(Status.OPEN);
        overdueTask.setDueDate(LocalDate.now().minusDays(1));

        List<Task> overdueTasks = Arrays.asList(overdueTask);
        when(taskRepository.findByDueDateBefore(any(LocalDate.class))).thenReturn(overdueTasks);

        List<TaskResponseDTO> result = taskService.getOverdueTasks();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository).findByDueDateBefore(any(LocalDate.class));
    }

    @Test
    void testGetTasksDueToday() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findWithFilters(any(), any(), any(), any(), any(), any())).thenReturn(tasks);

        List<TaskResponseDTO> result = taskService.getTasksDueToday();

        assertNotNull(result);
        verify(taskRepository).findWithFilters(any(), any(), any(), any(), any(), any(LocalDate.class));
    }
}
