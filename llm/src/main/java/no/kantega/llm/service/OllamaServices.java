package no.kantega.llm.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import no.kantega.llm.util.LabelMap;

@ApplicationScoped
public class OllamaServices {

    String baseUrl = "http://localhost:11434/";

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
    
    @Produces
    List<ChatLanguageModel> getChatLanguageModels() {
        return chatModelNames.stream()
        .map(modelName -> chatModels.getLabeled(modelName, name -> OllamaChatModel.builder()
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
    
    @Produces
    List<StreamingChatLanguageModel> getMistralStreamingChatModel() {
        return streamingChatModelNames.orElse(chatModelNames).stream()
            .map(modelName -> streamingChatModels.getLabeled(modelName, name -> OllamaStreamingChatModel.builder()
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
