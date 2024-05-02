package no.kantega.huggingface.fx;

import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
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
    LabelAdapter<HuggingFaceEmbeddingModel> labelAdapterForHuggingfaceEmbeddingModel() {
        return LabelAdapter.forClass(HuggingFaceEmbeddingModel.class, em -> "Hugging face %s embedding model".formatted(modelManager.getModelName(em)));
    }

    @Produces
    LabelAdapter<HuggingFaceChatModel> labelAdapterForHuggingfaceChatModel() {
        return LabelAdapter.forClass(HuggingFaceChatModel.class, cm -> "Hugging face %s chat model".formatted(modelManager.getModelName(cm)));
    }
}
