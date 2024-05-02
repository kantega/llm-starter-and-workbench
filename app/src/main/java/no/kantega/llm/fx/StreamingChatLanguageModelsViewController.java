package no.kantega.llm.fx;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import jakarta.enterprise.context.Dependent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import no.hal.wb.storedstate.Configurable;

@Dependent
public class StreamingChatLanguageModelsViewController extends ModelsViewController<StreamingChatLanguageModel> implements Configurable {

    public StreamingChatLanguageModelsViewController() {
        super(StreamingChatLanguageModel.class);
    }

    @FXML
    ListView<StreamingChatLanguageModel> streamingChatLanguageModelsListView;
    
    @FXML
    void initialize() {
        super.initialize(streamingChatLanguageModelsListView);
    }

    @Override
    public void configure(JsonNode configuration) {
        // doesn't support any configuration, yet
    }
}
