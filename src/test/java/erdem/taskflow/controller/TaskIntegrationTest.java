package erdem.taskflow.controller;

import erdem.taskflow.dto.TaskRequestDTO;
import erdem.taskflow.model.Priority;
import erdem.taskflow.model.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateAndGetTask() throws Exception {
        // 1. Create Task
        TaskRequestDTO request = new TaskRequestDTO();
        request.setTitle("Integration Test Task");
        request.setDescription("Testing End-to-End with MockMvc");
        request.setPriority(Priority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(5));

        MvcResult result = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        String taskId = io.micrometer.common.util.StringUtils.isEmpty(responseString) ? ""
                : objectMapper.readTree(responseString).get("id").asText();

        // 2. Get Task (if we got a valid ID)
        if (!taskId.isEmpty()) {
            mockMvc.perform(get("/api/tasks/" + taskId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(taskId))
                    .andExpect(jsonPath("$.status").value(Status.OPEN.name()));
        }
    }
}
