package no.kantega.huggingface;

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
    ViewProvider huggingfaceChatModelView() {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.HuggingfaceModelView", "Huggingface model", "Chat models"), fxmlLoaderProvider, "/no/kantega/huggingface/fx/HuggingfaceChatModelView.fxml"){};
    }
}
