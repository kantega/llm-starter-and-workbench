package no.hal.wb.fx;

import java.io.IOException;

import jakarta.inject.Provider;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import no.hal.wb.views.ViewProvider;

public class FxmlViewProvider implements ViewProvider {

    private final ViewProvider.Info viewInfo;
    private final Provider<FXMLLoader> fxmlLoaderProvider;
    private final String fxmlPath;

    public FxmlViewProvider(ViewProvider.Info viewInfo, Provider<FXMLLoader> fxmlLoaderProvider, String fxmlPath) {
        this.viewInfo = viewInfo;
        this.fxmlLoaderProvider = fxmlLoaderProvider;
        this.fxmlPath = fxmlPath;
    }

    @Override
    public Info getViewInfo() {
        return viewInfo;
    }

    @Override
    public Instance createView() {
        var fxmlUrl = getClass().getResource(fxmlPath);
        var fxmlLoader = fxmlLoaderProvider.get();
        try {
            Parent parent = fxmlLoader.load(fxmlUrl.openStream());
            return new Instance(fxmlLoader.getController(), parent);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
