///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS dev.langchain4j:langchain4j:0.29.1
//DEPS dev.langchain4j:langchain4j-ollama:0.29.1

import java.util.*;

import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.util.function.Function;

public class ChatWithPrompt {

    ChatLanguageModel llm;
    ChatMemory chatMemory;
    PromptTemplate promptTemplate;

    private void setup() {
        llm = OllamaChatModel.builder()
            .baseUrl("http://localhost:11434/")
            .modelName("mistral")
            .build();
        chatMemory = MessageWindowChatMemory.builder()
            .maxMessages(20)
            .id("default")
            .build();
        promptTemplate = PromptTemplate.from("""
            Below is a user message. First, sumnmarize it, and then provide the response.
            Finally, output a random fact of this day of year, given today's date is {{current_date}}.
            User message: "{{user_message}}"
            """);
    }

    private static void chat(Function<String, String> responder) {
        try (var scanner = new java.util.Scanner(System.in)) {
            while (true) {
                System.out.print("user message to llm > ");
                String line = scanner.nextLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                System.out.println(responder.apply(line));
            }
        }
    }

    private String respond(String text) {
        chatMemory.add(promptTemplate.apply(Map.of(
            "user_message", text
        )).toUserMessage());
        var answer = llm.generate(chatMemory.messages()).content();
        chatMemory.add(answer);
        return answer.text();
    }

    public static void main(String[] args) {
        var chat = new ChatWithPrompt();
        chat.setup();
        chat(chat::respond);
    }
}
