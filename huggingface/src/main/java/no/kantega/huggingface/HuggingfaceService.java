package no.kantega.huggingface;

import java.util.function.Function;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;
import no.hal.fx.util.LabelMap;

@ApplicationScoped
public class HuggingfaceService {

    private LabelMap<String, EmbeddingModel> embeddingModels = new LabelMap<>();

    public EmbeddingModel withEmbeddingModelLabel(String modelName, Function<String, EmbeddingModel> embeddingModelProvider) {
        return embeddingModels.getLabeled(modelName, embeddingModelProvider);
    }

    public String getEmbeddingModelName(EmbeddingModel embeddingModel) {
        return embeddingModels.getLabel(embeddingModel).toString();
    }

    //

    private LabelMap<String, ChatLanguageModel> chatModels = new LabelMap<>();

    public ChatLanguageModel withChatModelLabel(String modelName, Function<String, ChatLanguageModel> chatModelProvider) {
        var chatModel = chatModelProvider.apply(modelName);
        chatModels.setLabel(modelName, chatModel);
        return chatModel;
    }

    public String getChatModelName(ChatLanguageModel chatModel) {
        return chatModels.getLabel(chatModel).toString();
    }
}
