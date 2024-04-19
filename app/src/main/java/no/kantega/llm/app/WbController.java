package no.kantega.llm.app;

import com.panemu.tiwulfx.control.dock.DetachableTabPane;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.layout.Pane;
import no.hal.wb.bindings.BindingsManager;
import no.hal.wb.views.ViewManager;

@Dependent
public class WbController {

    @FXML
    Pane bindingControllerRoot;

    @FXML
    DetachableTabPane detachableTabPane1;

    @FXML
    Menu viewMenu;
    
    @Inject
    ViewManager viewManager;
    
    @Inject
    BindingsManager bindingsManager;
    
    @FXML
    void initialize() {
        bindingsManager.setControllerRoot(bindingControllerRoot);
        viewManager.setDetachableTabPane(detachableTabPane1);
        viewMenu.getItems().addAll(viewManager.createViewCreationItems("New %s view"));
        viewManager.createInitialViews();
    }
    
    @FXML
    void newView(Event event) {
        viewManager.createView(event.getSource(), detachableTabPane1);
    }
}
