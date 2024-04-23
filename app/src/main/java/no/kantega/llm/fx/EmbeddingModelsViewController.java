package no.kantega.llm.fx;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
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
import no.hal.wb.storedstate.Configurable;

@Dependent
public class EmbeddingModelsViewController implements BindableView, Configurable {

    @FXML
    ListView<EmbeddingModel> embeddingModelsListView;

    private ObservableList<EmbeddingModel> embeddingModels = FXCollections.observableArrayList();

    @Inject
    void setEmbeddingModels(Instance<List<EmbeddingModel>> embeddingModels) {
        FxBindings.setAll(this.embeddingModels, embeddingModels.stream().flatMap(List::stream).toList());
    }

    @Inject
    Instance<LabelAdapter<?>> labelAdapters;

    private List<BindingSource<?>> bindingSources;

    @Override
    public List<BindingSource<?>> getBindingSources() {
        return this.bindingSources;
    }

    @FXML
    void initialize() {
        this.embeddingModelsListView.setItems(this.embeddingModels);
        AdapterListView.adapt(this.embeddingModelsListView, CompositeLabelAdapter.of(this.labelAdapters), ChildrenAdapter.forChildren(this.embeddingModels));

        this.bindingSources = List.of(
            new BindingSource<EmbeddingModel>(this.embeddingModelsListView, EmbeddingModel.class, FxBindings.selectedItemProperty(embeddingModelsListView, EmbeddingModel.class))
        );
    }

    @Override
    public void configure(JsonNode configuration) {
        // doesn't support any configuration, yet
    }
}
