package no.kantega.huggingface.fx;

import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import no.hal.fx.adapter.LabelAdapter;
import no.kantega.huggingface.HuggingfaceService;

@ApplicationScoped
public class FxProviders {

    @Inject
    HuggingfaceService huggingfaceService;

    @Produces
    LabelAdapter<HuggingFaceEmbeddingModel> labelAdapterForHuggingfaceEmbeddingModel() {
        return LabelAdapter.forClass(HuggingFaceEmbeddingModel.class, em -> "Hugging face %s embedding model".formatted(huggingfaceService.getEmbeddingModelName(em)));
    }

    @Produces
    LabelAdapter<HuggingFaceChatModel> labelAdapterForHuggingfaceChatModel() {
        return LabelAdapter.forClass(HuggingFaceChatModel.class, cm -> "Hugging face %s chat model".formatted(huggingfaceService.getChatModelName(cm)));
    }
}
