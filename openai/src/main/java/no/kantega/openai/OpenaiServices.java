package no.kantega.openai;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import no.hal.fx.util.LabelMap;

@ApplicationScoped
public class OpenaiServices {

    @ConfigProperty(name = "langchain4j.openai.api-key")
    String openApiKey;

    public String getOpenApiKey() {
        return openApiKey;
    }

    @ConfigProperty(name = "langchain4j.openai.chat-model.temperature")
    double temperature;

    public double getTemperature() {
        return temperature;
    }

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

    private LabelMap<OpenAiChatModelName, StreamingChatLanguageModel> streamingChatModels = new LabelMap<>();

    public StreamingChatLanguageModel withStreamingChatModelLabel(OpenAiChatModelName modelName, Function<OpenAiChatModelName, StreamingChatLanguageModel> chatModelProvider) {
        return streamingChatModels.getLabeled(modelName, chatModelProvider);
    }

    public String getStreamingChatModelName(StreamingChatLanguageModel chatModel) {
        return streamingChatModels.getLabel(chatModel).toString();
    }
}
