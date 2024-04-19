package no.kantega.llm.fx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import no.hal.fx.adapter.CompositeLabelAdapter;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingSource;
import no.hal.fx.bindings.BindingTarget;
import no.hal.fx.util.ButtonActionProgressHelper;
import no.kantega.llm.fx.ChatMemoryViewController.ChatMemoryUpdate;
import no.kantega.llm.fx.IngestorViewController.TextSegmentEmbeddings;

@Dependent
public class RagChatViewController implements BindableView {

    @FXML
    TextInputControl systemPromptText;

    @FXML
    TextInputControl userMessageText;

    @FXML
    Button sendUserMessageAction;

    @FXML
    TextInputControl aiMessageText;

    @Inject
    Instance<LabelAdapter> labelAdapters;

    private Property<StreamingChatLanguageModel> chatModelProperty = new SimpleObjectProperty<StreamingChatLanguageModel>();
    
    private ChatMemory chatMemory;

    private Property<ChatMemoryUpdate> chatMemoryUpdateProperty = new SimpleObjectProperty<ChatMemoryUpdate>();

    private ChatbotAgent chatbotAgent;

    private ButtonActionProgressHelper buttonActionProgressHelper;
    
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
    
    private Property<TextSegmentEmbeddings> textSegmentEmbeddingsProperty = new SimpleObjectProperty<TextSegmentEmbeddings>();
    
    private void updateChatbotAgent() {
        var chatModel = chatModelProperty.getValue();
        var textSegmentEmbeddings = textSegmentEmbeddingsProperty.getValue();
        if (chatModel != null && textSegmentEmbeddings != null) {
            var embeddingStore = EmbeddingsSearchViewController.getEmbeddingStore(textSegmentEmbeddings);
            var contentRetriever = new EmbeddingStoreContentRetriever(embeddingStore, textSegmentEmbeddings.embeddingModel(), 5, 0.5);
            chatbotAgent = aiServices.computeIfAbsent(chatModel, cm -> AiServices.builder(ChatbotAgent.class)
                .streamingChatLanguageModel(cm)
                .chatMemory(chatMemory)
                .contentRetriever(contentRetriever)
                .build()
            );
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

        chatModelProperty.subscribe(this::updateChatbotAgent);
        textSegmentEmbeddingsProperty.subscribe(this::updateChatbotAgent);

        buttonActionProgressHelper = new ButtonActionProgressHelper();
        systemPromptText.textProperty().subscribe(text -> handleRestartChat());

        String sendUserMessageActionTextFormat = sendUserMessageAction.getText();
        LabelAdapter labelAdapter = CompositeLabelAdapter.of(this.labelAdapters);
        sendUserMessageAction.disableProperty().bind(chatModelProperty.map(Objects::isNull));
        var computedLabelValue = chatModelProperty.map(cm -> sendUserMessageActionTextFormat.formatted(labelAdapter.getText(cm)));
        sendUserMessageAction.textProperty().bind(computedLabelValue.orElse(sendUserMessageActionTextFormat.formatted("?")));

        bindingTargets = List.of(
            new BindingTarget<StreamingChatLanguageModel>(sendUserMessageAction, StreamingChatLanguageModel.class, chatModelProperty),
            new BindingTarget<TextSegmentEmbeddings>(sendUserMessageAction, TextSegmentEmbeddings.class, textSegmentEmbeddingsProperty)
        );
        bindingSources = List.of(
            new BindingSource<ChatMemoryUpdate>(this.aiMessageText, ChatMemoryUpdate.class, chatMemoryUpdateProperty)
        );
    }

    private void chatMemoryUpdated(String nextToken) {
        chatMemoryUpdateProperty.setValue(new ChatMemoryUpdate(chatMemory, nextToken));
    }

    @FXML
    void handleSendUserMessage(ActionEvent event) {
        aiMessageText.setText("");
        if (chatbotAgent == null) {
            updateChatbotAgent();
        }
        buttonActionProgressHelper.performStreamingAction(event.getSource(), callback ->
            chatbotAgent.answer(userMessageText.getText())
                .onNext(
                    nextToken -> {
                        callback.call(null);
                        Platform.runLater(() -> {
                            if (aiMessageText.getText().isEmpty()) {
                                chatMemoryUpdated(null);
                            }
                            aiMessageText.setText(aiMessageText.getText() + nextToken);
                            chatMemoryUpdated(nextToken);
                        });
                    }    
                )
                .onComplete(
                    answer -> {
                        callback.call(true);
                        Platform.runLater(() -> {
                            userMessageText.setText(null);
                            aiMessageText.setText(answer.content().text());
                            if (aiMessageText instanceof TextArea textArea) {
                                textArea.setScrollTop(Double.MAX_VALUE);
                            }
                            chatMemoryUpdated(null);
                        });
                    }
                )
                .onError(throwable -> callback.call(false))
                .start()
            );
    }

    @FXML
    void handleRestartChat() {
        chatMemory.clear();
        chatMemory.add(new SystemMessage(systemPromptText.getText()));
        chatMemoryUpdated(null);
    }

    interface ChatbotAgent {
        TokenStream answer(String query);
    }
}
