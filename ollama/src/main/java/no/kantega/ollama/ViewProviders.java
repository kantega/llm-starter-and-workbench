package no.kantega.ollama;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import javafx.fxml.FXMLLoader;
import no.hal.wb.fx.FxmlViewProvider;
import no.hal.wb.views.ViewProvider;

@ApplicationScoped
public class ViewProviders {
    
    @Inject
    Provider<FXMLLoader> fxmlLoaderProvider;

    @Produces
    ViewProvider ollamaChatModelsView() {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.OllamaModelsView", "Ollama models", "Chat models"), fxmlLoaderProvider, "/no/kantega/ollama/fx/OllamaModelsView.fxml"){};
    }
}
