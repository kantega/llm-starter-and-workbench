package no.kantega.llm.fx;

import java.util.List;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import jakarta.enterprise.context.Dependent;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import no.hal.fx.bindings.BindingTarget;
import no.hal.fx.bindings.BindingsTarget;

@Dependent
public class ChatMemoryViewController implements BindingsTarget {

    @FXML
    GridPane chatMessagesPane;

    private List<BindingTarget<?>> bindingTargets;

    @Override
    public List<BindingTarget<?>> getBindingTargets() {
        return bindingTargets;
    }

    public record ChatMemoryUpdate(ChatMemory chatMemory, String token, Object updateKey) {
        public ChatMemoryUpdate(ChatMemory chatMemory, String token) {
            this(chatMemory, token, System.currentTimeMillis());
        }
    }

    private Property<ChatMemoryUpdate> chatMemoryProperty = new SimpleObjectProperty<ChatMemoryUpdate>();

    private ChatMessagesGridPaneController chatMemoryGridPaneController;
    
    private List<ChatMessage> initialChatMessages = List.of(
        new UserMessage("I'm the user"),
        new AiMessage("And I'm the chat model")
    );

    @FXML
    void initialize() {
        chatMemoryGridPaneController = new ChatMessagesGridPaneController(chatMessagesPane);
        chatMemoryProperty.subscribe(cm -> {
            if (cm != null) {
                if (cm.token() != null) {
                    chatMemoryGridPaneController.nextTokenProperty().setValue(cm.token());
                } else {
                    chatMemoryGridPaneController.chatMessagesList().setAll(cm != null ? cm.chatMemory().messages() : List.of());
                }
            }
        });
        chatMemoryGridPaneController.chatMessagesList().setAll(initialChatMessages);

        bindingTargets = List.of(
            new BindingTarget<ChatMemoryUpdate>(chatMessagesPane, ChatMemoryUpdate.class, chatMemoryProperty)
        );
    }
}
