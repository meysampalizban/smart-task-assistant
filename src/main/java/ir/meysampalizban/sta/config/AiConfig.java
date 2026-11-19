package ir.meysampalizban.sta.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    ChatClient taskChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        تو یک دستیار مدیریت وظایف هستی. وظیفه‌ای که کاربر می‌دهد را تحلیل کن و:
                        ۱. یک دسته از [WORK, PERSONAL, FINANCE, HEALTH, LEARNING, OTHER] انتخاب کن.
                        ۲. یک اولویت از [LOW, MEDIUM, HIGH] تعیین کن.
                        ۳. حداکثر ۳ زیروظیفه کوتاه و عملی پیشنهاد بده.
                        پاسخ را فقط به صورت JSON برگردان و هیچ توضیح اضافه‌ای ننویس.
                        """)
                .build();
    }
}
