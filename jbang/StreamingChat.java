///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS dev.langchain4j:langchain4j:0.29.1
//DEPS dev.langchain4j:langchain4j-ollama:0.29.1

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;

public class StreamingChat {

    StreamingChatLanguageModel llm;
    ChatMemory chatMemory;

    private void setup() {
        llm = OllamaStreamingChatModel.builder()
            .baseUrl("http://localhost:11434/")
            .modelName("mistral")
            .build();
        chatMemory = MessageWindowChatMemory.builder()
            .maxMessages(20)
            .id("default")
            .build();
        chatMemory.add(new SystemMessage("Respond briefly, like a backend developer."));
    }

    private void streamingChat(
        Function<String, UserMessage> userMessageHandler,
        Function<AiMessage, String> aiMessageHandler
    )  {
        try (var scanner = new Scanner(System.in)) {
            System.out.print("user message to llm > ");
            while (true) {
                String line = scanner.nextLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                var userMessage = userMessageHandler.apply(line);
                chatMemory.add(userMessage);
                llm.generate(chatMemory.messages(), new StreamingResponseHandler<AiMessage>() {
                    public void onNext(String token) {
                        System.out.print(token);
                    }
                    public void onComplete(Response<AiMessage> response) {
                        aiMessageHandler.apply(response.content());
                        System.out.print("\nuser message to llm > ");
                    }
                    public void onError(Throwable error) {
                        System.out.println("\n... oops, something went wrong!");
                    }
                });
            }
        }
    }

    public static void main(String[] args) {
        var chat = new StreamingChat();
        chat.setup();
        chat.streamingChat(
            userMessageText -> new UserMessage(userMessageText),
            aiMessage -> {
                chat.chatMemory.add(aiMessage);
                return aiMessage.text();
            }
        );
        System.exit(0);
    }
}
