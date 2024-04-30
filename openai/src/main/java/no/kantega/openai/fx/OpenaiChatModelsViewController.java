package no.kantega.openai.fx;

import java.util.List;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingSource;
import no.kantega.openai.OpenaiService;

@Dependent
public class OpenaiChatModelsViewController implements BindableView {

    @FXML
    ListView<OpenAiChatModelName> openaiModelsListView;

    @FXML
    OpenaiChatModelViewController openaiChatModelViewController;

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
        this.openaiModelsListView.getItems().addAll(OpenAiChatModelName.values());
        // AdapterListView.adapt(this.openaiModelsListView, LabelAdapter.forClass(OpenAiChatModelName.class, OpenAiChatModelName::name));

        this.bindingSources = List.of(
            // new BindingSource<ChatLanguageModel>(this.chatModelAction, ChatLanguageModel.class, chatModelProperty),
            new BindingSource<StreamingChatLanguageModel>(this.streamingChatModelAction, StreamingChatLanguageModel.class, streamingChatModelProperty)
        );
    }

    @Inject
    OpenaiService openaiServices;

    // @FXML
    // void createAndUpdateChatModel() {
    //     var modelName = ((OpenaiApi.Model) openaiModelsListView.getSelectionModel().getSelectedItem()).name();
    //     var chatModel = openaiServices.withChatModelLabel(modelName, name -> OpenaiChatModel.builder()
    //         .baseUrl(openaiServices.getBaseUrl())
    //         .modelName(name)
    //         .build()
    //     );
    //     chatModelProperty.setValue(chatModel);
    // }
    
    @FXML
    void createAndUpdateStreamingChatModel() {
        var modelName = openaiModelsListView.getSelectionModel().getSelectedItem();
        var chatModel = openaiServices.withStreamingChatModelLabel(modelName, name -> OpenAiStreamingChatModel.builder()
            .apiKey(openaiServices.getOpenApiKey())
            .modelName(name)
            .build()
        );
        streamingChatModelProperty.setValue(chatModel);
    }
}
