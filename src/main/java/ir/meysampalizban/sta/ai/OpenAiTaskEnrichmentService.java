package ir.meysampalizban.sta.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAiTaskEnrichmentService implements TaskEnrichmentService {

    private final ChatClient chatClient;

    @Override
    public TaskSuggestion suggest(String title) {
        return chatClient.prompt()
                .user(u -> u.text("این وظیفه را تحلیل کن: {title}")
                        .param("title", title))
                .call()
                .entity(TaskSuggestion.class, spec -> spec.validateSchema());
    }
}