///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS dev.langchain4j:langchain4j:0.29.1
//DEPS dev.langchain4j:langchain4j-ollama:0.29.1
//DEPS org.jsoup:jsoup:1.16.1
//DEPS io.github.furstenheim:copy_down:1.1

import java.util.*;
import java.util.function.*;

import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.data.document.*;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;

public class RagChat {

    ChatMemory chatMemory;
    ChatbotAgent chatbotAgent;

    private void setup() {
        var html2markdownConverter = new io.github.furstenheim.CopyDown();
        DocumentParser soupDocumentParser = inputStream -> {
            try {
                var soup = org.jsoup.Jsoup.parse(inputStream, "UTF-8", "");
                var markdown = html2markdownConverter.convert(soup.outerHtml());
                return Document.from(markdown);
            } catch (Exception e) {
                System.err.println(e);
                throw new RuntimeException(e);
            }
        };
        var documentsPath = "/Users/hal/git/fschat/files/html-cache/www__nord__no/__student__eksamen";
        List<Document> documents = FileSystemDocumentLoader.loadDocumentsRecursively(documentsPath, soupDocumentParser);
    
        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
            .baseUrl("http://localhost:11434/")
            .modelName("nomic-embed-text")
            .build();

        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>()
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
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(300, 30))
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build();
        ingestor.ingest(documents);

        ContentRetriever contentRetriever = new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel, 5, 0.5)
        // {
        //     public List<Content> retrieve(Query query) {
        //         var contents = super.retrieve(query);
        //         System.out.println("Retrieved " + contents.size() + " text fragments for " + query.text() + ": ");
        //         for (var content : contents) {
        //             System.out.println(content);
        //         }
        //         return contents;
        //     }
        // }
        ;

        StreamingChatLanguageModel llm = OllamaStreamingChatModel.builder()
            .baseUrl("http://localhost:11434/")
            .modelName("mistral")
            .build();
        chatMemory = MessageWindowChatMemory.builder()
            .maxMessages(20)
            .id("default")
            .build();
        chatMemory.add(new SystemMessage("You are a student advisor. Answer in Norwegian."));

        chatbotAgent = AiServices.builder(ChatbotAgent.class)
            .streamingChatLanguageModel(llm)
            .chatMemory(chatMemory)
            .contentRetriever(contentRetriever)
            .build();
    }

    public interface ChatbotAgent {
        TokenStream respond(String userMessage);
    }

    private void streamingChat()  {
        try (var scanner = new Scanner(System.in)) {
            System.out.print("user message to llm > ");
            while (true) {
                String line = scanner.nextLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                chatbotAgent.respond(line)
                    .onNext(token -> System.out.print(token))
                    .onComplete(response -> {
                        // System.out.println(chatMemory.messages());
                        System.out.print("\nuser message to llm > ");
                    })
                    .onError(error -> System.out.println("\n... oops, something went wrong:\n" + error))
                    .start();
            }
        }
    }

    public static void main(String[] args) {
        var chat = new RagChat();
        chat.setup();
        chat.streamingChat();
        System.exit(0);
    }
}
