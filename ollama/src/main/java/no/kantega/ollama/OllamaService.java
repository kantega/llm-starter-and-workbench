package no.kantega.ollama;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import no.hal.fx.util.LabelMap;

@ApplicationScoped
public class OllamaService {

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
    
    public EmbeddingModel withEmbeddingModelLabel(String modelName, Function<String, EmbeddingModel> embeddingModelProvider) {
        return embeddingModels.getLabeled(modelName, embeddingModelProvider);
    }

    public String getEmbeddingModelName(EmbeddingModel embeddingModel) {
        return embeddingModels.getLabel(embeddingModel);
    }

    @Produces
    List<EmbeddingModel> getEmbeddingModels() {
        return List.of(withEmbeddingModelLabel("nomic-embed-text", name -> OllamaEmbeddingModel.builder()
            .baseUrl(getBaseUrl())
            .modelName(name)
            .build()
        ));
    }

    private LabelMap<String, ChatLanguageModel> chatModels = new LabelMap<>();

    public ChatLanguageModel withChatModelLabel(String modelName, Function<String, ChatLanguageModel> chatModelProvider) {
        return chatModels.getLabeled(modelName, chatModelProvider);
    }

    public String getChatModelName(ChatLanguageModel chatModel) {
        return chatModels.getLabel(chatModel).toString();
    }

    private LabelMap<String, StreamingChatLanguageModel> streamingChatModels = new LabelMap<>();

    public StreamingChatLanguageModel withStreamingChatModelLabel(String modelName, Function<String, StreamingChatLanguageModel> chatModelProvider) {
        return streamingChatModels.getLabeled(modelName, chatModelProvider);
    }

    public String getStreamingChatModelName(StreamingChatLanguageModel chatModel) {
        return streamingChatModels.getLabel(chatModel).toString();
    }
}
