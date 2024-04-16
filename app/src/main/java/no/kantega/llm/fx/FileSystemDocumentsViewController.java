package no.kantega.llm.fx;

import java.io.File;
import java.util.List;

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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import no.hal.fx.adapter.AdapterListView;
import no.hal.fx.adapter.ChildrenAdapter;
import no.hal.fx.adapter.CompositeLabelAdapter;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingSource;
import no.hal.fx.util.ButtonActionProgressHelper;

@Dependent
public class FileSystemDocumentsViewController implements BindableView {

    @FXML
    TextField documentsFolderPath;

    @FXML
    ListView<Object> fileSystemDocumentsListView;

    public record FileSystemDocuments(List<Document> documents, Object updateKey) {
        public FileSystemDocuments(List<Document> documents) {
            this(documents, System.currentTimeMillis());
        }
    }

    private Property<FileSystemDocuments> fileSystemDocumentsProperty = new SimpleObjectProperty<>(new FileSystemDocuments(List.of()));
    private Property<FileSystemDocuments> selectedDocumentsProperty = new SimpleObjectProperty<>(new FileSystemDocuments(List.of()));

    @Inject
    Instance<LabelAdapter> labelAdapters;

    private List<BindingSource<?>> bindingSources;

    @Override
    public List<BindingSource<?>> getBindingSources() {
        return this.bindingSources;
    }

    @FXML
    void initialize() {
        fileSystemDocumentsProperty.subscribe(fileSystemDocuments -> {
            selectedDocumentsProperty.setValue(fileSystemDocuments);
            fileSystemDocumentsListView.getItems().setAll(fileSystemDocuments.documents());
        });

        AdapterListView.adapt(this.fileSystemDocumentsListView, CompositeLabelAdapter.of(this.labelAdapters), ChildrenAdapter.forClass(FileSystemDocuments.class, FileSystemDocuments::documents));
        this.fileSystemDocumentsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.fileSystemDocumentsListView.getSelectionModel().getSelectedItems().subscribe(() -> {
            var selection = (List<?>) this.fileSystemDocumentsListView.getSelectionModel().getSelectedItems();
            selectedDocumentsProperty.setValue(selection.isEmpty() ? fileSystemDocumentsProperty.getValue() : new FileSystemDocuments((List<Document>) selection));
        });
        this.bindingSources = List.of(
            new BindingSource<FileSystemDocuments>(this.fileSystemDocumentsListView, FileSystemDocuments.class, selectedDocumentsProperty)
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

    CopyDown html2markdownConverter = new CopyDown();
    DocumentParser documentParser = inputStream -> {
        try {
            var soup = Jsoup.parse(inputStream, "UTF-8", "");
            var markdown = html2markdownConverter.convert(soup.outerHtml());
            return Document.from(markdown);
        } catch (Exception e) {
            System.err.println(e);
            throw new RuntimeException(e);
        }
    };

    private ButtonActionProgressHelper buttonActionProgressHelper = new ButtonActionProgressHelper();

    private Alert alert = null;

    @FXML
    void loadDocuments(ActionEvent event) {
        buttonActionProgressHelper.performAction(
            event, () -> {
                List<Document> documents = FileSystemDocumentLoader.loadDocumentsRecursively(documentsFolderPath.getText(), documentParser);
                return new FileSystemDocuments(documents);
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
