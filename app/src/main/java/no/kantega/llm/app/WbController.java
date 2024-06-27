package no.kantega.llm.app;

import java.util.List;
import java.util.Optional;

import com.panemu.tiwulfx.control.dock.DetachableTabPane;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import no.hal.wb.bindings.BindingsManager;
import no.hal.wb.views.ViewManager;
import no.hal.wb.views.ViewModel;

@Dependent
public class WbController {

    @FXML
    Pane viewModelContainer;

    @FXML
    MenuBar menuBar;
    
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
        var viewMenu = findMenu(menuBar.getMenus(), "View");
        var categoryMenuItems = viewManager.createViewCreationMenuItems("%s view");
        for (var category : categoryMenuItems.keySet()) {
            Menu menu = findMenu(menuBar.getMenus(), category);
            if (menu == null && viewMenu != null) {
                menu = findMenu(viewMenu.getItems(), category);
            }
            if (menu != null) {
                menu.getItems().addAll(categoryMenuItems.get(category));
            } else {
                var newMenu = new Menu(category == null || category.isEmpty() ? "Other" : category);
                newMenu.getItems().addAll(categoryMenuItems.get(category));
                if (viewMenu != null) {
                    viewMenu.getItems().add(newMenu);
                } else {
                    menuBar.getMenus().add(newMenu);
                }
            }
        }
        viewManager.createInitialViews();
    }

    private Menu findMenu(List<? extends MenuItem> menuItems, String... menuPath) {
        for (int itemNum = 0; itemNum < menuPath.length; itemNum++) {
            var pathItem = menuPath[itemNum];
            Optional<? extends MenuItem> optionalMenu = menuItems.stream().filter(menuItem -> pathItem.equals(menuItem.getText())).findFirst();
            if (optionalMenu.isPresent() && optionalMenu.get() instanceof Menu menu) {
                if (itemNum == menuPath.length - 1) {
                    return menu;
                }
                menuItems = menu.getItems();
            } else {
                return null;
            }
        }
        return null;
    }
}
