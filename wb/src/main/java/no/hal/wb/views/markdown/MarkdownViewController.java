package no.hal.wb.views.markdown;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import no.hal.wb.storedstate.Configurable;
import one.jpro.platform.mdfx.MarkdownView;
import one.jpro.platform.mdfx.extensions.ImageExtension;
import one.jpro.platform.mdfx.impl.AdaptiveImage;

@Dependent
public class MarkdownViewController implements Configurable {
    
    private String markdownString;
    private URI markdownResource;

    private StackPane content;
    private MarkdownView markdownView;
    private ContextMenu contextMenu;
    
    private Button forwardButton, backwardButton;
    
    @Inject
    PathResolver pathResolver;

    @Inject Logger logger;

    private static URI resolveUri(URI baseUri, String uriString) {
        URI uri = URI.create(uriString);
        if (uri.isAbsolute()) {
            return uri;
        } else if ("jar".equals(baseUri.getScheme())) {
            try {
                URL baseUrl = baseUri.toURL();
                return new URL(baseUrl, uriString).toURI();
            } catch (MalformedURLException | URISyntaxException exc) {
                throw new RuntimeException(exc);
            }
        } else {
            return baseUri.resolve(uri);
        }
    }

    public MarkdownViewController() {
        markdownView = new MarkdownView(markdownString, List.of(new ImageExtension((String) null, (url, view) -> {
            var actualUri = pathResolver.resolvePath(markdownResource);
            var imageUri = resolveUri(actualUri, url);
            Image img = new Image(imageUri.toString(), false);
            AdaptiveImage r = new AdaptiveImage(img);
            r.maxWidthProperty().bind(view.widthProperty());
            return r;
        }))) {
            @Override
            public void setLink(Node node, String link, String description) {
                super.setLink(node, link, description);
                node.getProperties().put("markdown-link", markdownResource.resolve(link));
                node.getProperties().put("markdown-link-description", description);
            }
        };
        markdownView.setPadding(new Insets(5));
        
        markdownView.setOnMouseClicked(mouseEvent -> {
            if (! contextMenuOpener.apply(mouseEvent)) {
                var node = mouseEvent.getPickResult().getIntersectedNode();
                if (node.getStyleClass().contains("markdown-link") && node.getProperties().get("markdown-link") instanceof URI link) {
                    navigateTo(link);
                }
            }
        });
        ScrollPane scrollPane = new ScrollPane(markdownView);
        scrollPane.setFitToWidth(true);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        
        content = new StackPane(scrollPane, createNavigationButtons());
    }
    
    private Node createNavigationButtons() {
        backwardButton = new Button("<");
        backwardButton.setOnAction(actionEvent -> navigateBackward());
        backwardButton.setDisable(true);
        
        forwardButton = new Button(">");
        forwardButton.setOnAction(actionEvent -> navigateForward());
        forwardButton.setDisable(true);
    
        var buttonGroup = new HBox(backwardButton, forwardButton);
        buttonGroup.setManaged(false);
    
        return buttonGroup;
    }

    private Function<MouseEvent, Boolean> contextMenuOpener = mouseEvent -> {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            mouseEvent.consume();
            contextMenu.show(markdownView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            return true;
        }
        return false;
    };

    public Node getContent() {
        return content;
    }

    private void updateView() {
        if (markdownString == null) {
            var actualResource = pathResolver.resolvePath(markdownResource);
            logger.infof("Reading markup for %s from %s", markdownResource, actualResource);
            StringBuilder markdown = new StringBuilder();
            try (InputStream input = actualResource.toURL().openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                reader.lines().forEach(line -> markdown.append(line).append("\n"));        
            } catch (Exception e) {
                appendException(e, markdownResource, markdown);
            }
            markdownString = markdown.toString();
        }
        markdownView.setMdString(markdownString);
    }

    private void appendException(Exception e, URI source, StringBuilder markdownBuffer) {
        markdownBuffer.append("## Exception\n");
        markdownBuffer.append(e.getClass().getName());
        markdownBuffer.append(" when reading markdown from ");
        markdownBuffer.append(source);
        markdownBuffer.append("\n\n");
        if (e.getMessage() != null) {
            markdownBuffer.append(e.getMessage());
            markdownBuffer.append("\n\n");
        }
        try (var writer = new StringWriter();
            var printWriter = new PrintWriter(writer)) {
            e.printStackTrace(printWriter);
            printWriter.flush();
            markdownBuffer.append(writer.toString());
        } catch (Exception e2) {
        }
    }

    private record HistoryEntry(String markdownString, URI markdownResource) {
    }

    private List<HistoryEntry> history;
    private int historyPosition = 0;

    private void navigate(int delta) {
        int newPosition = historyPosition + delta;
        if (history == null || newPosition < 0 || newPosition >= history.size()) {
            return;
        }
        historyPosition = newPosition;
        var entry = history.get(historyPosition);
        markdownString = entry.markdownString;
        markdownResource = entry.markdownResource;
        updateView();
        backwardButton.setDisable(historyPosition == 0);
        forwardButton.setDisable(historyPosition >= history.size() - 1);
    }

    @Inject
    HostServices hostServices;

    protected boolean shouldOpenInBrowser(URI link) {
        return link.getScheme() != null && link.getScheme().startsWith("http");
    }

    private void navigateTo(URI link) {
        logger.infof("Navigating to %s", link);
        if (shouldOpenInBrowser(link)) {
            hostServices.showDocument(link.toString());
        } else {
            pushHistory(link);
            navigate(1);
        }
    }

    private void pushHistory(URI link) {
        if (history == null) {
            history = new ArrayList<>();
            history.add(new HistoryEntry(markdownString, markdownResource));
        } else {
            while (historyPosition < history.size() - 1) {
                history.removeLast();
            }
        }
        history.add(new HistoryEntry(null, link));
    }

    public void navigateForward() {
        navigate(1);
    }

    public void navigateBackward() {
        navigate(-1);
    }

    // configuration

    public final static JsonNode configuration(String markdownResource) {
        var configuration = JsonNodeFactory.instance.objectNode();
        configuration.put("markdownResource", markdownResource);
        return configuration;
    }

    @Override
    public JsonNode getConfiguration() {
        return configuration(markdownResource.toString());
    }

    @Override
    public void configure(JsonNode configuration) {
        markdownResource = switch (configuration.get("markdownResource")) {
            case NullNode nullNode -> null;
            case ValueNode valueNode -> URI.create(valueNode.asText());
            default -> null;
        };
        updateView();
    }
}
