//package erdem.taskflow.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import erdem.taskflow.dto.TaskResponseDTO;
//import erdem.taskflow.service.TaskService;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/backup")
//@CrossOrigin(origins = "*")
//public class BackupController {
//
//    private final TaskService taskService;
//    private final ObjectMapper objectMapper;
//
//    public BackupController(TaskService taskService, ObjectMapper objectMapper) {
//        this.taskService = taskService;
//        this.objectMapper = objectMapper;
//    }
//
//    @GetMapping("/export")
//    public ResponseEntity<String> exportTasks() {
//        try {
//            List<TaskResponseDTO> tasks = taskService.getAll();
//
//            Map<String, Object> backup = new HashMap<>();
//            backup.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//            backup.put("taskCount", tasks.size());
//            backup.put("tasks", tasks);
//
//            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(backup);
//
//            String filename = "taskflow_backup_" +
//                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
//
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .body(json);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//}
//
