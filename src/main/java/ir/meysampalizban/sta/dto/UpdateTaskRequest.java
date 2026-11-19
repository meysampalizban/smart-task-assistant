package ir.meysampalizban.sta.dto;

import ir.meysampalizban.sta.enums.TaskPriority;
import ir.meysampalizban.sta.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @Size(max = 255) String title,
        TaskStatus status,
        TaskPriority priority
) {}
