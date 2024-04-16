package no.kantega.llm.fx;

import java.util.List;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import no.hal.fx.adapter.AdapterListView;
import no.hal.fx.adapter.ChildrenAdapter;
import no.hal.fx.adapter.CompositeLabelAdapter;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingSource;
import no.hal.fx.bindings.FxBindings;

@Dependent
public class ChatModelsViewController implements BindableView {

    @FXML
    ListView<Object> chatModelsListView;
    @FXML
    ListView<Object> streamingChatModelsListView;

    private ObservableList<Object> chatModels = FXCollections.observableArrayList();
    private ObservableList<Object> streamingChatModels = FXCollections.observableArrayList();

    @Inject
    void setChatModels(Instance<List<ChatLanguageModel>> chatModels) {
        FxBindings.addAll(this.chatModels, chatModels.stream().flatMap(List::stream).toList());
    }
    @Inject
    void setStreamingChatModels(Instance<List<StreamingChatLanguageModel>> streamingChatModels) {
        FxBindings.addAll(this.streamingChatModels, streamingChatModels.stream().flatMap(List::stream).toList());
    }

    @Inject
    Instance<LabelAdapter> labelAdapters;

    private List<BindingSource<?>> bindingSources;

    @Override
    public List<BindingSource<?>> getBindingSources() {
        return this.bindingSources;
    }

    @FXML
    void initialize() {
        this.chatModelsListView.setItems(this.chatModels);
        AdapterListView.adapt(this.chatModelsListView, CompositeLabelAdapter.of(this.labelAdapters), ChildrenAdapter.forChildren(this.chatModels));

        this.streamingChatModelsListView.setItems(this.streamingChatModels);
        AdapterListView.adapt(this.streamingChatModelsListView, CompositeLabelAdapter.of(this.labelAdapters), ChildrenAdapter.forChildren(this.streamingChatModels));
        
        ObservableValue<ChatLanguageModel> chatModelProperty = FxBindings.selectedItemProperty(chatModelsListView.getSelectionModel(), ChatLanguageModel.class);
        ObservableValue<StreamingChatLanguageModel> streamingChatModelProperty = FxBindings.selectedItemProperty(streamingChatModelsListView.getSelectionModel(), StreamingChatLanguageModel.class);

        this.bindingSources = List.of(
            new BindingSource<ChatLanguageModel>(this.chatModelsListView, ChatLanguageModel.class, chatModelProperty),
            new BindingSource<StreamingChatLanguageModel>(this.streamingChatModelsListView, StreamingChatLanguageModel.class, streamingChatModelProperty)
        );
    }
}
