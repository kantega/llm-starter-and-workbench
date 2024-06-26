package no.kantega.llm.fx;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.enterprise.context.Dependent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import no.hal.wb.storedstate.Configurable;

@Dependent
public class EmbeddingModelsViewController extends ModelsViewController<EmbeddingModel> implements Configurable {

    public EmbeddingModelsViewController() {
        super(EmbeddingModel.class);
    }

    @FXML
    ListView<EmbeddingModel> embeddingModelsListView;
    
    @FXML
    void initialize() {
        super.initialize(embeddingModelsListView);
    }

    @Override
    public void configure(JsonNode configuration) {
        // doesn't support any configuration, yet
    }
}
