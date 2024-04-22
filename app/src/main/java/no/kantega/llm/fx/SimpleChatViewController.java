package no.kantega.llm.fx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputControl;
import no.hal.fx.adapter.CompositeLabelAdapter;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingSource;
import no.hal.fx.bindings.BindingTarget;
import no.kantega.llm.fx.ChatMemoryViewController.ChatMemoryUpdate;

@Dependent
public class SimpleChatViewController extends AbstractChatViewController implements BindableView {

    private ChatMemory chatMemory;

    @Override
    protected ChatMemory getChatMemory() {
        return chatMemory;
    }

    private ChatbotAgent chatbotAgent;

    @Override
    protected ChatbotAgent getChatbotAgent() {
        return chatbotAgent;
    }

    @FXML
    TextInputControl systemPromptText;

    @FXML
    TextInputControl userMessageText;

    @FXML
    Button sendUserMessageAction;

    @FXML
    TextInputControl aiMessageText;

    @Override
    protected TextInputControl getAiMessageTextControl() {
        return aiMessageText;
    }

    @Inject
    Instance<LabelAdapter> labelAdapters;

    private Property<StreamingChatLanguageModel> chatModelProperty = new SimpleObjectProperty<StreamingChatLanguageModel>();
        
    private String systemPrompt = """
        You are a simple chatbot.
        """;

    private List<BindingSource<?>> bindingSources;

    @Override
    public List<BindingSource<?>> getBindingSources() {
        return bindingSources;
    }

    private List<BindingTarget<?>> bindingTargets;

    @Override
    public List<BindingTarget<?>> getBindingTargets() {
        return bindingTargets;
    }

    private Map<StreamingChatLanguageModel, ChatbotAgent> aiServices = new HashMap<>();

    @FXML
    void initialize() {
        systemPromptText.setText(systemPrompt);
        
        chatMemory = MessageWindowChatMemory.builder()
            .maxMessages(20)
            .id("default")
            .build();
        chatMemory.add(new SystemMessage(systemPrompt));

        chatModelProperty.subscribe(cm -> {
            if (cm != null) {
                chatbotAgent = aiServices.computeIfAbsent(cm, cm2 -> AiServices.builder(ChatbotAgent.class)
                    .streamingChatLanguageModel(cm2)
                    .chatMemory(chatMemory)
                    .build()
                );
            }
        });
        systemPromptText.textProperty().subscribe(text -> handleRestartChat());

        String sendUserMessageActionTextFormat = sendUserMessageAction.getText();
        LabelAdapter labelAdapter = CompositeLabelAdapter.of(this.labelAdapters);
        sendUserMessageAction.disableProperty().bind(chatModelProperty.map(Objects::isNull));
        var computedLabelValue = chatModelProperty.map(cm -> sendUserMessageActionTextFormat.formatted(labelAdapter.getText(cm)));
        sendUserMessageAction.textProperty().bind(computedLabelValue.orElse(sendUserMessageActionTextFormat.formatted("?")));

        bindingTargets = List.of(
            new BindingTarget<StreamingChatLanguageModel>(sendUserMessageAction, StreamingChatLanguageModel.class, chatModelProperty)
        );
        bindingSources = List.of(
            new BindingSource<ChatMemoryUpdate>(this.aiMessageText, ChatMemoryUpdate.class, chatMemoryUpdateProperty())
        );
    }

    @FXML
    void handleSendUserMessage(ActionEvent event) {
        handleSendUserMessage(userMessageText.getText(), event.getSource());
    }

    @Override
    protected void handleCompleteAiMessage(AiMessage aiMessage) {
        userMessageText.setText(null);
        super.handleCompleteAiMessage(aiMessage);
    }

    @FXML
    void handleRestartChat() {
        chatMemory.clear();
        chatMemory.add(new SystemMessage(systemPromptText.getText()));
        chatMemoryUpdated(null);
    }
}
