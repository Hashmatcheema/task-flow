package erdem.taskflow.repository;

import erdem.taskflow.model.Priority;
import erdem.taskflow.model.Status;
import erdem.taskflow.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class TaskRepositoryPerformanceTest {

    private static final int TASK_COUNT = 10_000;
    private static final int WARMUP_ITERATIONS = 3;
    private static final int MEASUREMENT_ITERATIONS = 10;
    private static final long MAX_FILTER_TIME_MS = 100;

    @Autowired
    private TaskRepository taskRepository;

    private Random random;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        random = new Random(42);
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
    }

    private void generateTestData() {
        LocalDate today = LocalDate.now();
        List<Task> tasks = new ArrayList<>();
        Instant baseTime = Instant.now();

        for (int i = 0; i < TASK_COUNT; i++) {
            Task task = new Task();
            
            String[] titlePrefixes = {"Project", "Task", "Feature", "Bug", "Enhancement", "Review", "Design", "Test"};
            String[] searchTerms = {"Java", "Spring", "Database", "API", "Frontend", "Backend", "Security", "Performance"};
            String titlePrefix = titlePrefixes[random.nextInt(titlePrefixes.length)];
            String searchTerm = searchTerms[random.nextInt(searchTerms.length)];
            task.setTitle(titlePrefix + " " + searchTerm + " " + i);
            task.setDescription("Description for " + titlePrefix + " " + searchTerm + " task number " + i);

            int priorityRand = random.nextInt(100);
            if (priorityRand < 30) {
                task.setPriority(Priority.HIGH);
            } else if (priorityRand < 70) {
                task.setPriority(Priority.MEDIUM);
            } else {
                task.setPriority(Priority.LOW);
            }

            int statusRand = random.nextInt(100);
            if (statusRand < 40) {
                task.setStatus(Status.OPEN);
            } else if (statusRand < 70) {
                task.setStatus(Status.IN_PROGRESS);
            } else {
                task.setStatus(Status.COMPLETED);
            }

            int dateRand = random.nextInt(100);
            if (dateRand < 20) {
                task.setDueDate(today.minusDays(random.nextInt(30) + 1));
            } else if (dateRand < 30) {
                task.setDueDate(today);
            } else {
                task.setDueDate(today.plusDays(random.nextInt(365) + 1));
            }

            task.setCreatedAt(baseTime.minusSeconds(random.nextInt(86400 * 30)));
            task.setStatusUpdatedAt(task.getCreatedAt());
            task.setStatusHistory(new ArrayList<>());
            
            tasks.add(task);

            if (tasks.size() >= 500) {
                taskRepository.saveAll(tasks);
                tasks.clear();
            }
        }

        if (!tasks.isEmpty()) {
            taskRepository.saveAll(tasks);
        }

        taskRepository.flush();
    }

    private PerformanceStats measureFilterPerformance(Runnable filterOperation) {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            filterOperation.run();
        }

        long[] times = new long[MEASUREMENT_ITERATIONS];
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            long start = System.nanoTime();
            filterOperation.run();
            long end = System.nanoTime();
            times[i] = TimeUnit.NANOSECONDS.toMillis(end - start);
        }

        long min = times[0];
        long max = times[0];
        long sum = times[0];
        for (int i = 1; i < times.length; i++) {
            if (times[i] < min) min = times[i];
            if (times[i] > max) max = times[i];
            sum += times[i];
        }
        double avg = (double) sum / times.length;

        return new PerformanceStats(min, max, avg);
    }

    private static class PerformanceStats {
        final long minMs;
        final long maxMs;
        final double avgMs;

        PerformanceStats(long minMs, long maxMs, double avgMs) {
            this.minMs = minMs;
            this.maxMs = maxMs;
            this.avgMs = avgMs;
        }

        @Override
        public String toString() {
            return String.format("Min: %d ms, Max: %d ms, Avg: %.2f ms", minMs, maxMs, avgMs);
        }
    }

    @Test
    void testFilterByStatus_Performance() {
        generateTestData();
        System.out.println("Generated " + TASK_COUNT + " tasks for status filter test");
        LocalDate today = LocalDate.now();

        PerformanceStats stats = measureFilterPerformance(() -> {
            List<Task> result = taskRepository.findWithFilters(Status.OPEN, null, null, null, null, today);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        });

        System.out.println("Filter by Status - " + stats);
        assertTrue(stats.avgMs < MAX_FILTER_TIME_MS,
                String.format("Average filter time (%.2f ms) exceeds maximum allowed (%d ms)", 
                        stats.avgMs, MAX_FILTER_TIME_MS));
        assertTrue(stats.maxMs < MAX_FILTER_TIME_MS * 1.5,
                String.format("Maximum filter time (%d ms) exceeds acceptable limit (%d ms)", 
                        stats.maxMs, (long)(MAX_FILTER_TIME_MS * 1.5)));
    }

    @Test
    void testFilterByPriority_Performance() {
        generateTestData();
        System.out.println("Generated " + TASK_COUNT + " tasks for priority filter test");
        LocalDate today = LocalDate.now();

        PerformanceStats stats = measureFilterPerformance(() -> {
            List<Task> result = taskRepository.findWithFilters(null, Priority.HIGH, null, null, null, today);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        });

        System.out.println("Filter by Priority - " + stats);
        assertTrue(stats.avgMs < MAX_FILTER_TIME_MS,
                String.format("Average filter time (%.2f ms) exceeds maximum allowed (%d ms)", 
                        stats.avgMs, MAX_FILTER_TIME_MS));
        assertTrue(stats.maxMs < MAX_FILTER_TIME_MS * 1.5,
                String.format("Maximum filter time (%d ms) exceeds acceptable limit (%d ms)", 
                        stats.maxMs, (long)(MAX_FILTER_TIME_MS * 1.5)));
    }

    @Test
    void testFilterByDateRange_Performance() {
        generateTestData();
        LocalDate today = LocalDate.now();
        System.out.println("Generated " + TASK_COUNT + " tasks for date range filter test");

        PerformanceStats stats = measureFilterPerformance(() -> {
            List<Task> result = taskRepository.findWithFilters(null, null, today, today, null, today);
            assertNotNull(result);
        });

        System.out.println("Filter by Date Range - " + stats);
        assertTrue(stats.avgMs < MAX_FILTER_TIME_MS,
                String.format("Average filter time (%.2f ms) exceeds maximum allowed (%d ms)", 
                        stats.avgMs, MAX_FILTER_TIME_MS));
        assertTrue(stats.maxMs < MAX_FILTER_TIME_MS * 1.5,
                String.format("Maximum filter time (%d ms) exceeds acceptable limit (%d ms)", 
                        stats.maxMs, (long)(MAX_FILTER_TIME_MS * 1.5)));
    }

    @Test
    void testFilterBySearchTerm_Performance() {
        generateTestData();
        System.out.println("Generated " + TASK_COUNT + " tasks for search term filter test");
        LocalDate today = LocalDate.now();

        PerformanceStats stats = measureFilterPerformance(() -> {
            List<Task> result = taskRepository.findWithFilters(null, null, null, null, "Java", today);
            assertNotNull(result);
        });

        System.out.println("Filter by Search Term - " + stats);
        assertTrue(stats.avgMs < MAX_FILTER_TIME_MS,
                String.format("Average filter time (%.2f ms) exceeds maximum allowed (%d ms)", 
                        stats.avgMs, MAX_FILTER_TIME_MS));
        assertTrue(stats.maxMs < MAX_FILTER_TIME_MS * 1.5,
                String.format("Maximum filter time (%d ms) exceeds acceptable limit (%d ms)", 
                        stats.maxMs, (long)(MAX_FILTER_TIME_MS * 1.5)));
    }

    @Test
    void testCombinedFilters_Performance() {
        generateTestData();
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        System.out.println("Generated " + TASK_COUNT + " tasks for combined filters test");

        PerformanceStats stats = measureFilterPerformance(() -> {
            List<Task> result = taskRepository.findWithFilters(
                    Status.OPEN, Priority.HIGH, today, nextWeek, null, today);
            assertNotNull(result);
        });

        System.out.println("Combined Filters (Status + Priority + Date) - " + stats);
        assertTrue(stats.avgMs < MAX_FILTER_TIME_MS,
                String.format("Average filter time (%.2f ms) exceeds maximum allowed (%d ms)", 
                        stats.avgMs, MAX_FILTER_TIME_MS));
        assertTrue(stats.maxMs < MAX_FILTER_TIME_MS * 1.5,
                String.format("Maximum filter time (%d ms) exceeds acceptable limit (%d ms)", 
                        stats.maxMs, (long)(MAX_FILTER_TIME_MS * 1.5)));
    }

    @Test
    void testGetAll_Performance() {
        generateTestData();
        System.out.println("Generated " + TASK_COUNT + " tasks for getAll test");

        PerformanceStats stats = measureFilterPerformance(() -> {
            List<Task> result = taskRepository.findAllByOrderByPriorityDescDueDateAsc();
            assertNotNull(result);
            assertEquals(TASK_COUNT, result.size());
        });

        System.out.println("Get All (with sorting) - " + stats);
        assertTrue(stats.avgMs < MAX_FILTER_TIME_MS * 3,
                String.format("Average getAll time (%.2f ms) is too high", stats.avgMs));
    }

    @Test
    void testFilterPerformance_AllScenarios() {
        generateTestData();
        System.out.println("Generated " + TASK_COUNT + " tasks for comprehensive performance test");
        LocalDate today = LocalDate.now();

        PerformanceStats statusStats = measureFilterPerformance(() -> {
            taskRepository.findWithFilters(Status.OPEN, null, null, null, null, today);
        });
        System.out.println("Status Filter: " + statusStats);
        assertTrue(statusStats.avgMs < MAX_FILTER_TIME_MS);

        PerformanceStats priorityStats = measureFilterPerformance(() -> {
            taskRepository.findWithFilters(null, Priority.MEDIUM, null, null, null, today);
        });
        System.out.println("Priority Filter: " + priorityStats);
        assertTrue(priorityStats.avgMs < MAX_FILTER_TIME_MS);

        PerformanceStats dateStats = measureFilterPerformance(() -> {
            taskRepository.findWithFilters(null, null, today, today.plusDays(7), null, today);
        });
        System.out.println("Date Range Filter: " + dateStats);
        assertTrue(dateStats.avgMs < MAX_FILTER_TIME_MS);

        PerformanceStats searchStats = measureFilterPerformance(() -> {
            taskRepository.findWithFilters(null, null, null, null, "Spring", today);
        });
        System.out.println("Search Term Filter: " + searchStats);
        assertTrue(searchStats.avgMs < MAX_FILTER_TIME_MS);

        PerformanceStats combinedStats = measureFilterPerformance(() -> {
            taskRepository.findWithFilters(Status.IN_PROGRESS, Priority.HIGH, today, today.plusDays(30), "API", today);
        });
        System.out.println("Combined Filters: " + combinedStats);
        assertTrue(combinedStats.avgMs < MAX_FILTER_TIME_MS);

        System.out.println("\n=== Performance Test Summary ===");
        System.out.println("All filter operations completed within acceptable time limits.");
    }
}
