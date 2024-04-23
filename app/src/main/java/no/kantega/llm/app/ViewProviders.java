package no.kantega.llm.app;

import java.io.IOException;

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
    ViewProvider embeddingModelsView() {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.EmbeddingModelsView", "Embedding models"), fxmlLoaderProvider, "/no/kantega/llm/fx/EmbeddingModelsView.fxml");
    }

    @Produces
    ViewProvider chatModelsView() {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.ChatModelsView", "Chat models"), fxmlLoaderProvider, "/no/kantega/llm/fx/ChatModelsView.fxml");
    }

    @Produces
    ViewProvider embeddingsScoreView() {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.EmbeddingsScoreView", "Embeddings score"), fxmlLoaderProvider, "/no/kantega/llm/fx/EmbeddingsScoreView.fxml");
    }

    @Produces
    ViewProvider chatMemoryView() {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.ChatMemoryView", "Chat memory"), fxmlLoaderProvider, "/no/kantega/llm/fx/ChatMemoryView.fxml");
    }

    @Produces
    ViewProvider simpleChatView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.SimpleChatView", "Simple chat"), fxmlLoaderProvider, "/no/kantega/llm/fx/SimpleChatView.fxml");
    }

    @Produces
    ViewProvider fileSystemDocumentsView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.FileSystemDocumentsView", "File system documents"), fxmlLoaderProvider, "/no/kantega/llm/fx/FileSystemDocumentsView.fxml");
    }
    @Produces
    ViewProvider uriDocumentsView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.UriDocumentsView", "Uri documents"), fxmlLoaderProvider, "/no/kantega/llm/fx/UriDocumentsView.fxml");
    }

    @Produces
    ViewProvider ingestorView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.IngestorView", "Ingestor"), fxmlLoaderProvider, "/no/kantega/llm/fx/IngestorView.fxml");
    }

    @Produces
    ViewProvider embeddingsSearchView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.EmbeddingsSearchView", "Embeddings search"), fxmlLoaderProvider, "/no/kantega/llm/fx/EmbeddingsSearchView.fxml");
    }

    @Produces
    ViewProvider ragChatView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.RagChatView", "Rag chat"), fxmlLoaderProvider, "/no/kantega/llm/fx/RagChatView.fxml");
    }
}
