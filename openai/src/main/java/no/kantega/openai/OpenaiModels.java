package no.kantega.openai;

import java.time.Duration;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import no.kantega.llm.ModelConfiguration;
import no.kantega.llm.ModelConfiguration.Named;

@ApplicationScoped
public class OpenaiModels {

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
    List<ModelConfiguration<EmbeddingModel>> getEmbeddingModel() {
        return List.of(new Named<EmbeddingModel>("OpenAi embedding model", OpenAiEmbeddingModel.builder().apiKey(getOpenApiKey()).build()));
    }

    public record StreamingChatModelConfiguration (
        String apiKey,
        OpenAiChatModelName openAiModelName,
        StreamingChatModelOptions options
    ) implements ModelConfiguration<OpenAiStreamingChatModel> {
        
        public StreamingChatModelConfiguration(String apiKey, OpenAiChatModelName openAiModelName) {
            this(apiKey, openAiModelName, null);
        }

        @Override
        public String modelName() {
            return openAiModelName.name();
        }

        @Override
        public OpenAiStreamingChatModel buildModel() {
            var builder = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName());
            if (options != null) {
                builder
                    .temperature(options.temperature)
                    .topP(options.topP)
                    .seed(options.seed);
            }
            return builder.build();
        }
    }

    public record StreamingChatModelOptions (
        double temperature,
        double topP,
        int seed
    ) {
    }
}
