package no.hal .wb.bindings;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingController;
import no.hal.wb.views.ViewEvent;

@ApplicationScoped
public class BindingsManager {

    private BindingController bindingController;

    public void setControllerRoot(Pane bindingControllerRoot) {
        bindingController = new BindingController(bindingControllerRoot);
        Platform.runLater(() -> {
            bindingControllerRoot.getScene().addEventFilter(KeyEvent.ANY, bindingController.new BindingKeyHandler());
            bindingControllerRoot.getScene().addEventFilter(MouseEvent.ANY, bindingController.new BindingMouseHandler());
        });
    }

    private List<BindableView> bindableViews = new ArrayList<>();
            
    private boolean autoBindView = true;

    public void onEvent(@Observes ViewEvent.Added event) {
        if (event.viewInfo().instance().controller() instanceof BindableView bindableView) {
            bindableViews.add(bindableView);
            bindingController.addBindingSources(bindableView);
            bindingController.addBindingTargets(bindableView);
            if (autoBindView) {
                bindingController.bindToTargets(bindableView);
            }
        }
    }
    
    public void onEvent(@Observes ViewEvent.Removed event) {
        if (event.viewInfo().instance().controller() instanceof BindableView bindableView) {
            bindableViews.remove(bindableView);
            bindingController.removeBindingSources(bindableView);
            bindingController.removeBindingTargets(bindableView);
            bindingController.removeBindings(bindableView);
        }
    }
}
