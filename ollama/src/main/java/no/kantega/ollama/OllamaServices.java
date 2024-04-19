package no.kantega.ollama;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import no.hal.fx.util.LabelMap;

@ApplicationScoped
public class OllamaServices {

    @ConfigProperty(name = "quarkus.rest-client.ollama-api.url")
    String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    @ConfigProperty(name = "langchain4j.ollama.chat-model.temperature")
    double temperature;

    @ConfigProperty(name = "langchain4j.ollama.timeout")
    Duration timeout;

    //

    private LabelMap<String, EmbeddingModel> embeddingModels = new LabelMap<>();
    
    @ConfigProperty(name = "llmwb.ollama.embedding-model.names")
    List<String> embeddingModelNames;
    
    @Produces
    public List<EmbeddingModel> getEmbeddingModels() {
        return embeddingModelNames.stream()
        .map(modelName -> embeddingModels.getLabeled(modelName, name -> OllamaEmbeddingModel.builder()
        .baseUrl(baseUrl)
        .timeout(timeout)
        .modelName(name)
        .build()))
        .toList();
    }
    
    public String getEmbeddingModelName(EmbeddingModel embeddingModel) {
        return embeddingModels.getLabel(embeddingModel);
    }

    private LabelMap<String, ChatLanguageModel> chatModels = new LabelMap<>();

    @ConfigProperty(name = "llmwb.ollama.chat-model.names")
    List<String> chatModelNames;
    
    public String[] getChatModelNames() {
        return chatModelNames.toArray(new String[0]);
    }

    public ChatLanguageModel withChatModelLabel(String modelName, Function<String, ChatLanguageModel> chatModelProvider) {
        return chatModels.getLabeled(modelName, chatModelProvider);
    }

    @Produces
    List<ChatLanguageModel> getChatLanguageModels() {
        return chatModelNames.stream()
        .map(modelName -> withChatModelLabel(modelName, name -> OllamaChatModel.builder()
        .baseUrl(baseUrl)
        .timeout(timeout)
        .modelName(name)
        .temperature(temperature)
        .build()))
        .toList();
    }
    
    public String getChatModelName(ChatLanguageModel chatModel) {
        return chatModels.getLabel(chatModel).toString();
    }

    private LabelMap<String, StreamingChatLanguageModel> streamingChatModels = new LabelMap<>();

    @ConfigProperty(name = "llmwb.ollama.streaming-chat-model.names")
    Optional<List<String>> streamingChatModelNames;
    
    public String[] getStreamingChatModelNames() {
        return streamingChatModelNames.orElse(chatModelNames).toArray(new String[0]);
    }

    public StreamingChatLanguageModel withStreamingChatModelLabel(String modelName, Function<String, StreamingChatLanguageModel> chatModelProvider) {
        return streamingChatModels.getLabeled(modelName, chatModelProvider);
    }

    @Produces
    List<StreamingChatLanguageModel> getMistralStreamingChatModel() {
        return streamingChatModelNames.orElse(chatModelNames).stream()
            .map(modelName -> withStreamingChatModelLabel(modelName, name -> OllamaStreamingChatModel.builder()
            .baseUrl(baseUrl)
            .timeout(timeout)
            .modelName(name)
            .temperature(temperature)
            .build()))
        .toList();
    }

    public String getStreamingChatModelName(StreamingChatLanguageModel chatModel) {
        return streamingChatModels.getLabel(chatModel).toString();
    }
}
