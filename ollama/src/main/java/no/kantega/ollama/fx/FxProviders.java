package no.kantega.ollama.fx;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import no.hal.fx.adapter.ChildrenAdapter;
import no.hal.fx.adapter.LabelAdapter;
import no.kantega.ollama.OllamaServices;
import no.kantega.ollama.rest.OllamaApi;

@ApplicationScoped
public class FxProviders {

    @Inject
    OllamaServices ollamaServices;

    @Produces
    LabelAdapter labelAdapterForOllamaEmbeddingModel() {
        return LabelAdapter.forClass(OllamaEmbeddingModel.class, em -> "Ollama %s embedding model".formatted(ollamaServices.getEmbeddingModelName(em)));
    }
    @Produces
    LabelAdapter labelAdapterForOllamaChatModel() {
        return LabelAdapter.forClass(OllamaChatModel.class, cm -> "Ollama %s chat model".formatted(ollamaServices.getChatModelName(cm)));
    }
    @Produces
    LabelAdapter labelAdapterForOllamaStreamingChatModel() {
        return LabelAdapter.forClass(OllamaStreamingChatModel.class, cm -> "Ollama %s streaming chat model".formatted(ollamaServices.getStreamingChatModelName(cm)));
    }

    @Produces
    LabelAdapter labelAdapterForOllamaModel() {
        return LabelAdapter.forClass(OllamaApi.Model.class, m -> "Ollama %s model".formatted(m.name()));
    }
    @Produces
    ChildrenAdapter childrenAdapterForOllamaModels() {
        return ChildrenAdapter.forClass(OllamaApi.Models.class, OllamaApi.Models::models);
    }
}
