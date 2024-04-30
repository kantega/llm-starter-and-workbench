package no.kantega.ollama.fx;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
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
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingSource;
import no.kantega.ollama.OllamaService;
import no.kantega.ollama.rest.OllamaApi;

@Dependent
public class OllamaModelsViewController implements BindableView {

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

    // private Property<ChatLanguageModel> chatModelProperty = new SimpleObjectProperty<>();
    private Property<StreamingChatLanguageModel> streamingChatModelProperty = new SimpleObjectProperty<>();

    @Inject
    Instance<LabelAdapter<?>> labelAdapters;

    private List<BindingSource<?>> bindingSources;

    @Override
    public List<BindingSource<?>> getBindingSources() {
        return this.bindingSources;
    }

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

        this.bindingSources = List.of(
            new BindingSource<EmbeddingModel>(this.embeddingModelAction, EmbeddingModel.class, embeddingModelProperty),
            // new BindingSource<ChatLanguageModel>(this.chatModelAction, ChatLanguageModel.class, chatModelProperty),
            new BindingSource<StreamingChatLanguageModel>(this.streamingChatModelAction, StreamingChatLanguageModel.class, streamingChatModelProperty)
        );
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
    OllamaService ollamaServices;

    // @FXML
    // void createAndUpdateChatModel() {
    //     var modelName = ((OllamaApi.Model) ollamaModelsListView.getSelectionModel().getSelectedItem()).name();
    //     var chatModel = ollamaServices.withChatModelLabel(modelName, name -> OllamaChatModel.builder()
    //         .baseUrl(ollamaServices.getBaseUrl())
    //         .modelName(name)
    //         .build()
    //     );
    //     chatModelProperty.setValue(chatModel);
    // }
    
    @FXML
    void createAndUpdateEmbeddingModel() {
        var modelName = ollamaModelsListView.getSelectionModel().getSelectedItem().name();
        var embeddingModel = ollamaServices.withEmbeddingModelLabel(modelName, name -> OllamaEmbeddingModel.builder()
            .baseUrl(ollamaServices.getBaseUrl())
            .modelName(name)
            .build()
        );
        embeddingModelProperty.setValue(embeddingModel);
    }
    
    @FXML
    void createAndUpdateStreamingChatModel() {
        var modelName = ollamaModelsListView.getSelectionModel().getSelectedItem().name();
        var chatModel = ollamaServices.withStreamingChatModelLabel(modelName, name -> OllamaStreamingChatModel.builder()
            .baseUrl(ollamaServices.getBaseUrl())
            .modelName(name)
            .build()
        );
        streamingChatModelProperty.setValue(chatModel);
    }
}