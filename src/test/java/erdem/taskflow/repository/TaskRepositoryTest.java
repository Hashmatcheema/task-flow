package erdem.taskflow.repository;

import erdem.taskflow.model.Priority;
import erdem.taskflow.model.Status;
import erdem.taskflow.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskRepositoryTest {

    @Mock
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAllByOrderByPriorityDescDueDateAsc() {
        Task task1 = createTask("Task 1", Priority.LOW, LocalDate.now().plusDays(2));
        Task task2 = createTask("Task 2", Priority.HIGH, LocalDate.now().plusDays(1));
        Task task3 = createTask("Task 3", Priority.MEDIUM, LocalDate.now().plusDays(3));

        when(taskRepository.findAllByOrderByPriorityDescDueDateAsc())
                .thenReturn(Arrays.asList(task2, task3, task1));

        List<Task> tasks = taskRepository.findAllByOrderByPriorityDescDueDateAsc();

        assertEquals(3, tasks.size());
        assertEquals("Task 2", tasks.get(0).getTitle()); // HIGH priority first
        assertEquals("Task 3", tasks.get(1).getTitle()); // MEDIUM priority
        assertEquals("Task 1", tasks.get(2).getTitle()); // LOW priority
        verify(taskRepository).findAllByOrderByPriorityDescDueDateAsc();
    }

    @Test
    void testCountByStatus() {
        when(taskRepository.countByStatus(Status.OPEN)).thenReturn(1L);
        when(taskRepository.countByStatus(Status.COMPLETED)).thenReturn(1L);

        long openCount = taskRepository.countByStatus(Status.OPEN);
        long completedCount = taskRepository.countByStatus(Status.COMPLETED);

        assertEquals(1, openCount);
        assertEquals(1, completedCount);
        verify(taskRepository).countByStatus(Status.OPEN);
        verify(taskRepository).countByStatus(Status.COMPLETED);
    }

    @Test
    void testFindByDueDateBefore() {
        LocalDate today = LocalDate.now();
        Task overdueTask = createTask("Overdue Task", Priority.MEDIUM, today.minusDays(1));

        when(taskRepository.findByDueDateBefore(today)).thenReturn(Arrays.asList(overdueTask));

        List<Task> overdueTasks = taskRepository.findByDueDateBefore(today);

        assertEquals(1, overdueTasks.size());
        assertEquals("Overdue Task", overdueTasks.get(0).getTitle());
        verify(taskRepository).findByDueDateBefore(today);
    }

    @Test
    void testFindWithFilters_ByStatus() {
        Task openTask = createTask("Open Task", Priority.MEDIUM, null);
        openTask.setStatus(Status.OPEN);
        LocalDate today = LocalDate.now();

        when(taskRepository.findWithFilters(Status.OPEN, null, null, null, null, today))
                .thenReturn(Arrays.asList(openTask));

        List<Task> filteredTasks = taskRepository.findWithFilters(
                Status.OPEN, null, null, null, null, today);

        assertEquals(1, filteredTasks.size());
        assertEquals("Open Task", filteredTasks.get(0).getTitle());
        verify(taskRepository).findWithFilters(Status.OPEN, null, null, null, null, today);
    }

    @Test
    void testFindWithFilters_ByPriority() {
        Task highTask = createTask("High Task", Priority.HIGH, null);
        LocalDate today = LocalDate.now();

        when(taskRepository.findWithFilters(null, Priority.HIGH, null, null, null, today))
                .thenReturn(Arrays.asList(highTask));

        List<Task> filteredTasks = taskRepository.findWithFilters(
                null, Priority.HIGH, null, null, null, today);

        assertEquals(1, filteredTasks.size());
        assertEquals("High Task", filteredTasks.get(0).getTitle());
        verify(taskRepository).findWithFilters(null, Priority.HIGH, null, null, null, today);
    }

    @Test
    void testCountOverdueTasks() {
        LocalDate today = LocalDate.now();
        when(taskRepository.countOverdueTasks(today)).thenReturn(2L);

        long overdueCount = taskRepository.countOverdueTasks(today);

        assertEquals(2, overdueCount);
        verify(taskRepository).countOverdueTasks(today);
    }

    private Task createTask(String title, Priority priority, LocalDate dueDate) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Description for " + title);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setStatus(Status.OPEN);
        task.setCreatedAt(Instant.now());
        task.setStatusUpdatedAt(Instant.now());
        task.setStatusHistory(new java.util.ArrayList<>());
        return task;
    }
}