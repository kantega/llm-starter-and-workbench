///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS io.quarkus.platform:quarkus-bom:3.9.2@pom
//DEPS io.quarkus:quarkus-arc
//DEPS dev.langchain4j:langchain4j-ollama:0.29.1
//DEPS io.quarkiverse.langchain4j:quarkus-langchain4j-core:0.10.3
//DEPS io.quarkiverse.langchain4j:quarkus-langchain4j-ollama:0.10.3

//DEPS org.jsoup:jsoup:1.16.1
//DEPS io.github.furstenheim:copy_down:1.1

//SOURCES LlmServices.java
//FILES application.properties

//JAVAC_OPTIONS -parameters
//JAVA_OPTIONS -Djava.util.logging.manager=org.jboss.logmanager.LogManager

import java.util.*;
import java.util.function.*;

import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;

import io.quarkus.logging.Log;
import io.quarkus.runtime.*;
import jakarta.inject.Inject;

public class QuarkusRagChat {

    public static void main(String[] args) throws Exception {
        Quarkus.run(Runner.class, args);
    }

    public static class Runner implements QuarkusApplication {

        @Inject
        ChatMemory chatMemory;

        @Inject
        EmbeddingModel embeddingModel;

        @Inject
        EmbeddingStore embeddingStore;

        @Inject
        StreamingChatLanguageModel llm;

        @Inject
        DocumentParser documentParser;

        ChatbotAgent chatbotAgent;

        private void setup() {
            var documentsPath = "/Users/hal/git/fschat/files/html-cache/www__nord__no/__student__eksamen";
            List<Document> documents = FileSystemDocumentLoader.loadDocumentsRecursively(documentsPath, documentParser);
            EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 30))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build()
                .ingest(documents);

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

        @Override
        public int run(String... args) throws Exception {
            setup();
            streamingChat();
            return 0;
        }
    }
}
