package no.kantega.llm.fx;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.RelevanceScore;
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

    private record SimilarityMatch(EmbeddingMatch<TextSegment> match, double cosineSimilarity, double distanceSimilarity) {
    }

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

    private TextSegment getTextFragment1() {
        return TextSegment.from(embeddingsText1.getText());
    }

    private List<String> getTextFragments() {
        return List.of(embeddingsText2.getText().split("\n"));
    }

    @FXML
    void handleEmbeddingsScore() {
        EmbeddingModel embeddingModel = embeddingModelProperty.getValue();
        Embedding embedding1 = embeddingModel.embed(getTextFragment1()).content();
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        var lines = getTextFragments();
        for (var line : lines) {
            TextSegment ts2 = TextSegment.from(line);
            Embedding embedding2 = embeddingModel.embed(ts2).content();
            embeddingStore.add(embedding2, ts2);
        }
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(embedding1, lines.size());
        matchesListView.getItems().setAll(matches);
    }

    private static final BiFunction<Embedding, Embedding, Double> COSINE_SIMILARITY = (emb1, emb2) -> RelevanceScore.fromCosineSimilarity(CosineSimilarity.between(emb1, emb2));

    void handleMetricEmbeddingsScore(BiFunction<Embedding, Embedding, Double> metric) {
        EmbeddingModel embeddingModel = embeddingModelProperty.getValue();
        Embedding embedding1 = embeddingModel.embed(getTextFragment1()).content();
        var matches = getTextFragments().stream()
            .map(line -> TextSegment.from(line))
            .map(ts -> {
                var tse = embeddingModel.embed(ts).content();
                var score = metric.apply(embedding1, tse);
                EmbeddingMatch<TextSegment> match = new EmbeddingMatch<TextSegment>(score, "embeddingId", tse, ts);
                return match;
            })
            .sorted((match1, match2) -> Double.compare(match2.score(), match1.score()))
            .toList();
        matchesListView.getItems().setAll(matches);
    }

    void handleCosineSimilarityScore() {
        handleMetricEmbeddingsScore(COSINE_SIMILARITY);
    }
}
