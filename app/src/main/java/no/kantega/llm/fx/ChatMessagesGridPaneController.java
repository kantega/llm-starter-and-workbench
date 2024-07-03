package no.kantega.llm.fx;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import jakarta.enterprise.context.Dependent;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import no.hal.fx.util.CopyToClipboardActionCreator;

@Dependent
public class ChatMessagesGridPaneController {

    public ChatMessagesGridPaneController() {
    }

    private GridPane chatMessagesGridPane;

    @FXML
    void setChatMemoryGridPane(GridPane chatMemoryGridPane) {
        this.chatMessagesGridPane = chatMemoryGridPane;
    }
    
    public ChatMessagesGridPaneController(GridPane chatMemoryGridPane) {
        setChatMemoryGridPane(chatMemoryGridPane);
        initialize();
    }

    private ObservableList<ChatMessage> chatMessages = FXCollections.observableArrayList();

    public ObservableList<ChatMessage> chatMessagesList() {
        return chatMessages;
    }

    private Property<String> nextTokenProperty = new SimpleObjectProperty<String>();

    public Property<String> nextTokenProperty() {
        return nextTokenProperty;
    }

    @FXML
    void initialize() {
        chatMessages.subscribe(() -> updateChatMessagesView());
        nextTokenProperty.subscribe(token -> updateStreamingTarget(token));
    }

    private void updateChatMessagesView() {
        chatMessagesGridPane.getChildren().clear();
        streamTarget = null;
        int row = 0;
        for (var message : chatMessages) {
            switch (message.type()) {
                case ChatMessageType.AI, ChatMessageType.USER, ChatMessageType.SYSTEM -> {
                    chatMessagesGridPane.add(createMessageText(message), 1, row);
                    chatMessagesGridPane.add(createAgentText(message.type()), message.type() != ChatMessageType.AI ? 0 : 2, row);
                    row++;
                }
                default -> {}
            }
        }
        scrollToBottom();
    }

    private void setMessageTextStyle(Node node, ChatMessageType messageType, String genericStyle) {
        var messageStyle = messageType.name().toLowerCase() + "-chat-message";
        node.getStyleClass().addAll(genericStyle, messageStyle);
    }
    
    private Labeled createMessageText(ChatMessageType messageType, String messageText) {
        var messageTextNode = new Label(messageText);
        setMessageTextStyle(messageTextNode, messageType, "chat-message");
        CopyToClipboardActionCreator.setContextMenu(messageTextNode, Labeled::getText);
        messageTextNode.setWrapText(true);
        return messageTextNode;
    }

    private Labeled createMessageText(ChatMessage message) {
        return createMessageText(message.type(), message.text());
    }

    private Labeled createAgentText(ChatMessageType messageType) {
        var agentText = new Label(messageType.name());
        setMessageTextStyle(agentText, messageType, "chat-message-role");
        GridPane.setValignment(agentText, VPos.TOP);
        return agentText;
    }

    private void scrollToBottom() {
        Parent parent = chatMessagesGridPane.getParent();
        while (parent != null) {
            if (! parent.getClass().getName().contains(".skin.")) {
                break;
            }
            parent = parent.getParent();
        }
        if (parent instanceof ScrollPane scrollPane) {
            scrollPane.setVvalue(scrollPane.getVmax());
        }
    }

    private Labeled streamTarget = null;

    public void updateStreamingTarget(String nextToken) {
        if (nextToken == null) {
            streamTarget = null;
        } else if (streamTarget == null) {
            streamTarget = createMessageText(ChatMessageType.AI, nextToken);
            int nextRow = chatMessagesGridPane.getChildrenUnmodifiable().size() / 2;
            chatMessagesGridPane.add(streamTarget, 1, nextRow);
            chatMessagesGridPane.add(createAgentText(ChatMessageType.AI), 2, nextRow);
        } else {
            streamTarget.setText(streamTarget.getText() + nextToken);
        }
        scrollToBottom();
    }
}
