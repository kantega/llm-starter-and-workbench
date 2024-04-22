package no.kantega.llm.fx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import no.hal.fx.adapter.AdapterListView;
import no.hal.fx.adapter.ChildrenAdapter;
import no.hal.fx.adapter.CompositeLabelAdapter;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingSource;
import no.hal.fx.bindings.BindingTarget;
import no.hal.fx.util.ActionProgressHelper;
import no.kantega.llm.fx.FileSystemDocumentsViewController.FileSystemDocuments;

@Dependent
public class IngestorViewController implements BindableView {

    @FXML
    TextField segmentSizeText;

    @FXML
    TextField segmentOverlapText;

    @FXML
    Button ingestAction;

    @Inject
    Instance<LabelAdapter> labelAdapters;

    private List<BindingTarget<?>> bindingTargets;

    @Override
    public List<BindingTarget<?>> getBindingTargets() {
        return this.bindingTargets;
    }

    private List<BindingSource<?>> bindingSources;

    @Override
    public List<BindingSource<?>> getBindingSources() {
        return this.bindingSources;
    }

    private Property<FileSystemDocuments> documentsModelProperty = new SimpleObjectProperty<FileSystemDocuments>(new FileSystemDocuments(List.of()));
    private Property<EmbeddingModel> embeddingModelProperty = new SimpleObjectProperty<EmbeddingModel>();

    public record TextSegmentEmbeddings(EmbeddingModel embeddingModel, List<TextSegmentEmbedding> textSegmentEmbeddings, Object updateKey) {
        public TextSegmentEmbeddings(EmbeddingModel embeddingModel, List<TextSegmentEmbedding> textSegmentEmbeddings) {
            this(embeddingModel, textSegmentEmbeddings, System.currentTimeMillis());
        }
    }

    private Property<TextSegmentEmbeddings> allTextSegmentEmbeddingsProperty = new SimpleObjectProperty<>();
    private Property<TextSegmentEmbeddings> selectedTextSegmentEmbeddingsProperty = new SimpleObjectProperty<>();

    @FXML
    ListView<Object> embeddingsListView;

    @FXML
    void initialize() {
        String ingestActionTextFormat = ingestAction.getText();
        LabelAdapter labelAdapter = CompositeLabelAdapter.of(this.labelAdapters);
        ingestAction.disableProperty().bind(embeddingModelProperty.map(Objects::isNull));        
        ingestAction.textProperty().bind(Bindings.createStringBinding(() -> {
            var documentCount = documentsModelProperty.getValue().documents().size();
            var embeddingModelText = embeddingModelProperty.map(em -> labelAdapter.getText(em)).orElse("?").getValue();
            return ingestActionTextFormat.formatted(documentCount, embeddingModelText);
        }, documentsModelProperty, embeddingModelProperty));

        allTextSegmentEmbeddingsProperty.subscribe(textSegmentEmbeddings -> {
            selectedTextSegmentEmbeddingsProperty.setValue(textSegmentEmbeddings);
            embeddingsListView.getItems().setAll(textSegmentEmbeddings != null ? textSegmentEmbeddings.textSegmentEmbeddings() : Collections.emptyList());
        });

        AdapterListView.adapt(this.embeddingsListView, CompositeLabelAdapter.of(this.labelAdapters), ChildrenAdapter.forClass(TextSegmentEmbeddings.class, TextSegmentEmbeddings::textSegmentEmbeddings));
        this.embeddingsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.embeddingsListView.getSelectionModel().getSelectedItems().subscribe(() -> {
            var selection = (List<?>) this.embeddingsListView.getSelectionModel().getSelectedItems();
            var allTextSegmentEmbeddings = allTextSegmentEmbeddingsProperty.getValue();
            selectedTextSegmentEmbeddingsProperty.setValue(selection.isEmpty() ? allTextSegmentEmbeddings : new TextSegmentEmbeddings(allTextSegmentEmbeddings.embeddingModel(), (List<TextSegmentEmbedding>) selection));
        });

        this.bindingTargets = List.of(
            new BindingTarget<FileSystemDocuments>(ingestAction, FileSystemDocuments.class, documentsModelProperty),
            new BindingTarget<EmbeddingModel>(ingestAction, EmbeddingModel.class, embeddingModelProperty)
        );
        this.bindingSources = List.of(
            new BindingSource<TextSegmentEmbeddings>(this.embeddingsListView, TextSegmentEmbeddings.class, selectedTextSegmentEmbeddingsProperty)
        );
    }

    private ActionProgressHelper buttonActionProgressHelper = new ActionProgressHelper();

    public record TextSegmentEmbedding(TextSegment textSegment, Embedding embedding) {
    }

    @FXML
    void handleIngest() {
        List<TextSegmentEmbedding> allTextSegments = new ArrayList<>();
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>() {
            public List<String> addAll(List<Embedding> embeddings, List<TextSegment> textSegments) {
                var ids = super.addAll(embeddings, textSegments);
                for (int i = 0; i < textSegments.size(); i++) {
                    allTextSegments.add(new TextSegmentEmbedding(textSegments.get(i), embeddings.get(i)));
                }
                return ids;
            }
        };
        EmbeddingModel embeddingModel = embeddingModelProperty.getValue();
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(Integer.valueOf(segmentSizeText.getText()), Integer.valueOf(segmentOverlapText.getText())))
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build();
        
        buttonActionProgressHelper.performAction(ingestAction,
            () -> {
                ingestor.ingest(documentsModelProperty.getValue().documents());
                return allTextSegments;
            },
            textSegments -> allTextSegmentEmbeddingsProperty.setValue(new TextSegmentEmbeddings(embeddingModel, textSegments)),
            exception -> allTextSegmentEmbeddingsProperty.setValue(new TextSegmentEmbeddings(embeddingModel, Collections.emptyList()))
        );
    }
}
