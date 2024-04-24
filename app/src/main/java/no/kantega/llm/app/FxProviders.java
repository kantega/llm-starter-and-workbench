package no.kantega.llm.app;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import no.hal.fx.adapter.LabelAdapter;
import no.kantega.llm.fx.IngestorViewController.TextSegmentEmbedding;
import no.kantega.llm.service.OpenAiServices;

@ApplicationScoped
public class FxProviders {

    @Inject
    OpenAiServices openAiServices;

    private String textSegmentString(String prefix, TextSegment textSegment) {
        var metadata = textSegment.metadata();
        var source = metadata.get(Document.FILE_NAME);
        if (source != null) {
            return "%s%s #%s\n%s".formatted(prefix, source, metadata.get("index"), textSegment.text());
        } else if (prefix.isEmpty()) {
            return textSegment.text();
        } else {
            return "%s\n%s".formatted(prefix, textSegment.text());
        }
    }

    @Produces
    LabelAdapter<TextSegment> labelAdapterForTextSegment() {
        return LabelAdapter.forClass(TextSegment.class, ts -> textSegmentString("", ts));
    }

    @Produces
    LabelAdapter<TextSegmentEmbedding> labelAdapterForTextSegmentEmbedding() {
        return LabelAdapter.forClass(TextSegmentEmbedding.class, tse -> textSegmentString("", tse.textSegment()));
    }
    
    @Produces
    LabelAdapter<EmbeddingMatch> labelAdapterForEmbeddingMatch() {
        return LabelAdapter.forClass(EmbeddingMatch.class, match -> {
            return textSegmentString("%.2f: ".formatted(match.score()), (TextSegment) match.embedded());
        });
    }

    @Produces
    LabelAdapter<Document> labelAdapterForDocument() {
        return LabelAdapter.forClass(Document.class, document -> {
            String source = document.metadata().get(Document.FILE_NAME);
            if (source == null) {
                source = document.metadata().get(Document.URL);
            }
            return "%s: %s characters".formatted(source, document.text().length());
        });
    }

    @Produces
    LabelAdapter<AllMiniLmL6V2EmbeddingModel> labelAdapterForAllMiniLmL6V2EmbeddingModel() {
        return LabelAdapter.forClass(AllMiniLmL6V2EmbeddingModel.class, ignore -> "Default in-memory embedding model");
    }

    @Produces
    LabelAdapter<OpenAiEmbeddingModel> labelAdapterForOpenAiEmbeddingModel() {
        return LabelAdapter.forClass(OpenAiEmbeddingModel.class, ignore -> "OpenAi's embedding model");
    }
    @Produces
    LabelAdapter<OpenAiChatModel> labelAdapterForOpenAiChatModel() {
        return LabelAdapter.forClass(OpenAiChatModel.class, cm -> "OpenAi %s chat model".formatted(openAiServices.getChatModelName(cm)));
    }
    @Produces
    LabelAdapter<OpenAiStreamingChatModel> labelAdapterForOpenAiStreamingChatModel() {
        return LabelAdapter.forClass(OpenAiStreamingChatModel.class, cm -> "OpenAi %s streaming chat model".formatted(openAiServices.getStreamingChatModelName(cm)));
    }
}
