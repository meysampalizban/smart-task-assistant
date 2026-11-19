package ir.meysampalizban.sta.dto;

import ir.meysampalizban.sta.enums.TaskCategory;
import ir.meysampalizban.sta.enums.TaskPriority;
import ir.meysampalizban.sta.enums.TaskStatus;

import java.time.Instant;
import java.util.List;

public record TaskResponse(
        Long id,
        String title,
        TaskCategory category,
        TaskPriority priority,
        TaskStatus status,
        List<String> subtasks,
        boolean enriched,
        Instant createdAt
)  {}
