package erdem.taskflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue
    private UUID id;

    @Getter
    @NotBlank
    private String title;

    @Getter
    private String description;

    @Getter
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Getter
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant createdAt;
    private Instant statusUpdatedAt;

    @ElementCollection
    @CollectionTable(name = "task_status_history", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "status_change")
    private List<String> statusHistory = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (status == null) {
            status = Status.OPEN;
        }
        if (statusUpdatedAt == null) {
            statusUpdatedAt = now;
        }
        if (statusHistory == null) {
            statusHistory = new ArrayList<>();
        }
        // Add initial status to history if empty
        if (statusHistory.isEmpty() && status != null) {
            statusHistory.add(createStatusHistoryEntry(status, now));
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Status change tracking is handled in service layer
    }

    public void updateStatus(Status newStatus) {
        if (this.status != newStatus) {
            this.status = newStatus;
            this.statusUpdatedAt = Instant.now();
            if (statusHistory == null) {
                statusHistory = new ArrayList<>();
            }
            statusHistory.add(createStatusHistoryEntry(newStatus, Instant.now()));
        }
    }

    private String createStatusHistoryEntry(Status status, Instant timestamp) {
        return status.name() + "|" + timestamp.toString();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStatusUpdatedAt() {
        return statusUpdatedAt;
    }

    public void setStatusUpdatedAt(Instant statusUpdatedAt) {
        this.statusUpdatedAt = statusUpdatedAt;
    }

    public List<String> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<String> statusHistory) {
        this.statusHistory = statusHistory;
    }

//    public Status getStatus() {
//        return status;
//    }
//
//    public Instant getCreatedAt() {
//        return createdAt;
//    }
//
//    public Instant getStatusUpdatedAt() {
//        return statusUpdatedAt;
//    }
//
//    public List<String> getStatusHistory() {
//        return statusHistory;
    }
//}
//
