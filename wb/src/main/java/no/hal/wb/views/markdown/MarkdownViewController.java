package no.hal.wb.views.markdown;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.application.HostServices;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import no.hal.wb.storedstate.Configurable;
import one.jpro.platform.mdfx.MarkdownView;
import one.jpro.platform.mdfx.extensions.ImageExtension;
import one.jpro.platform.mdfx.impl.AdaptiveImage;

@Dependent
public class MarkdownViewController implements Configurable {
    
    private String markdownString;
    private URI markdownResource;
    private MarkdownView markdownView;
    
    private MenuItem forwardButton, backwardButton;
    
    private PathResolver pathResolver;
    private HostServices hostServices;

    @Inject Logger logger;

    public MarkdownViewController() {
        markdownView = new MarkdownView(markdownString, List.of(new ImageExtension((String) null, (url, view) -> {
            Image img = new Image(resolveLink(url).toString(), false);
            AdaptiveImage r = new AdaptiveImage(img);
            r.maxWidthProperty().bind(view.widthProperty());
            return r;
        }))) {
            @Override
            public void setLink(Node node, String link, String description) {
                super.setLink(node, link, description);
                node.getProperties().put("markdown-link", resolveLink(link));
                node.getProperties().put("markdown-link-description", description);
            }
        };
        
        forwardButton = new MenuItem("Forward");
        forwardButton.setOnAction(actionEvent -> navigateForward());
        backwardButton = new MenuItem("Backward");
        backwardButton.setOnAction(actionEvent -> navigateBackward());
        var contextMenu = new ContextMenu(forwardButton, backwardButton);
        
        markdownView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(markdownView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            } else {
                var node = mouseEvent.getPickResult().getIntersectedNode();
                if (node.getStyleClass().contains("markdown-link") && node.getProperties().get("markdown-link") instanceof URI link) {
                    navigateTo(link);
                }
            }
        });
    }

    public Node getMarkdownView() {
        return markdownView;
    }

    public void setPathResolver(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
        updateMarkdownView();
    }

    private URI resolveLink(String link) {
        return pathResolver.resolvePath(markdownResource).resolve(link);
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    private void updateMarkdownView() {
        if (markdownString == null) {
            if (pathResolver == null) {
                return;
            }
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
        updateMarkdownView();
        backwardButton.setDisable(historyPosition == 0);
        forwardButton.setDisable(historyPosition >= history.size() - 1);
    }

    private void navigateTo(URI link) {
        logger.infof("Navigating to %s", link);
        if (link.getScheme().startsWith("http")) {
            if (hostServices != null) {
                hostServices.showDocument(link.toString());
            }
            return;
        }
        if (history == null) {
            history = new ArrayList<>();
            history.add(new HistoryEntry(markdownString, markdownResource));
        } else {
            while (historyPosition < history.size() - 1) {
                history.removeLast();
            }
        }
        history.add(new HistoryEntry(null, link));
        navigate(1);
    }

    public void navigateForward() {
        navigate(1);
    }

    public void navigateBackward() {
        navigate(-1);
    }

    // configuration

    public final static JsonNode configuration(String markdownString, String markdownResource) {
        var configuration = JsonNodeFactory.instance.objectNode();
        configuration.put("markdownResource", markdownResource);
        return configuration;
    }

    @Override
    public JsonNode getConfiguration() {
        return configuration(markdownString, markdownResource.toString());
    }

    @Override
    public void configure(JsonNode configuration) {
        markdownResource = switch (configuration.get("markdownResource")) {
            case NullNode nullNode -> null;
            case ValueNode valueNode -> URI.create(valueNode.asText());
            default -> null;
        };
        updateMarkdownView();
    }
}
