import java.time.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class LlmServices {

    String baseUrl = "http://localhost:11434/";

    @ConfigProperty(name = "quarkus.langchain4j.ollama.chat-model.temperature")
    double temperature;

    @ConfigProperty(name = "quarkus.langchain4j.ollama.timeout")
    Duration timeout;

    @Produces
    public DocumentParser getDocumentParser() {
        var html2markdownConverter = new io.github.furstenheim.CopyDown();
        return inputStream -> {
            try {
                var soup = org.jsoup.Jsoup.parse(inputStream, "UTF-8", "");
                var markdown = html2markdownConverter.convert(soup.outerHtml());
                return Document.from(markdown);
            } catch (Exception e) {
                System.err.println(e);
                throw new RuntimeException(e);
            }
        };
    }

    @Produces
    public EmbeddingStore getEmbeddingStore() {
        return new InMemoryEmbeddingStore<>()
        // {
        //     public List<String> addAll(List<Embedding> embeddings, List<TextSegment> textSegments) {
        //         var ids = super.addAll(embeddings, textSegments);
        //         System.out.println("Added " + embeddings.size() + " embeddings: ");
        //         for (int i = 0; i < embeddings.size(); i++) {
        //             var embedding = embeddings.get(i);
        //             var textSegment = textSegments.get(i);
        //             System.out.println("  " + i + ": " + textSegment.metadata());
        //         }
        //         return ids;
        //     }
        //     public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        //         var result = super.search(request);
        //         for (var match : result.matches()) {
        //             System.out.println(match.score() + ": " + match.embedded().metadata());
        //         }
        //         return result;
        //     }
        // }
        ;
    }

    @Produces
    public ChatMemory getChatMemory() {
        return MessageWindowChatMemory.builder()
            .maxMessages(20)
            .id("default")
            .build();
    }

    @Produces
    public EmbeddingModel getEmbeddingModel() {
        return OllamaEmbeddingModel.builder()
            .baseUrl(baseUrl)
            .timeout(timeout)
            .modelName("nomic-embed-text")
            .build();
    }

    @Produces
    public StreamingChatLanguageModel getStreamingChatLanguageModel() {
        return OllamaStreamingChatModel.builder()
            .baseUrl(baseUrl)
            .timeout(timeout)
            .modelName("mistral")
            .temperature(temperature)
            .build();
    }
}
