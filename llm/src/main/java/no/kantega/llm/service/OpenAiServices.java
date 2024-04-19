package no.kantega.llm.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import no.hal.fx.util.LabelMap;

@ApplicationScoped
public class OpenAiServices {

    @ConfigProperty(name = "langchain4j.openai.api-key")
    String openApiKey;

    @ConfigProperty(name = "langchain4j.openai.chat-model.temperature")
    double temperature;

    @ConfigProperty(name = "langchain4j.openai.timeout")
    Duration timeout;

    @Produces
    List<EmbeddingModel> getEmbeddingModels() {
        return List.of(OpenAiEmbeddingModel.builder()
            .apiKey(openApiKey)
            .timeout(timeout)
            .build()
        );
    }

    private LabelMap<OpenAiChatModelName, ChatLanguageModel> chatModels = new LabelMap<>();

    @ConfigProperty(name = "llmwb.openai.chat-model.names")
    List<String> chatModelNames;

    private Map<String, OpenAiChatModelName> modelNames = Stream.of(OpenAiChatModelName.values()).collect(Collectors.toMap(OpenAiChatModelName::toString, cmn -> cmn));

    @Produces
    List<ChatLanguageModel> getChatLanguageModels() {
        return chatModelNames.stream()
            .map(modelNames::get)
            .map(modelName -> chatModels.getLabeled(modelName, name -> OpenAiChatModel.builder()
                .apiKey(openApiKey)
                .timeout(timeout)
                .modelName(name)
                .temperature(temperature)
                .logRequests(true)
                .logResponses(true)
                .build()))
            .toList();
    }

    public String getChatModelName(ChatLanguageModel chatModel) {
        return chatModels.getLabel(chatModel).toString();
    }
    
    private LabelMap<OpenAiChatModelName, StreamingChatLanguageModel> streamingChatModels = new LabelMap<>();
    
    @ConfigProperty(name = "llmwb.openai.streaming-chat-model.names")
    Optional<List<String>> streamingChatModelNames;

    @Produces
    List<StreamingChatLanguageModel> getStreamingChatLanguageModels() {
        return streamingChatModelNames.orElse(chatModelNames).stream()
            .map(modelNames::get)
            .map(modelName -> streamingChatModels.getLabeled(modelName, name -> OpenAiStreamingChatModel.builder()
                .apiKey(openApiKey)
                .timeout(timeout)
                .modelName(name)
                .temperature(temperature)
                .logRequests(true)
                .logResponses(true)
                .build()))
            .toList();
    }

    public String getStreamingChatModelName(StreamingChatLanguageModel chatModel) {
        return streamingChatModels.getLabel(chatModel).toString();
    }
}
