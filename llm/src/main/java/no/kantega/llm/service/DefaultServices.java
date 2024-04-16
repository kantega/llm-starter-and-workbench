package no.kantega.llm.service;

import java.util.List;

import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class DefaultServices {

    @Produces
    List<EmbeddingModel> getEmbeddingModel() {
        return List.of(new AllMiniLmL6V2EmbeddingModel());
    }
}
