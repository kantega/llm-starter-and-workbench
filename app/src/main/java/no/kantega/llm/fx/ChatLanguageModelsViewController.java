package no.kantega.llm.fx;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.model.chat.ChatLanguageModel;
import jakarta.enterprise.context.Dependent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import no.hal.wb.storedstate.Configurable;

@Dependent
public class ChatLanguageModelsViewController extends ModelsViewController<ChatLanguageModel> implements Configurable {

    public ChatLanguageModelsViewController() {
        super(ChatLanguageModel.class);
    }

    @FXML
    ListView<ChatLanguageModel> chatLanguageModelsListView;
    
    @FXML
    void initialize() {
        super.initialize(chatLanguageModelsListView);
    }

    @Override
    public void configure(JsonNode configuration) {
        // doesn't support any configuration, yet
    }
}
