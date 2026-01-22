package erdem.taskflow.controller;

import erdem.taskflow.dto.TaskRequestDTO;
import erdem.taskflow.dto.TaskResponseDTO;
import erdem.taskflow.model.Priority;
import erdem.taskflow.model.Status;
import erdem.taskflow.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private UUID testTaskId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllTasks() {
        TaskResponseDTO task = createTestTaskResponse();
        when(taskService.getAll(null, null)).thenReturn(Arrays.asList(task));

        ResponseEntity<List<TaskResponseDTO>> response = taskController.getAllTasks(null, null, null, null, null, null, null);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testTaskId, response.getBody().get(0).getId());
        verify(taskService).getAll(null, null);
    }

    @Test
    void testGetAllTasks_WithFilters() {
        TaskResponseDTO task = createTestTaskResponse();
        when(taskService.filterTasks(any(), any(), any(), any(), any(), any(), any())).thenReturn(Arrays.asList(task));

        ResponseEntity<List<TaskResponseDTO>> response = taskController.getAllTasks(Status.OPEN, Priority.HIGH, null, null, null, null, null);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(taskService).filterTasks(eq(Status.OPEN), eq(Priority.HIGH), any(), any(), any(), any(), any());
    }

    @Test
    void testGetTaskById_Success() {
        TaskResponseDTO task = createTestTaskResponse();
        when(taskService.getById(testTaskId)).thenReturn(task);

        ResponseEntity<TaskResponseDTO> response = taskController.getTaskById(testTaskId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(testTaskId, response.getBody().getId());
        assertEquals("Test Task", response.getBody().getTitle());
        verify(taskService).getById(testTaskId);
    }

    @Test
    void testCreateTask() {
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("New Task");
        requestDTO.setDescription("New Description");
        requestDTO.setPriority(Priority.MEDIUM);
        requestDTO.setDueDate(LocalDate.now().plusDays(1));

        TaskResponseDTO responseDTO = createTestTaskResponse();
        responseDTO.setTitle("New Task");
        when(taskService.create(any(TaskRequestDTO.class))).thenReturn(responseDTO);

        ResponseEntity<TaskResponseDTO> response = taskController.createTask(requestDTO);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("New Task", response.getBody().getTitle());
        verify(taskService).create(any(TaskRequestDTO.class));
    }

    @Test
    void testUpdateTask() {
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Updated Task");
        requestDTO.setPriority(Priority.HIGH);

        TaskResponseDTO responseDTO = createTestTaskResponse();
        responseDTO.setTitle("Updated Task");
        when(taskService.update(eq(testTaskId), any(TaskRequestDTO.class))).thenReturn(responseDTO);

        ResponseEntity<TaskResponseDTO> response = taskController.updateTask(testTaskId, requestDTO);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Updated Task", response.getBody().getTitle());
        verify(taskService).update(eq(testTaskId), any(TaskRequestDTO.class));
    }

    @Test
    void testUpdateTaskStatus() {
        TaskResponseDTO responseDTO = createTestTaskResponse();
        responseDTO.setStatus(Status.IN_PROGRESS);
        when(taskService.updateStatus(testTaskId, Status.IN_PROGRESS)).thenReturn(responseDTO);

        ResponseEntity<TaskResponseDTO> response = taskController.updateTaskStatus(testTaskId, Status.IN_PROGRESS);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(Status.IN_PROGRESS, response.getBody().getStatus());
        verify(taskService).updateStatus(testTaskId, Status.IN_PROGRESS);
    }

    @Test
    void testDeleteTask() {
        doNothing().when(taskService).delete(testTaskId);

        ResponseEntity<Void> response = taskController.deleteTask(testTaskId);

        assertEquals(204, response.getStatusCode().value());
        verify(taskService).delete(testTaskId);
    }

    private TaskResponseDTO createTestTaskResponse() {
        TaskResponseDTO task = new TaskResponseDTO();
        task.setId(testTaskId);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setPriority(Priority.HIGH);
        task.setDueDate(LocalDate.now().plusDays(1));
        task.setStatus(Status.OPEN);
        task.setCreatedAt(Instant.now());
        task.setStatusUpdatedAt(Instant.now());
        task.setStatusHistory(new ArrayList<>());
        return task;
    }
}