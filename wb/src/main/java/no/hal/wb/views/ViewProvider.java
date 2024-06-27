package no.hal.wb.views;

import com.fasterxml.jackson.databind.JsonNode;

import javafx.scene.Node;

public interface ViewProvider {

    Info getViewInfo();
    
    Instance createView(JsonNode configuration);
    
    default boolean supportsDuplicate() {
        return false;
    }
    default Instance duplicateView(Instance instance) {
        if (! supportsDuplicate()) {
            throw new UnsupportedOperationException("Cannot duplicate " + getViewInfo().viewProviderId() + " view");
        }
        return createView(instance.configuration());
    }
    default void dispose(Instance viewInstance) {
    }

    default String viewProviderId() {
        return getViewInfo().viewProviderId();
    }

    public record Info(String viewProviderId, String viewTitle, String viewCategory) {
        public Info(String viewProviderId, String viewTitle) {
            this(viewProviderId, viewTitle, null);
        }
    }

    public record Instance(Object controller, Node viewNode, JsonNode configuration) {
    }
}
