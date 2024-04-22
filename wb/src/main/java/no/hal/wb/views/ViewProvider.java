package no.hal.wb.views;

import javafx.scene.Node;

public interface ViewProvider {

    Info getViewInfo();
    
    Instance createView();
    
    default boolean supportsDuplicate() {
        return false;
    }
    default Instance duplicateView(Instance instance) {
        if (! supportsDuplicate()) {
            throw new UnsupportedOperationException("Cannot duplicate " + getViewInfo().viewProviderId() + " view");
        }
        return createView();
    }
    default void dispose(Instance viewInstance) {
    }

    public record Info(String viewProviderId, String viewTitle) {
    }

    public record Instance(Object controller, Node viewNode) {
    }
}
