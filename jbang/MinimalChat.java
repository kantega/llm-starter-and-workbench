///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS dev.langchain4j:langchain4j-ollama:0.29.1

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class MinimalChat {

    ChatLanguageModel llm;

    private void setup() {
        llm = OllamaChatModel.builder()
            .baseUrl("http://localhost:11434/")
            .modelName("mistral")
            .build();
    }

    private void chat() {
        try (var scanner = new java.util.Scanner(System.in)) {
            while (true) {
                System.out.print("user message to llm > ");
                String line = scanner.nextLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                System.out.println(llm.generate(line));
            }
        }
    }

    public static void main(String[] args) {
        var chat = new MinimalChat();
        chat.setup();
        chat.chat();
    }
}
