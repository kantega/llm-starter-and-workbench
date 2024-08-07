package no.kantega.llm.fx;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import no.hal.fx.adapter.AdapterListView;
import no.hal.fx.adapter.CompositeLabelAdapter;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindingSource;
import no.hal.fx.bindings.BindingTarget;
import no.hal.fx.bindings.BindingsSource;
import no.hal.fx.bindings.BindingsTarget;
import no.hal.fx.util.ActionProgressHelper;
import no.hal.fx.util.CopyToClipboardActionCreator;
import no.kantega.llm.fx.UriDocumentsViewController.Documents;

@Dependent
public class IngestorViewController implements BindingsSource, BindingsTarget {

    @FXML
    TextField segmentSizeText;

    @FXML
    TextField segmentOverlapText;

    @FXML
    Button ingestAction;
    @FXML
    Button embedAction;

    private String embedActionTextFormat;

    @Inject
    Instance<LabelAdapter<?>> labelAdapters;

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

    private Property<Documents> documentsModelProperty = new SimpleObjectProperty<Documents>(new Documents(List.of()));
    private Property<EmbeddingModel> embeddingModelProperty = new SimpleObjectProperty<EmbeddingModel>();

    public record TextSegments(List<TextSegment> textSegments, Object updateKey) {
        public TextSegments(List<TextSegment> textSegments) {
            this(textSegments, System.currentTimeMillis());
        }
    }

    public record TextSegmentEmbeddings(EmbeddingModel embeddingModel, List<TextSegmentEmbedding> textSegmentEmbeddings, Object updateKey) {
        public TextSegmentEmbeddings(EmbeddingModel embeddingModel, List<TextSegmentEmbedding> textSegmentEmbeddings) {
            this(embeddingModel, textSegmentEmbeddings, System.currentTimeMillis());
        }
    }

    private Property<TextSegments> textSegmentsProperty = new SimpleObjectProperty<>(new TextSegments(List.of()));

    private Property<TextSegmentEmbeddings> allTextSegmentEmbeddingsProperty = new SimpleObjectProperty<>();

    @FXML
    ListView<TextSegment> textSegmentsListView;

    @FXML
    void initialize() {
        String ingestActionTextFormat = ingestAction.getText();
        ingestAction.textProperty().bind(documentsModelProperty.map(dm -> ingestActionTextFormat.formatted(dm.documents().size())));
        
        this.embedActionTextFormat = embedAction.getText();
        this.emLabelAdapter = CompositeLabelAdapter.of(this.labelAdapters);

        documentsModelProperty.subscribe(this::updateEmbedActionText);
        embeddingModelProperty.subscribe(this::updateEmbedActionText);
        textSegmentsListView.getItems().subscribe(this::textSegmentsUpdated);
        textSegmentsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        textSegmentsListView.getSelectionModel().getSelectedItems().subscribe(this::textSegmentsUpdated);
        CopyToClipboardActionCreator.setContextMenu(textSegmentsListView, listView -> {
            var sb = new StringBuilder();
            for (var textSegment : textSegmentsProperty.getValue().textSegments()) {
                sb.append("\n").append(textSegment.text()).append("\n");
            }
            return sb.toString();
        });

        AdapterListView.adapt(this.textSegmentsListView, CompositeLabelAdapter.of(this.labelAdapters));

        this.bindingTargets = List.of(
            new BindingTarget<Documents>(ingestAction, Documents.class, documentsModelProperty),
            new BindingTarget<EmbeddingModel>(ingestAction, EmbeddingModel.class, embeddingModelProperty)
        );
        this.bindingSources = List.of(
            new BindingSource<TextSegments>(this.textSegmentsListView, TextSegments.class, textSegmentsProperty),
            new BindingSource<TextSegmentEmbeddings>(this.embedAction, TextSegmentEmbeddings.class, allTextSegmentEmbeddingsProperty)
        );
    }

    private LabelAdapter<EmbeddingModel> emLabelAdapter;

    private void textSegmentsUpdated() {
        var textSegments = textSegmentsListView.getSelectionModel().getSelectedItems();
        if (textSegments.isEmpty()) {
            textSegments = textSegmentsListView.getItems();
        }
        textSegmentsProperty.setValue(new TextSegments(textSegments));
        updateEmbedActionText();
    }

    private void updateEmbedActionText() {
    var segmentsCount = textSegmentsProperty.getValue().textSegments().size();
        var embeddingModelText = embeddingModelProperty.map(em -> emLabelAdapter.getText(em)).orElse("?").getValue();
        embedAction.setText(this.embedActionTextFormat.formatted(segmentsCount, embeddingModelText));
    }

    private ActionProgressHelper buttonActionProgressHelper = new ActionProgressHelper();

    public record TextSegmentEmbedding(TextSegment textSegment, Embedding embedding) {
    }

    @FXML
    void handleIngest(ActionEvent event) {
        var documentSplitter = DocumentSplitters.recursive(Integer.valueOf(segmentSizeText.getText()), Integer.valueOf(segmentOverlapText.getText()));
        var documents = documentsModelProperty.getValue().documents();
        buttonActionProgressHelper.performAction(event,
            () -> {
                List<TextSegment> textSegments = new ArrayList<>();
                for (var document : documents) {
                    try {
                        textSegments.addAll(documentSplitter.split(document));
                    } catch (Exception ex) {
                        logger.warn("Exception splitting document: " + ex, ex);
                    }
                }
                return textSegments;
            },
            textSegments -> textSegmentsListView.getItems().setAll(textSegments),
            ex -> {
                logger.warn("Exception splitting documents: " + ex, ex);
                textSegmentsListView.getItems().clear();
            }
        );
    }

    @Inject
    Logger logger;
    
    @FXML
    void handleEmbed(ActionEvent event) {
        List<TextSegment> textSegments = new ArrayList<>(textSegmentsListView.getSelectionModel().getSelectedItems());
        if (textSegments.isEmpty()) {
            textSegments.addAll(textSegmentsListView.getItems());
        }
        EmbeddingModel embeddingModel = embeddingModelProperty.getValue();
        buttonActionProgressHelper.performActions(event, textSegments.size(), progress -> {
            try {
                var textSegmentEmbeddings = new ArrayList<TextSegmentEmbedding>();
                for (int i = 0; i < textSegments.size(); i++) {
                    var textSegment = textSegments.get(i);
                    var embedding = embeddingModel.embed(textSegment).content();
                    textSegmentEmbeddings.add(new TextSegmentEmbedding(textSegment, embedding));
                    progress.call(i + 1);
                }
                progress.call(null);
                Platform.runLater(() -> allTextSegmentEmbeddingsProperty.setValue(new TextSegmentEmbeddings(embeddingModel, textSegmentEmbeddings)));
            } catch (Exception e) {
                logger.error("Error embedding text segments: " + e, e);
                progress.call(null);
                return;
            }
        });
    }
}
