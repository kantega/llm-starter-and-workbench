package no.kantega.llm.app;

import com.panemu.tiwulfx.control.dock.DetachableTabPane;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import no.hal.wb.bindings.BindingsManager;
import no.hal.wb.views.ViewManager;

@Dependent
public class WbController {

    @Inject
    Provider<FXMLLoader> fxmlLoaderProvider;

    @FXML
    Pane bindingControllerRoot;

    @FXML
    DetachableTabPane detachableTabPane1;

    @Inject
    ViewManager viewManager;
    
    @Inject
    BindingsManager bindingsManager;

    @FXML
    void initialize() {
        bindingsManager.setControllerRoot(bindingControllerRoot);
        viewManager.createInitialViews(detachableTabPane1);
    }
    
    @FXML
    void newView(Event event) {
        viewManager.createView(event.getSource(), detachableTabPane1);
    }
}
