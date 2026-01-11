package com.notetaking.summarizer.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.ContentBlock;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClaudeService {

    @Value("${claude.api-key:}")
    private String apiKey;

    private AnthropicClient client;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_CLAUDE_API_KEY_HERE")) {
            client = AnthropicOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();
            log.info("Claude API client initialized");
        } else {
            log.warn("Claude API key not configured - summarization disabled");
        }
    }

    public String summarize(String noteTitle, String noteContent) {
        log.info("Summarize called. Client initialized: {}", client != null);

        if (client == null) {
            log.warn("Claude client is null - skipping summarization");
            return null;
        }

        try {
            log.info("Calling Claude API for summarization...");
            String prompt = String.format(
                "Please provide a brief summary (2-3 sentences) of the following note:\n\nTitle: %s\n\nContent: %s",
                noteTitle, noteContent
            );

            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.CLAUDE_3_5_HAIKU_LATEST)
                    .maxTokens(200)
                    .addUserMessage(prompt)
                    .build();

            Message response = client.messages().create(params);
            log.info("Claude API response received");

            StringBuilder result = new StringBuilder();
            for (ContentBlock block : response.content()) {
                block.text().ifPresent(textBlock -> result.append(textBlock.text()));
            }

            String summary = result.length() > 0 ? result.toString() : null;
            log.info("Summary generated: {}", summary != null ? "yes" : "no");
            return summary;
        } catch (Exception e) {
            log.error("Error calling Claude API: {}", e.getMessage(), e);
            return null;
        }
    }
}