package no.kantega.ollama.fx;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import no.hal.fx.adapter.AdapterListView;
import no.hal.fx.adapter.LabelAdapter;
import no.kantega.llm.ModelManager;
import no.kantega.ollama.OllamaModels;
import no.kantega.ollama.rest.OllamaApi;

@Dependent
public class OllamaModelsViewController {

    @FXML
    ListView<OllamaApi.Model> ollamaModelsListView;

    @FXML
    TextArea ollamaModelDetailsText;

    @FXML
    OllamaChatModelViewController ollamaChatModelViewController;

    @FXML
    Button embeddingModelAction;

    private Property<EmbeddingModel> embeddingModelProperty = new SimpleObjectProperty<>();

    // @FXML
    // Button chatModelAction;

    @FXML
    Button streamingChatModelAction;

    @Inject
    Instance<LabelAdapter<?>> labelAdapters;

    @FXML
    void initialize() {
        AdapterListView.adapt(this.ollamaModelsListView, LabelAdapter.forClass(OllamaApi.Model.class, OllamaApi.Model::name));
        this.ollamaModelsListView.getSelectionModel().selectedItemProperty().addListener((prop, oldValue, newValue) -> {
            this.ollamaModelDetailsText.setText("");
            if (newValue instanceof OllamaApi.Model model) {
                Platform.runLater(() -> {
                    var info = ollamaApi.getModelInfo(new OllamaApi.ShowParams(model.name()));
                    this.ollamaModelDetailsText.setText("%s\n%s".formatted(info.modelfile(), info.details()));
                });
            };
        });

        Platform.runLater(this::refreshChatModels);
    }

    @RestClient
    OllamaApi ollamaApi;

    @FXML
    void refreshChatModels() {
        var models = ollamaApi.getModels();
        ollamaModelsListView.getItems().setAll(models.models());
        var modelNames = models.models().stream().map(OllamaApi.Model::name).toList();
        ollamaChatModelViewController.updateChatModelChoices(modelNames);
    }

    @Inject
    ModelManager modelManager;

    @Inject
    OllamaModels ollamaModels;

    @FXML
    void createEmbeddingModel() {
        var modelName = ollamaModelsListView.getSelectionModel().getSelectedItem().name();
        modelManager.registerModel(new OllamaModels.EmbeddingModelConfiguration(ollamaModels.getBaseUrl(), modelName));
    }
    
    @FXML
    void createStreamingChatModel() {
        var modelName = ollamaModelsListView.getSelectionModel().getSelectedItem().name();
        modelManager.registerModel(new OllamaModels.StreamingChatModelConfiguration(ollamaModels.getBaseUrl(), modelName));
    }
}
