package ir.meysampalizban.sta.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ir.meysampalizban.sta.enums.TaskCategory;
import ir.meysampalizban.sta.enums.TaskPriority;
import ir.meysampalizban.sta.enums.TaskStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task")
@Getter @Setter @NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskCategory category = TaskCategory.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(nullable = false)
    private boolean enriched = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<SubTask> SubTasks = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /** جایگزینی کامل زیروظایف — orphanRemoval بقیه را حذف می‌کند */
    public void replaceSubTasks(List<String> titles) {
        this.SubTasks.clear();
        int pos = 0;
        for (String t : titles) {
            if (t == null || t.isBlank()) continue;
            SubTask s = new SubTask();
            s.setTask(this);
            s.setTitle(t.trim());
            s.setPosition(pos++);
            this.SubTasks.add(s);
        }
    }
}