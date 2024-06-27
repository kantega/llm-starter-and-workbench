package no.kantega.llm.app;

import java.io.IOException;
import java.util.Map;

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
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.EmbeddingModelsView", "Embedding models", "Embeddings"), fxmlLoaderProvider, "/no/kantega/llm/fx/EmbeddingModelsView.fxml"){};
    }

    @Produces
    ViewProvider embeddingsScoreView() {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.EmbeddingsScoreView", "Embeddings score", "Embeddings"), fxmlLoaderProvider, "/no/kantega/llm/fx/EmbeddingsScoreView.fxml"){};
    }

    @Produces
    ViewProvider chatMemoryView() {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.ChatMemoryView", "Chat memory", "Chat"), fxmlLoaderProvider, "/no/kantega/llm/fx/ChatMemoryView.fxml"){};
    }

    @Produces
    ViewProvider chatLanguageModelsView() {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.ChatLanguageModelsView", "Chat models", "Chat models"), fxmlLoaderProvider, "/no/kantega/llm/fx/ChatLanguageModelsView.fxml"){};
    }

    @Produces
    ViewProvider streamingChatLanguageModelsView() {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.StreamingChatLanguageModelsView", "Streaming chat models", "Chat models"), fxmlLoaderProvider, "/no/kantega/llm/fx/StreamingChatLanguageModelsView.fxml"){};
    }

    @Produces
    ViewProvider simpleChatView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.SimpleChatView", "Simple chat", "Chat"), fxmlLoaderProvider, "/no/kantega/llm/fx/SimpleChatView.fxml"){};
    }

    @Produces
    ViewProvider uriDocumentsView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.UriDocumentsView", "Uri documents", "ETL"), fxmlLoaderProvider, "/no/kantega/llm/fx/UriDocumentsView.fxml"){};
    }

    @Produces
    ViewProvider ingestorView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.IngestorView", "Ingestor", "ETL"), fxmlLoaderProvider, "/no/kantega/llm/fx/IngestorView.fxml"){};
    }

    @Produces
    ViewProvider embeddingsSearchView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.EmbeddingsSearchView", "Embeddings search", "Embeddings"), fxmlLoaderProvider, "/no/kantega/llm/fx/EmbeddingsSearchView.fxml"){};
    }

    @Produces
    ViewProvider ragChatView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.RagChatView", "Rag chat", "Chat"), fxmlLoaderProvider, "/no/kantega/llm/fx/RagChatView.fxml"){};
    }

    @Produces
    ViewProvider csvView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.CsvView", "Csv", "Data"), fxmlLoaderProvider, "/no/kantega/llm/fx/CsvView.fxml"){};
    }

    @Produces
    ViewProvider expressionView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.ExpressionView", "Expression", "Scripting"), fxmlLoaderProvider, "/no/kantega/llm/fx/ExpressionSupportView.fxml"){};
    }

    @Produces
    ViewProvider barChartView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.BarChartView", "Bar chart", "Charts"), fxmlLoaderProvider, "/no/kantega/llm/fx/BarChartView.fxml"){};
    }

    @Produces
    ViewProvider stackedChartView() throws IOException {
        return new FxmlViewProvider(new ViewProvider.Info("no.kantega.llm.fx.StackedBarChartView", "Stacked bar chart", "Charts"), fxmlLoaderProvider, "/no/kantega/llm/fx/StackedBarChartView.fxml"){};
    }
}
