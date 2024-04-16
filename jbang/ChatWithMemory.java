///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS dev.langchain4j:langchain4j:0.29.1
//DEPS dev.langchain4j:langchain4j-ollama:0.29.1

import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.util.function.Function;

public class ChatWithMemory {

    ChatLanguageModel llm;
    ChatMemory chatMemory;

    private void setup() {
        llm = OllamaChatModel.builder()
            .baseUrl("http://localhost:11434/")
            .modelName("mistral")
            .build();
        chatMemory = MessageWindowChatMemory.builder()
            .maxMessages(20)
            .id("default")
            .build();
        chatMemory.add(new SystemMessage("You respond like a very shy and humble person."));
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
        chatMemory.add(new UserMessage(text));
        var answer = llm.generate(chatMemory.messages()).content();
        chatMemory.add(answer);
        return answer.text();
    }

    public static void main(String[] args) {
        var chat = new ChatWithMemory();
        chat.setup();
        chat(chat::respond);
    }
}
