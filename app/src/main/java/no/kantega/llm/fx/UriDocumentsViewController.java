package no.kantega.llm.fx;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jboss.logging.Logger;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapParser;
import crawlercommons.sitemaps.SiteMapURL;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.stage.DirectoryChooser;
import no.hal.fx.adapter.AdapterListView;
import no.hal.fx.adapter.CompositeLabelAdapter;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindingSource;
import no.hal.fx.bindings.BindingsSource;
import no.hal.fx.util.ActionProgressHelper;
import no.kantega.llm.docparse.AbstractJSoupDocumentParser;

@Dependent
public class UriDocumentsViewController implements BindingsSource {

    @FXML
    TextField uriText;
    
    @FXML
    Button scanForUrisAction;

    @FXML
    TextInputControl pathFilterText;
    @FXML
    TextInputControl extensionFilterText;

    @FXML
    ListView<URI> uriListView;

    @FXML
    ComboBox<String> documentParserSelector;

    @FXML
    TextInputControl contentSelectorText;

    @FXML
    Button loadDocumentsAction;

    private FilteredList<URI> filteredUris;
    private ObservableList<URI> allUris;

    @FXML
    ListView<Document> documentListView;

    public record Documents(List<Document> documents, Object updateKey) {
        public Documents(List<Document> documents) {
            this(documents, System.currentTimeMillis());
        }
    }

    private Property<Documents> uriDocumentsProperty = new SimpleObjectProperty<>(new Documents(List.of()));

    @Inject
    Instance<LabelAdapter<?>> labelAdapters;

    private List<BindingSource<?>> bindingSources;

    @Override
    public List<BindingSource<?>> getBindingSources() {
        return this.bindingSources;
    }

    private String loadDocumentsActionFormat;

    @FXML
    void initialize() {
        allUris = uriListView.getItems();
        filteredUris = new FilteredList<>(allUris);
        uriListView.setItems(filteredUris);
        uriListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        loadDocumentsActionFormat = loadDocumentsAction.getText();
        filteredUris.subscribe(this::updateLoadDocumentsActionText);
        uriListView.getSelectionModel().getSelectedItems().subscribe(this::updateLoadDocumentsActionText);
        AdapterListView.adapt(this.uriListView, uri -> {
            var baseUriString = getUri(uriText.getText()).toString();
            var uriString = uri.toString();
            return (uriString.startsWith(baseUriString) ? uriString.substring(baseUriString.length()) : uriString);
        });

        pathFilterText.textProperty().subscribe(this::updateUriList);
        extensionFilterText.textProperty().subscribe(this::updateUriList);

        documentParserSelector.getSelectionModel().select(0);
        uriDocumentsProperty.subscribe(uriDocuments -> documentListView.getItems().setAll(uriDocuments.documents()));

        AdapterListView.adapt(this.documentListView, CompositeLabelAdapter.of(this.labelAdapters));
        this.bindingSources = List.of(
            new BindingSource<Documents>(this.documentListView, Documents.class, uriDocumentsProperty)
        );
        updateUriList();
    }

    private void updateLoadDocumentsActionText() {
        int uriCount = uriListView.getSelectionModel().getSelectedItems().size();
        if (uriCount == 0) {
            uriCount = uriListView.getItems().size();
        }
        loadDocumentsAction.setText(loadDocumentsActionFormat.formatted(uriCount));
    }

    private void updateUriList() {
        filteredUris.setPredicate(uri -> {
            String pathFilter = pathFilterText.getText();
            if (! (pathFilter.isBlank() || uri.getPath().contains(pathFilter))) {
                return false;
            }
            String filename = Path.of(uri.getPath()).getFileName().toString();
            String extensionFilter = extensionFilterText.getText();
            if (! (extensionFilter.isBlank() || filename.endsWith(extensionFilter))) {
                return false;
            }
            return true;
        });
    }

    private DirectoryChooser directoryChooser = new DirectoryChooser();

    private boolean isFileUri(String uri) {
        return uri.startsWith("file:") || (! uri.contains(":"));
    }
    private boolean isFileUri(URI uri) {
        return "file".equals(uri.getScheme());
    }
    private String getFilePath(String uri) {
        if (uri.startsWith("file:")) {
            return URI.create(uri).getPath();
        } else if (! uri.contains(":")) {
            return uri; 
        }
        return null;
    }

    private URI getUri(String uri) {
        return (isFileUri(uri) ? Path.of(getFilePath(uri)).toUri() : URI.create(uri));
    }

    @FXML
    void browseDocumentsFolder() {
        directoryChooser.setTitle("Select folder");
        if (isFileUri(uriText.getText())) {
            String path = getFilePath(uriText.getText());
            if (! path.isBlank()) {
                directoryChooser.setInitialDirectory(new File(path));
            }
        }
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            uriText.setText(selectedDirectory.getAbsolutePath());
            scanForUrisAction.fire();
        }
    }

    @Inject
    Logger logger;

    @FXML
    String plainTextLabel;

    @FXML
    String htmlAsTextLabel;

    @FXML
    String htmlAsMarkdownLabel;

    @Inject
    @Named("plain-text")
    DocumentParser plainTextDocumentParser;

    @Inject
    @Named("jsoup+text")
    DocumentParser htmlAsTextDocumentParser;

    @Inject
    @Named("jsoup+copydown")
    DocumentParser htmlAsMarkdownDocumentParser;

    private ActionProgressHelper buttonActionProgressHelper = new ActionProgressHelper();

    private Alert alert = null;

    @FXML
    void scanForUris(ActionEvent event) {
        buttonActionProgressHelper.performAction(
            event, () -> {
                List<URI> uris = new ArrayList<>();
                if (isFileUri(uriText.getText())) {
                    uris.addAll(scanFolderForFiles(getFilePath(uriText.getText())));
                } else if (uriText.getText().endsWith("sitemap.xml")) {
                    uris.addAll(scanSitemapForUris(uriText.getText()));
                } else {
                    uris.addAll(scanWebPageForUris(uriText.getText()));
                }
                return uris;
            },
            uris -> allUris.setAll(uris),
            exception -> {
                if (alert == null) {
                    alert = new Alert(Alert.AlertType.ERROR);
                }
                alert.setTitle("Exception when scanning for URIs");
                alert.setHeaderText(exception.getClass().getSimpleName());
                alert.setContentText(exception.getMessage());
                alert.showAndWait();        
            }
        );
    }

    private List<URI> scanFolderForFiles(String filePath) {
        try (var stream = Files.walk(Paths.get(filePath))) {
            return stream.filter(Files::isRegularFile).map(Path::toUri).toList();
        } catch (IOException ex) {
            throw new RuntimeException("Exception scanning folder for files: " + ex, ex);
        }
    }

    private List<URI> scanWebPageForUris(String filePath) {
        return List.of();
    }

    public List<URI> scanSitemapForUris(String siteUri) {
        List<URL> allUrls = new ArrayList<>();
        var siteMapIndexParser = new SiteMapParser();
        try {
            var siteXml = siteMapIndexParser.parseSiteMap(URI.create(siteUri).toURL());
            if (siteXml instanceof SiteMapIndex siteMapIndex) {
                for (AbstractSiteMap siteMapIndexItem : siteMapIndex.getSitemaps()) {
                    var siteMapParser = new SiteMapParser();
                    var urlSet = siteMapParser.parseSiteMap(siteMapIndexItem.getUrl());
                    if (urlSet instanceof SiteMap siteMap) {
                        allUrls.addAll(siteMap.getSiteMapUrls().stream().map(SiteMapURL::getUrl).toList());
                    }
                }
            }
            return allUrls.stream().map(url -> URI.create(url.toString())).toList();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error scanning sitemap for uris", ex);
        }
    }

    @FXML
    void loadDocuments(ActionEvent event) {
        List<URI> uris = new ArrayList<>(uriListView.getSelectionModel().getSelectedItems());
        if (uris.isEmpty()) {
            uris.addAll(uriListView.getItems());
        }
        int uriCount = uris.size();
        String selector = documentParserSelector.getValue();
        DocumentParser documentParser = selector.equals(plainTextLabel) ? plainTextDocumentParser
            : selector.equals(htmlAsTextLabel) ? htmlAsTextDocumentParser
            : selector.equals(htmlAsMarkdownLabel) ? htmlAsMarkdownDocumentParser
            : plainTextDocumentParser; 
        if (documentParser instanceof AbstractJSoupDocumentParser jsoupParser) {
            jsoupParser.setContentSelector(contentSelectorText.getText());
        }
        buttonActionProgressHelper.performActions(event, uriCount, progress -> {
            CountDownLatch latch = new CountDownLatch(uriCount);
            List<Document> documents = Collections.synchronizedList(new ArrayList<>());
            Runnable updater = () -> uriDocumentsProperty.setValue(new Documents(new ArrayList<>(documents)));
            uris.forEach(uri -> Thread.ofVirtual().start(() -> {
                try (var stream = uri.toURL().openStream()) {
                    var document = documentParser.parse(stream);
                    document.metadata().put(Document.URL, uri.toString());
                    if (isFileUri(uri)) {
                        var path = Path.of(uri.getPath());
                        document.metadata().put(Document.ABSOLUTE_DIRECTORY_PATH, path.getParent().toString());
                        document.metadata().put(Document.FILE_NAME, path.getFileName().toString());
                    }
                    int newSize = documents.size() + 1;
                    documents.add(document);
                    progress.call(newSize);
                    if (newSize % 10 == 0) {
                        Platform.runLater(updater);
                    }
                } catch (IOException ex) {
                    logger.warn("Error loading document from " + uri, ex);
                } finally {
                    latch.countDown();
                }
            }));
            try {
                latch.await();
            } catch (InterruptedException e) {
            } finally {
                progress.call(null);                
                Platform.runLater(updater);
            }
        });
    }
}
