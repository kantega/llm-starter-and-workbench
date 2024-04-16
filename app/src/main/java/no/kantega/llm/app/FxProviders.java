package no.kantega.llm.app;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import no.hal.fx.adapter.LabelAdapter;
import no.kantega.llm.fx.IngestorViewController.TextSegmentEmbedding;
import no.kantega.llm.service.OllamaServices;
import no.kantega.llm.service.OpenAiServices;

@ApplicationScoped
public class FxProviders {

    @Inject
    OllamaServices ollamaServices;

    @Inject
    OpenAiServices openAiServices;

    private String textSegmentString(String prefix, TextSegment textSegment) {
        var metadata = textSegment.metadata();
        var source = metadata.get("file_name");
        if (source != null) {
            return "%s%s #%s\n%s".formatted(prefix, source, metadata.get("index"), textSegment.text());
        } else if (prefix.isEmpty()) {
            return textSegment.text();
        } else {
            return "%s\n%s".formatted(prefix, textSegment.text());
        }
    }

    @Produces
    LabelAdapter labelAdapterForTextSegmentEmbedding() {
        return LabelAdapter.forClass(TextSegmentEmbedding.class, tse -> textSegmentString("", tse.textSegment()));
    }
    
    @Produces
    LabelAdapter labelAdapterForEmbeddingMatch() {
        return LabelAdapter.forClass(EmbeddingMatch.class, match -> {
            return textSegmentString("%.2f: ".formatted(match.score()), (TextSegment) match.embedded());
        });
    }

    @Produces
    LabelAdapter labelAdapterForDocument() {
        return LabelAdapter.forClass(Document.class, document ->
            "%s: %s characters".formatted(document.metadata().get("file_name"), document.text().length())
        );
    }

    @Produces
    LabelAdapter labelAdapterForAllMiniLmL6V2EmbeddingModel() {
        return LabelAdapter.forClass(AllMiniLmL6V2EmbeddingModel.class, ignore -> "Default in-memory embedding model");
    }

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
    LabelAdapter labelAdapterForOpenAiEmbeddingModel() {
        return LabelAdapter.forClass(OpenAiEmbeddingModel.class, ignore -> "OpenAi's embedding model");
    }
    @Produces
    LabelAdapter labelAdapterForOpenAiChatModel() {
        return LabelAdapter.forClass(OpenAiChatModel.class, cm -> "OpenAi %s chat model".formatted(openAiServices.getChatModelName(cm)));
    }
    @Produces
    LabelAdapter labelAdapterForOpenAiStreamingChatModel() {
        return LabelAdapter.forClass(OpenAiStreamingChatModel.class, cm -> "OpenAi %s streaming chat model".formatted(openAiServices.getStreamingChatModelName(cm)));
    }
}
