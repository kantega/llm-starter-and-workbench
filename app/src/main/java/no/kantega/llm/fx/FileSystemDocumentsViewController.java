package no.kantega.llm.fx;

import java.io.File;
import java.util.List;

import org.jboss.logging.Logger;
import org.jsoup.Jsoup;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import io.github.furstenheim.CopyDown;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import no.hal.fx.adapter.AdapterListView;
import no.hal.fx.adapter.CompositeLabelAdapter;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingSource;
import no.hal.fx.util.ActionProgressHelper;
import no.kantega.llm.fx.UriDocumentsViewController.Documents;

@Dependent
public class FileSystemDocumentsViewController implements BindableView {

    @FXML
    TextField documentsFolderPath;

    @FXML
    ListView<Document> fileSystemDocumentsListView;

    private Property<Documents> fileSystemDocumentsProperty = new SimpleObjectProperty<>(new Documents(List.of()));

    @Inject
    Instance<LabelAdapter<?>> labelAdapters;

    private List<BindingSource<?>> bindingSources;

    @Override
    public List<BindingSource<?>> getBindingSources() {
        return this.bindingSources;
    }

    @FXML
    void initialize() {
        fileSystemDocumentsProperty.subscribe(fileSystemDocuments -> fileSystemDocumentsListView.getItems().setAll(fileSystemDocuments.documents()));

        AdapterListView.adapt(this.fileSystemDocumentsListView, CompositeLabelAdapter.of(this.labelAdapters));
        this.bindingSources = List.of(
            new BindingSource<Documents>(this.fileSystemDocumentsListView, Documents.class, fileSystemDocumentsProperty)
        );
    }

    private DirectoryChooser directoryChooser = new DirectoryChooser();

    @FXML
    void browseDocumentsFolder() {
        directoryChooser.setTitle("Select the folder ");
        String currentPath = documentsFolderPath.getText();
        if (! currentPath.isBlank()) {
            directoryChooser.setInitialDirectory(new File(currentPath));
        }
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            documentsFolderPath.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @Inject
    Logger logger;

    @Inject
    DocumentParser documentParser;

    private ActionProgressHelper buttonActionProgressHelper = new ActionProgressHelper();

    private Alert alert = null;

    @FXML
    void loadDocuments(ActionEvent event) {
        buttonActionProgressHelper.performAction(
            event, () -> {
                List<Document> documents = FileSystemDocumentLoader.loadDocumentsRecursively(documentsFolderPath.getText(), documentParser);
                return new Documents(documents);
            },
            documents -> fileSystemDocumentsProperty.setValue(documents),
            exception -> {
                if (alert == null) {
                    alert = new Alert(Alert.AlertType.ERROR);
                }
                alert.setTitle("Exception when loading documents");
                alert.setHeaderText(exception.getClass().getSimpleName());
                alert.setContentText(exception.getMessage());
                alert.showAndWait();        
            }
        );
    }
}
