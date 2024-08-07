package no.kantega.llm.fx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dev.langchain4j.model.input.Prompt;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import no.hal.fx.bindings.BindingSource;
import no.hal.fx.bindings.BindingsSource;
import no.kantega.llm.prompts.SimplePromptTemplate;
import no.kantega.prompthubdeepsetai.PrompthubTemplatesProvider;

@Dependent
public class PromptViewController implements BindingsSource {

    @FXML
    ListView<String> templatesListView;

    @FXML
    TextInputControl templateString;

    @FXML
    TextInputControl promptString;

    @FXML
    Pane variableValuesPane;

    @Inject
    PrompthubTemplatesProvider templatesProvider;

    private List<BindingSource<?>> bindingSources;

    @Override
    public List<BindingSource<?>> getBindingSources() {
        return bindingSources;
    }

    @FXML
    void initialize() {
        var templates = templatesProvider.getPromptTemplates();
        templatesListView.getItems().addAll(templates.stream().map(SimplePromptTemplate::getDescription).toList());
        templatesListView.getSelectionModel().selectedIndexProperty().subscribe(selectedIndex -> {
            var idx = selectedIndex.intValue();
            if (idx >= 0 && idx < templates.size()) {
                var template = templates.get(idx);
                templateString.setText(template.getTemplate());
            }
        });
        templateString.textProperty().subscribe(templateString -> {
            var template = templatesProvider.from(templateString);
            updateTemplate(template);
        });
        
        bindingSources = List.of(
            new BindingSource<Prompt>(promptString, Prompt.class, promptString.textProperty().map(text -> text.isBlank() ? null : Prompt.from(text)))
        );
    }
    
    private SimplePromptTemplate currentTemplate;
    
    private void updateTemplate(SimplePromptTemplate template) {
        updateVariableValuesPane(template);
        this.currentTemplate = template;
        updatePromptString();
    }

    private String variableLabelFormat = "%s: ";

    private void updateVariableValuesPane(SimplePromptTemplate template) {
        if (Objects.equals(template, currentTemplate)) {
            return;
        }
        var templateVariables = template.getVariables();
        if (currentTemplate != null && templateVariables.equals(currentTemplate.getVariables())) {
            return;
        }
        var children = variableValuesPane.getChildren();
        var oldChildren = new ArrayList<>(children);
        children.clear();
        for (var variable : templateVariables) {
            Label label = null;
            TextArea textArea = null;
            var varLabel = variableLabelFormat.formatted(variable);
            for (int i = 0; i < oldChildren.size() - 1; i++) {
                if (oldChildren.get(i) instanceof Label l && varLabel.equals(l.getText()) && oldChildren.get(i + 1) instanceof TextArea ta) {
                    label = l;
                    textArea = ta;
                    oldChildren.remove(i + 1);
                    oldChildren.remove(i);
                    break;
                }
            }
            if (label == null) {
                label = new Label(varLabel);
                textArea = new TextArea("<value for " + variable + ">");
                textArea.selectAll();
                textArea.textProperty().subscribe(this::updatePromptString);
            }
            children.add(label);
            children.add(textArea);
        }
        oldChildren.clear();
    }

    private void updatePromptString() {
        var templateVariables = currentTemplate.getVariables();
        Map<String, Object> variableValues = new HashMap<>();
        var children = variableValuesPane.getChildrenUnmodifiable();
        for (var variable : templateVariables) {
            var varLabel = variableLabelFormat.formatted(variable);
            for (int i = 0; i < children.size() - 1; i++) {
                if (children.get(i) instanceof Label label && varLabel.equals(label.getText()) && children.get(i + 1) instanceof TextArea textArea) {
                    variableValues.put(variable, textArea.getText());
                    break;
                }
            }
        }
        promptString.setText(currentTemplate.render(variableValues));
    }
}
