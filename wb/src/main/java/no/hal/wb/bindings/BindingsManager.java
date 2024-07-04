package no.hal .wb.bindings;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import no.hal.fx.bindings.BindingController;
import no.hal.fx.bindings.BindingsSource;
import no.hal.fx.bindings.BindingsTarget;
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

    private List<BindingsSource> bindingsSources = new ArrayList<>();
    private List<BindingsTarget> bindingsTargets = new ArrayList<>();
            
    private boolean autoBindView = true;

    public void onEvent(@Observes ViewEvent.Added event) {
        if (event.viewInfo().instance().controller() instanceof BindingsSource bindingsSource) {
            bindingsSources.add(bindingsSource);
            bindingController.addBindingSources(bindingsSource);
            if (autoBindView) {
                bindingController.bindToSources(bindingsSource);
            }
        }
        if (event.viewInfo().instance().controller() instanceof BindingsTarget bindingsTarget) {
            bindingsTargets.add(bindingsTarget);
            bindingController.addBindingTargets(bindingsTarget);
            if (autoBindView) {
                bindingController.bindToTargets(bindingsTarget);
            }
        }
    }
    
    public void onEvent(@Observes ViewEvent.Removed event) {
        if (event.viewInfo().instance().controller() instanceof BindingsSource bindingsSource) {
            bindingsSources.remove(bindingsSource);
            bindingController.removeBindingSources(bindingsSource);
            bindingController.removeBindings(bindingsSource);
        }
        if (event.viewInfo().instance().controller() instanceof BindingsTarget bindingsTarget) {
            bindingsTargets.remove(bindingsTarget);
            bindingController.removeBindingTargets(bindingsTarget);
            bindingController.removeBindings(bindingsTarget);
        }
    }
}
