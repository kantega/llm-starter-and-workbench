package no.hal.wb.views.markdown;

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

    @Inject
    jakarta.enterprise.inject.Instance<MarkdownViewController> markdownViewController;

    @Override
    public Info getViewInfo() {
        return new Info(VIEW_ID, "Markdown view", "Markdown");
    }

    @Override
    public Instance createView(JsonNode configuration) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        var controller = markdownViewController.get();
        controller.configure(configuration);
        scrollPane.setContent(controller.getMarkdownView());
        return new Instance(controller, scrollPane, configuration);
    }
}
