package ir.meysampalizban.sta.service;

import ir.meysampalizban.sta.dto.CreateTaskRequest;
import ir.meysampalizban.sta.dto.UpdateTaskRequest;
import ir.meysampalizban.sta.entity.SubTask;
import ir.meysampalizban.sta.entity.Task;
import ir.meysampalizban.sta.repository.TaskRepository;
import org.springframework.stereotype.Service;

import ir.meysampalizban.sta.ai.TaskEnrichmentService;
import ir.meysampalizban.sta.ai.TaskEnrichmentService.TaskSuggestion;
import ir.meysampalizban.sta.common.EnrichmentException;
import ir.meysampalizban.sta.common.TaskNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ir.meysampalizban.sta.dto.TaskResponse;
import ir.meysampalizban.sta.enums.TaskCategory;
import ir.meysampalizban.sta.enums.TaskPriority;
import ir.meysampalizban.sta.enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository repository;
    private final TaskEnrichmentService enrichmentService;

    public TaskService(TaskRepository repository,
                       TaskEnrichmentService enrichmentService) {
        this.repository = repository;
        this.enrichmentService = enrichmentService;
    }

    // ------------------------------------------------------------------ create
    /**
     * وظیفه را ابتدا ذخیره می‌کند، سپس تلاش می‌کند با AI غنی‌سازی کند.
     * اگر AI شکست بخورد، وظیفه با مقادیر پیش‌فرض باقی می‌ماند (enriched=false).
     */
    public TaskResponse create(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title().trim());

        Task saved = repository.save(task);   // تراکنش کوتاه، بدون نگه‌داشتن اتصال در طول تماس AI

        try {
            applySuggestion(saved, enrichmentService.suggest(saved.getTitle()));
            saved = repository.save(saved);
        } catch (Exception ex) {
            log.warn("AI enrichment failed for task {}: {}", saved.getId(), ex.toString());
            // عمداً استثنا را پرتاب نمی‌کنیم — وظیفه کاربر نباید از دست برود
        }

        return toResponse(saved);
    }

    // ------------------------------------------------------------------- read
    @Transactional(readOnly = true)
    public Page<TaskResponse> findAll(TaskStatus status,
                                      TaskPriority priority,
                                      TaskCategory category,
                                      Pageable pageable) {
        return repository.findAll(filter(status, priority, category), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return toResponse(load(id));
    }

    // ----------------------------------------------------------------- update
    @Transactional
    public TaskResponse update(Long id, UpdateTaskRequest request) {
        Task task = load(id);

        if (request.title() != null && !request.title().isBlank()) {
            task.setTitle(request.title().trim());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        // dirty checking در پایان تراکنش، UPDATE را خودکار اجرا می‌کند
        return toResponse(task);
    }

    // ----------------------------------------------------------------- delete
    @Transactional
    public void delete(Long id) {
        repository.delete(load(id));
    }

    // ----------------------------------------------------------------- enrich
    /**
     * اجرای مجدد غنی‌سازی. برخلاف create، اینجا خطای AI به کاربر گزارش می‌شود
     * چون کاربر آگاهانه این عملیات را درخواست کرده است.
     */
    @Transactional
    public TaskResponse enrich(Long id) {
        Task task = load(id);

        try {
            applySuggestion(task, enrichmentService.suggest(task.getTitle()));
        } catch (Exception ex) {
            throw new EnrichmentException("AI enrichment failed for task " + id, ex);
        }

        return toResponse(task);
    }

    // ---------------------------------------------------------------- helpers
    private Task load(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private void applySuggestion(Task task, TaskSuggestion suggestion) {
        if (suggestion == null) {
            return;
        }
        if (suggestion.category() != null) {
            task.setCategory(suggestion.category());
        }
        if (suggestion.priority() != null) {
            task.setPriority(suggestion.priority());
        }
        task.replaceSubTasks(suggestion.subtasks() == null ? List.of() : suggestion.subtasks());
        task.setEnriched(true);
    }

    private Specification<Task> filter(TaskStatus status,
                                       TaskPriority priority,
                                       TaskCategory category) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>(3);
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private TaskResponse toResponse(Task task) {
        List<String> subtaskTitles = task.getSubTasks().stream()
                .map(SubTask::getTitle)
                .toList();

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getCategory(),
                task.getPriority(),
                task.getStatus(),
                subtaskTitles,
                task.isEnriched(),
                task.getCreatedAt()
        );
    }
}