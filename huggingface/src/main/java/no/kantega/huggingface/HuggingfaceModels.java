package no.kantega.huggingface;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import no.kantega.llm.ModelConfiguration;

@ApplicationScoped
public class HuggingfaceModels {

    @ConfigProperty(name = "langchain4j.huggingface.api-key")
    String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    public record ChatModelConfiguration (
        String accessToken,
        String modelName,
        StreamingChatModelOptions options
    ) implements ModelConfiguration<HuggingFaceChatModel> {

        @Override
        public HuggingFaceChatModel buildModel() {
            var builder = HuggingFaceChatModel.builder()
                .accessToken(accessToken)
                .modelId(modelName);
            if (options != null) {
                builder
                    .temperature(options.temperature);
            }
            return builder.build();
        }
    }
    public record StreamingChatModelOptions (
        double temperature
    ) {
    }
}
