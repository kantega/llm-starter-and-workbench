package no.kantega.ollama;

import java.time.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import no.kantega.llm.ModelConfiguration;

@ApplicationScoped
public class OllamaModels {

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

    public record EmbeddingModelConfiguration (
        String baseUrl,
        String modelName
    ) implements ModelConfiguration<OllamaEmbeddingModel> {

        @Override
        public OllamaEmbeddingModel buildModel() {
            return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
        }
    }

    public record StreamingChatModelConfiguration (
        String baseUrl,
        String modelName,
        StreamingChatModelOptions options
    ) implements ModelConfiguration<OllamaStreamingChatModel> {
        
        public StreamingChatModelConfiguration(String baseUrl, String modelName) {
            this(baseUrl, modelName, null);
        }

        @Override
        public OllamaStreamingChatModel buildModel() {
            var builder = OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName);
            if (options != null) {
                builder
                    .temperature(options.temperature)
                    .topK(options.topK)
                    .topP(options.topP)
                    .repeatPenalty(options.repeatPenalty)
                    .seed(options.seed)
                    .numPredict(options.numPredict)
                    .numCtx(options.numCtx);
            }
            return builder.build();
        }
    }

    public record StreamingChatModelOptions (
        double temperature,
        int topK,
        double topP,
        double repeatPenalty,
        int seed,
        int numPredict,
        int numCtx
    ) {
    }
}
