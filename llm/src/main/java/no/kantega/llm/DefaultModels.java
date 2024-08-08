package no.kantega.llm;

import java.util.List;

import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import no.kantega.llm.ModelConfiguration.Named;

@ApplicationScoped
public class DefaultModels {

    @Produces
    List<ModelConfiguration<EmbeddingModel>> getEmbeddingModel() {
        return List.of(new Named<EmbeddingModel>("minimal", new AllMiniLmL6V2EmbeddingModel()));
    }
}
