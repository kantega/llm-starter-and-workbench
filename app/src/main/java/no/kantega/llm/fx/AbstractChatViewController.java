package no.kantega.llm.fx;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.TokenStream;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import no.hal.fx.util.ActionProgressHelper;
import no.kantega.llm.fx.ChatMemoryViewController.ChatMemoryUpdate;

public abstract class AbstractChatViewController {

    protected abstract TextInputControl getAiMessageTextControl();
    
    protected abstract ChatMemory getChatMemory();

    private Property<ChatMemoryUpdate> chatMemoryUpdateProperty = new SimpleObjectProperty<ChatMemoryUpdate>();
    
    protected ObservableValue<ChatMemoryUpdate> chatMemoryUpdateProperty() {
        return chatMemoryUpdateProperty;
    }

    protected abstract ChatbotAgent getChatbotAgent();

    private ActionProgressHelper buttonActionProgressHelper = new ActionProgressHelper();

    protected void chatMemoryUpdated(String nextToken) {
        chatMemoryUpdateProperty.setValue(new ChatMemoryUpdate(getChatMemory(), nextToken));
    }

    protected void handleNextAiMessageToken(String nextToken) {
        if (getAiMessageTextControl().getText().isEmpty()) {
            chatMemoryUpdated(null);
        }
        if (getAiMessageTextControl() instanceof TextArea textArea) {
            textArea.appendText(nextToken);
            //textArea.setScrollTop(Double.MAX_VALUE);
        } else {
            getAiMessageTextControl().setText(getAiMessageTextControl().getText() + nextToken);
        }
        chatMemoryUpdated(nextToken);
    }

    protected void handleCompleteAiMessage(AiMessage aiMessage) {
        getAiMessageTextControl().setText(aiMessage.text());
        if (getAiMessageTextControl() instanceof TextArea textArea) {
            textArea.setScrollTop(Double.MAX_VALUE);
        }
        chatMemoryUpdated(null);
    }

    interface ChatbotAgent {
        TokenStream answer(String query);
    }

    void handleSendUserMessage(String userMessage, Object eventSource) {
        getAiMessageTextControl().setText("");
        buttonActionProgressHelper.performStreamingAction(eventSource, callback ->
            getChatbotAgent().answer(userMessage)
                .onNext(nextToken -> {
                    callback.call(null);
                    Platform.runLater(() -> handleNextAiMessageToken(nextToken));
                })
                .onComplete(answer -> {
                    callback.call(true);
                    Platform.runLater(() -> handleCompleteAiMessage(answer.content()));
                })
                .onError(throwable -> callback.call(false))
                .start()
            );
    }
}
