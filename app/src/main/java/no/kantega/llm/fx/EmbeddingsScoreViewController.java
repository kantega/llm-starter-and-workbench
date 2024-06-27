package no.kantega.llm.fx;

import java.util.List;
import java.util.Objects;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import no.hal.fx.adapter.AdapterListView;
import no.hal.fx.adapter.CompositeLabelAdapter;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindingTarget;
import no.hal.fx.bindings.BindingsTarget;

@Dependent
public class EmbeddingsScoreViewController implements BindingsTarget {

    @FXML
    TextArea embeddingsText1;

    @FXML
    TextArea embeddingsText2;

    @FXML
    Button embeddingsScoreAction;

    @Inject
    Instance<LabelAdapter<?>> labelAdapters;

    private List<BindingTarget<?>> bindingTargets;

    @Override
    public List<BindingTarget<?>> getBindingTargets() {
        return this.bindingTargets;
    }

    private Property<EmbeddingModel> embeddingModelProperty = new SimpleObjectProperty<EmbeddingModel>();

    @FXML
    ListView<EmbeddingMatch<TextSegment>> matchesListView;

    private ObservableList<EmbeddingMatch<TextSegment>> matchesList = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        String embeddingsScoreActionTextFormat = embeddingsScoreAction.getText();
        LabelAdapter<EmbeddingModel> labelAdapter = CompositeLabelAdapter.of(this.labelAdapters);
        embeddingsScoreAction.disableProperty().bind(embeddingModelProperty.map(Objects::isNull));
        var computedLabelValue = embeddingModelProperty.map(em -> embeddingsScoreActionTextFormat.formatted(labelAdapter.getText(em)));
        embeddingsScoreAction.textProperty().bind(computedLabelValue.orElse(embeddingsScoreActionTextFormat.formatted("?")));

        this.matchesListView.setItems(this.matchesList);
        AdapterListView.adapt(this.matchesListView, CompositeLabelAdapter.of(this.labelAdapters));

        this.bindingTargets = List.of(
            new BindingTarget<EmbeddingModel>(embeddingsScoreAction, EmbeddingModel.class, embeddingModelProperty)
        );
    }

    @FXML
    void handleEmbeddingsScore() {
        EmbeddingModel embeddingModel = embeddingModelProperty.getValue();
        TextSegment ts1 = TextSegment.from(embeddingsText1.getText());
        Embedding embedding1 = embeddingModel.embed(ts1).content();
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        var lines = embeddingsText2.getText().split("\n");
        for (var line : lines) {
            TextSegment ts2 = TextSegment.from(line);
            Embedding embedding2 = embeddingModel.embed(ts2).content();
            embeddingStore.add(embedding2, ts2);
        }
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(embedding1, lines.length);
        matchesListView.getItems().setAll(matches);
    }
}
