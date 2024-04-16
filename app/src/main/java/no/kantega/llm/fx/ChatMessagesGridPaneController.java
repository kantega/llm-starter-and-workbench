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
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
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
                    var messageText = new Label(message.text());
                    CopyToClipboardActionCreator.setContextMenu(messageText, Labeled::getText);
                    messageText.setWrapText(true);
                    chatMessagesGridPane.add(messageText, 1, row);
                    var agentText = new Text(message.type().name());
                    GridPane.setValignment(agentText, VPos.TOP);
                    chatMessagesGridPane.add(agentText, message.type() != ChatMessageType.AI ? 0 : 2, row);
                    row++;
                }
                default -> {}
            }
        }
        scrollToBottom();
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
            streamTarget = new Label(nextToken);
            streamTarget.setWrapText(true);
            int nextRow = chatMessagesGridPane.getChildrenUnmodifiable().size() / 2;
            chatMessagesGridPane.add(streamTarget, 1, nextRow);
            var agentText = new Text(ChatMessageType.AI.name());
            GridPane.setValignment(agentText, VPos.TOP);
            chatMessagesGridPane.add(agentText, 2, nextRow);
        } else {
            streamTarget.setText(streamTarget.getText() + nextToken);
        }
        scrollToBottom();
    }
}
