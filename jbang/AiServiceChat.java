///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS dev.langchain4j:langchain4j:0.29.1
//DEPS dev.langchain4j:langchain4j-ollama:0.29.1

import java.util.*;
import java.util.function.*;

import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;

public class AiServiceChat {

    ChatbotAgent chatbotAgent;

    private void setup() {
        StreamingChatLanguageModel llm = OllamaStreamingChatModel.builder()
            .baseUrl("http://localhost:11434/")
            .modelName("mistral")
            .build();
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
            .maxMessages(20)
            .id("default")
            .build();
        chatMemory.add(new SystemMessage("Respond briefly, like a backend developer."));
    
        chatbotAgent = AiServices.builder(ChatbotAgent.class)
            .streamingChatLanguageModel(llm)
            .chatMemory(chatMemory)
            .build();
    }

    public interface ChatbotAgent {
        TokenStream respond(String userMessage);
    }

    private void streamingChat(Function<AiMessage, String> aiMessageHandler)  {
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
                        aiMessageHandler.apply(response.content());
                        System.out.print("\nuser message to llm > ");
                    })
                    .onError(error -> System.out.println("\n... oops, something went wrong!"))
                    .start();
            }
        }
    }

    public static void main(String[] args) {
        var chat = new AiServiceChat();
        chat.setup();
        chat.streamingChat(
            aiMessage -> aiMessage.text()
        );
        System.exit(0);
    }
}
