package no.hal.wb.fx;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.inject.Provider;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import no.hal.wb.views.ViewProvider;

public class FxmlViewProvider implements ViewProvider {

    private final ViewProvider.Info viewInfo;
    private final Provider<FXMLLoader> fxmlLoaderProvider;
    private final String fxmlPath;
    private final Map<String, Object> fxmlParams;
    private final JsonNode configuration;

    public FxmlViewProvider(ViewProvider.Info viewInfo, Provider<FXMLLoader> fxmlLoaderProvider, String fxmlPath, Map<String, Object> fxmlParams, JsonNode configuration) {
        this.viewInfo = viewInfo;
        this.fxmlLoaderProvider = fxmlLoaderProvider;
        this.fxmlPath = fxmlPath;
        this.fxmlParams = fxmlParams;
        this.configuration = configuration;
    }
    public FxmlViewProvider(ViewProvider.Info viewInfo, Provider<FXMLLoader> fxmlLoaderProvider, String fxmlPath) {
        this(viewInfo, fxmlLoaderProvider, fxmlPath, null, null);
    }

    @Override
    public Info getViewInfo() {
        return viewInfo;
    }

    @Override
    public Instance createView(JsonNode configuration) {
        if (configuration == null) {
            configuration = this.configuration;
        }
        var fxmlUrl = getClass().getResource(fxmlPath);
        var fxmlLoader = fxmlLoaderProvider.get();
        if (fxmlParams != null) {
            fxmlLoader.getNamespace().putAll(fxmlParams);
        }
        fxmlLoader.setLocation(fxmlUrl);
        try {
            Parent parent = fxmlLoader.load();
            return new Instance(fxmlLoader.getController(), parent, configuration);
        } catch (Exception ex) {
            throw new RuntimeException("Exception when loading FXML from " + fxmlPath, ex);
        }
    }
}
