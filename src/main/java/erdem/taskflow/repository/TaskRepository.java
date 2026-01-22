package erdem.taskflow.repository;

import erdem.taskflow.model.Priority;
import erdem.taskflow.model.Status;
import erdem.taskflow.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findAllByOrderByPriorityDescDueDateAsc();

    long countByStatus(Status status);

    List<Task> findByDueDateBefore(LocalDate date);

    @Query("SELECT t FROM Task t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:dueDateFrom IS NULL OR t.dueDate >= :dueDateFrom) AND " +
           "(:dueDateTo IS NULL OR t.dueDate <= :dueDateTo) AND " +
           "(:searchTerm IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:today IS NULL OR :dueDateTo IS NULL OR :dueDateTo >= :today OR t.status != 'COMPLETED')")
    List<Task> findWithFilters(
            @Param("status") Status status,
            @Param("priority") Priority priority,
            @Param("dueDateFrom") LocalDate dueDateFrom,
            @Param("dueDateTo") LocalDate dueDateTo,
            @Param("searchTerm") String searchTerm,
            @Param("today") LocalDate today
    );

    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < :today AND t.status != 'COMPLETED'")
    long countOverdueTasks(@Param("today") LocalDate today);
}
