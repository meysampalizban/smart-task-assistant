package ir.meysampalizban.sta.ai;

import ir.meysampalizban.sta.enums.TaskCategory;
import ir.meysampalizban.sta.enums.TaskPriority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TaskEnrichmentService {
    TaskSuggestion suggest(String title);

    record TaskSuggestion(
            TaskCategory category,
            TaskPriority priority,
            List<String> subtasks
    ) {}
}
