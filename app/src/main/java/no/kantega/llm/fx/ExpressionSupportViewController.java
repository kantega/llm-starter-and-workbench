package no.kantega.llm.fx;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import no.hal.expressions.ExpressionSupport;
import no.hal.expressions.PreparedExpression;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.util.ActionProgressHelper;

@Dependent
public class ExpressionSupportViewController {

    @FXML
    ComboBox<String> langSelector;

    @FXML
    TextInputControl expressionText;
    @FXML
    TextInputControl resultValueText;

    @FXML
    Button loadDocumentsAction;

    private FilteredList<URI> filteredUris;
    private ObservableList<URI> allUris;

    @Inject
    Instance<LabelAdapter<?>> labelAdapters;

    @FXML
    void initialize() {
        langSelector.getSelectionModel().select(0);
    }

    @Inject
    Logger logger;

    @FXML
    String mvelLabel;

    @FXML
    String janinoLabel;

    @Inject
    @Named("mvel")
    ExpressionSupport mvelExpressionSupport;

    @Inject
    @Named("janino")
    ExpressionSupport janinoExpressionSupport;

    private ActionProgressHelper buttonActionProgressHelper = new ActionProgressHelper();

    private Alert alert = null;

    @FXML
    void evaluateExpression(ActionEvent event) {
        String selector = langSelector.getValue();
        ExpressionSupport expressionSupport = selector.equals(mvelLabel) ? mvelExpressionSupport
            : selector.equals(janinoLabel) ? janinoExpressionSupport
            : mvelExpressionSupport;
            resultValueText.setText("");
        buttonActionProgressHelper.performAction(event,
            () -> {
                PreparedExpression preparedExpr = expressionSupport.prepareExpression(expressionText.getText(), Collections.emptyMap(), null);
                return expressionSupport.evaluateExpression(preparedExpr, new HashMap<>());
            },
            result -> resultValueText.setText(String.valueOf(result)),
            exc -> {
                logger.warn(exc);
                exc.printStackTrace();
                if (alert == null) {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Exception when preparing/evaluating expression");
                }
                alert.setHeaderText(exc.getClass().getSimpleName());
                alert.setContentText(exc.getMessage());
                alert.show();
            });
    }
}
