package no.hal.wb.views.markdown;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import no.hal.wb.views.ViewProvider;

@ApplicationScoped
public class MarkdownViewProvider implements ViewProvider {

    public final static String VIEW_ID = "no.hal.wb.views.markdown.MarkdownView";

    private final Info viewInfo;
    private final JsonNode defaultConfiguration;
    private jakarta.enterprise.inject.Instance<MarkdownViewController> markdownViewController;

    private MarkdownViewProvider(jakarta.enterprise.inject.Instance<MarkdownViewController> markdownViewController, Info viewInfo, JsonNode defaultConfiguration) {
        this.markdownViewController = markdownViewController;
        this.viewInfo = viewInfo;
        this.defaultConfiguration = defaultConfiguration;
    }

    @Inject
    public MarkdownViewProvider(jakarta.enterprise.inject.Instance<MarkdownViewController> markdownViewController) {
        this(markdownViewController, new Info(VIEW_ID, "Markdown view", "Markdown"), (JsonNode) null);
    }

    public MarkdownViewProvider(jakarta.enterprise.inject.Instance<MarkdownViewController> markdownViewController,
        Info viewInfo, String markdownResource
    ) {
        this(markdownViewController, viewInfo,
            MarkdownViewController.configuration(markdownResource.formatted(viewInfo.viewProviderId()))
        );
    }

    @Override
    public Info getViewInfo() {
        return viewInfo;
    }

    @Override
    public Instance createView(JsonNode configuration) {
        if (configuration == null) {
            configuration = defaultConfiguration;
        }
        var controller = markdownViewController.get();
        controller.configure(configuration);
        return new Instance(controller, controller.getContent(), configuration);
    }
}
