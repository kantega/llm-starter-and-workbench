package no.kantega.llm.fx;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import no.hal.fx.adapter.AdapterListView;
import no.hal.fx.adapter.CompositeLabelAdapter;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingTarget;
import no.kantega.llm.fx.IngestorViewController.TextSegmentEmbeddings;

@Dependent
public class EmbeddingsSearchViewController implements BindableView {

    @FXML
    TextArea embeddingsText;

    @FXML
    Button embeddingsSearchAction;

    @Inject
    Instance<LabelAdapter<?>> labelAdapters;

    private List<BindingTarget<?>> bindingTargets;

    @Override
    public List<BindingTarget<?>> getBindingTargets() {
        return this.bindingTargets;
    }

    private Property<TextSegmentEmbeddings> textSegmentEmbeddingsProperty = new SimpleObjectProperty<TextSegmentEmbeddings>();

    @FXML
    ListView<EmbeddingMatch<TextSegment>> matchesListView;

    @FXML
    void initialize() {
        String embeddingsScoreActionTextFormat = embeddingsSearchAction.getText();
        LabelAdapter<EmbeddingModel> labelAdapter = CompositeLabelAdapter.of(this.labelAdapters);
        embeddingsSearchAction.disableProperty().bind(textSegmentEmbeddingsProperty.map(Objects::isNull));
        var computedLabelValue = textSegmentEmbeddingsProperty.map(tse -> embeddingsScoreActionTextFormat.formatted(labelAdapter.getText(tse.embeddingModel())));
        embeddingsSearchAction.textProperty().bind(computedLabelValue.orElse(embeddingsScoreActionTextFormat.formatted("?")));

        AdapterListView.adapt(this.matchesListView, CompositeLabelAdapter.of(this.labelAdapters));

        this.bindingTargets = List.of(
            new BindingTarget<TextSegmentEmbeddings>(embeddingsSearchAction, TextSegmentEmbeddings.class, textSegmentEmbeddingsProperty)
        );
    }

    static EmbeddingStore<TextSegment> getEmbeddingStore(TextSegmentEmbeddings textSegmentEmbeddings) {
        return getEmbeddingStore(textSegmentEmbeddings, null);
    }
    static EmbeddingStore<TextSegment> getEmbeddingStore(TextSegmentEmbeddings textSegmentEmbeddings, BiConsumer<EmbeddingSearchRequest, EmbeddingSearchResult<TextSegment>> searchCallback) {
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>() {
            @Override
            public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
                var result = super.search(request);
                if (searchCallback != null) {
                    searchCallback.accept(request, result);
                }
                return result;
            }
        };
        textSegmentEmbeddings.textSegmentEmbeddings().forEach(textSegmentEmbedding -> embeddingStore.add(textSegmentEmbedding.embedding(), textSegmentEmbedding.textSegment()));
        return embeddingStore;
    }

    @FXML
    void handleEmbeddingsSearch() {
        EmbeddingModel embeddingModel = textSegmentEmbeddingsProperty.getValue().embeddingModel();
        TextSegment textSegment = TextSegment.from(embeddingsText.getText());
        Embedding embedding = embeddingModel.embed(textSegment).content();
        var textSegmentEmbeddings = textSegmentEmbeddingsProperty.getValue();
        EmbeddingStore<TextSegment> embeddingStore = getEmbeddingStore(textSegmentEmbeddings);
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(embedding, textSegmentEmbeddings.textSegmentEmbeddings().size(), 0.7);
        matchesListView.getItems().setAll(matches);
    }
}
