package no.kantega.openai.fx;

import dev.langchain4j.model.openai.OpenAiChatModelName;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import no.hal.fx.adapter.LabelAdapter;
import no.kantega.llm.ModelManager;
import no.kantega.openai.OpenaiModels;

@Dependent
public class OpenaiChatModelsViewController {

    @FXML
    ListView<OpenAiChatModelName> openaiModelsListView;

    @FXML
    OpenaiChatModelViewController openaiChatModelViewController;

    @Inject
    Instance<LabelAdapter<?>> labelAdapters;

    @FXML
    void initialize() {
        this.openaiModelsListView.getItems().addAll(OpenAiChatModelName.values());
        // AdapterListView.adapt(this.openaiModelsListView, LabelAdapter.forClass(OpenAiChatModelName.class, OpenAiChatModelName::name));
    }

    @Inject
    ModelManager modelManager;
    
    @Inject
    OpenaiModels openaiModels;

    @FXML
    void createStreamingChatModel() {
        var modelName = openaiModelsListView.getSelectionModel().getSelectedItem();
        modelManager.registerModel(new OpenaiModels.StreamingChatModelConfiguration(openaiModels.getOpenApiKey(), modelName));
    }
}
