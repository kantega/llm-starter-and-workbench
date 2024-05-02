package no.kantega.openai.fx;

import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import no.hal.fx.adapter.LabelAdapter;
import no.kantega.llm.ModelManager;

@ApplicationScoped
public class FxProviders {

    @Inject
    ModelManager modelManager;

    @Produces
    LabelAdapter<OpenAiEmbeddingModel> labelAdapterForOpenaiEmbeddingModel() {
        return LabelAdapter.forClass(OpenAiEmbeddingModel.class, em -> "Openai embedding model");
    }
    @Produces
    LabelAdapter<OpenAiStreamingChatModel> labelAdapterForOpenaiStreamingChatModel() {
        return LabelAdapter.forClass(OpenAiStreamingChatModel.class, cm -> "Openai %s streaming chat model".formatted(modelManager.getModelName(cm)));
    }
}
