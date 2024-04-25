package no.kantega.llm.app;

import com.panemu.tiwulfx.control.dock.DetachableTabPane;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Menu;
import javafx.scene.layout.Pane;
import no.hal.wb.bindings.BindingsManager;
import no.hal.wb.views.ViewManager;
import no.hal.wb.views.ViewModel;

@Dependent
public class WbController {

    @FXML
    Pane viewModelContainer;

    @FXML
    Menu viewMenu;
    
    @Inject
    ViewManager viewManager;
    
    @Inject
    BindingsManager bindingsManager;
    
    @FXML
    void initialize() {
        bindingsManager.setControllerRoot(viewModelContainer);
        viewManager.initialize(viewModelContainer,
            new ViewModel.ContainerItem<>(new ViewModel.ContainerType.SplitPaneContainer(Orientation.HORIZONTAL),
                new ViewModel.ContainerItem<>(new ViewModel.ContainerType.TabPaneContainer())
        ));
        viewMenu.getItems().addAll(viewManager.createViewCreationMenuItems("New %s view"));
        viewManager.createInitialViews();
    }
}
