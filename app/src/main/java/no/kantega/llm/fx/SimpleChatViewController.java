package no.kantega.llm.fx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
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

    private Object chatbotAgent;

    @Override
    protected ChatbotAgent getChatbotAgent() {
        return getChatbotAgent(chatbotAgent);
    }
    @Override
    protected StreamingChatbotAgent getStreamingChatbotAgent() {
        return getStreamingChatbotAgent(chatbotAgent);
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
    Instance<LabelAdapter<?>> labelAdapters;

    private Property<ChatLanguageModel> chatModelProperty = new SimpleObjectProperty<ChatLanguageModel>();
    private Property<StreamingChatLanguageModel> streamingChatModelProperty = new SimpleObjectProperty<StreamingChatLanguageModel>();
        
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

    private Map<ChatLanguageModel, ChatbotAgent> chatServices = new HashMap<>();
    private Map<StreamingChatLanguageModel, StreamingChatbotAgent> streamingChatServices = new HashMap<>();

    private void updateChatbotAgent(Object chatModel) {
        switch (chatModel) {
            case ChatLanguageModel clm -> {
                chatbotAgent = chatServices.computeIfAbsent(clm, ignore -> AiServices.builder(ChatbotAgent.class)
                    .chatLanguageModel(clm)
                    .chatMemory(chatMemory)
                    .build()
                );
            }
            case StreamingChatLanguageModel sclm -> {
                chatbotAgent = streamingChatServices.computeIfAbsent(sclm, ignore -> AiServices.builder(StreamingChatbotAgent.class)
                    .streamingChatLanguageModel(sclm)
                    .chatMemory(chatMemory)
                    .build()
                );
            }
            case null, default -> {}
        }
    }

    @FXML
    void initialize() {
        systemPromptText.setText(systemPrompt);
        
        chatMemory = MessageWindowChatMemory.builder()
            .maxMessages(20)
            .id("default")
            .build();
        chatMemory.add(new SystemMessage(systemPrompt));

        LabelAdapter<Object> labelAdapter = CompositeLabelAdapter.of(this.labelAdapters);

        String sendUserMessageActionTextFormat = sendUserMessageAction.getText();
        chatModelProperty.subscribe(cm -> {
            updateChatbotAgent(cm);
            var label = labelAdapter.getText(cm);
            sendUserMessageAction.setText(sendUserMessageActionTextFormat.formatted(label != null ? label : "?"));
        });
        streamingChatModelProperty.subscribe(cm -> {
            updateChatbotAgent(cm);
            var label = labelAdapter.getText(cm);
            sendUserMessageAction.setText(sendUserMessageActionTextFormat.formatted(label != null ? label : "?"));
        });

        systemPromptText.textProperty().subscribe(text -> handleRestartChat());

        bindingTargets = List.of(
            new BindingTarget<ChatLanguageModel>(sendUserMessageAction, ChatLanguageModel.class, chatModelProperty),
            new BindingTarget<StreamingChatLanguageModel>(sendUserMessageAction, StreamingChatLanguageModel.class, streamingChatModelProperty)
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
        userMessageText.setText("");
        aiMessageText.setText("");
        chatMemory.clear();
        String systemPrompt = systemPromptText.getText();
        if (systemPrompt != null && (! systemPrompt.isBlank())) {
            chatMemory.add(new SystemMessage(systemPrompt));
        }
        chatMemoryUpdated(null);
    }
}
