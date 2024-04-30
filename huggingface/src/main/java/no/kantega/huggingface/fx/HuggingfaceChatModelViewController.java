package no.kantega.huggingface.fx;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.controlsfx.control.PropertySheet;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingSource;
import no.kantega.huggingface.HuggingfaceService;

@Dependent
public class HuggingfaceChatModelViewController implements BindableView {

    @ConfigProperty(name = "langchain4j.huggingface.api-key")
    String apiKey;

    @FXML
    PropertySheet huggingfaceChatModelPropertySheet;

    @FXML
    Button chatModelAction;

    private Property<ChatLanguageModel> chatModelProperty = new SimpleObjectProperty<>();

    private StringProperty baseUrlProperty = new SimpleStringProperty("http://localhost:11434/");
    private StringProperty modelNameProperty = new SimpleStringProperty("tiiuae/falcon-7b-instruct");
    private DoubleProperty temperatureProperty = new SimpleDoubleProperty(0.8);
    private IntegerProperty maxNewTokensProperty = new SimpleIntegerProperty(128);
    private BooleanProperty returnFullProperty = new SimpleBooleanProperty(true);
    private BooleanProperty waitForModelProperty = new SimpleBooleanProperty(true);

    private List<BindingSource<?>> bindingSources;

    @Override
    public List<BindingSource<?>> getBindingSources() {
        return this.bindingSources;
    }

    private record PropertySheetItem<T>(String name, Class<T> clazz, ObservableValue<? super T> property, Consumer<Object> setter) implements PropertySheet.Item {
        @Override
        public Class<?> getType() {
            return clazz;
        }
        @Override
        public String getCategory() {
            return "default";
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public String getDescription() {
            return name;
        }
        @Override
        public Object getValue() {
            return property.getValue();
        }
        @Override
        public void setValue(Object value) {
            setter.accept(value);
        }
        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
            return Optional.of(property);
        }
    }

    @FXML
    void initialize() {
        huggingfaceChatModelPropertySheet.getItems().addAll(
            // new PropertySheetItem<String>("Base URL", String.class, baseUrlProperty, value -> baseUrlProperty.setValue((String) value)),
            new PropertySheetItem<String>("Model name", String.class, modelNameProperty, value -> modelNameProperty.setValue((String) value)),
            new PropertySheetItem<Double>("Temperature", Double.class, temperatureProperty, value -> temperatureProperty.setValue((Double) value))
            // new PropertySheetItem<Integer>("Max new tokens", Integer.class, maxNewTokensProperty, value -> maxNewTokensProperty.setValue((Integer) value)),
            // new PropertySheetItem<Boolean>("Return full-text", Boolean.class, returnFullProperty, value -> returnFullProperty.setValue(Boolean.valueOf(value.toString()))),
            // new PropertySheetItem<Boolean>("Wait for model", Boolean.class, waitForModelProperty, value -> waitForModelProperty.setValue(Boolean.valueOf(value.toString())))
        );
        this.bindingSources = List.of(
            new BindingSource<ChatLanguageModel>(this.chatModelAction, ChatLanguageModel.class, chatModelProperty)
        );
    }
    
    @Inject
    HuggingfaceService huggingfaceService;

    @FXML
    void createAndUpdateChatModel() {
        var modelName = modelNameProperty.getValue();
        var chatModel = huggingfaceService.withChatModelLabel(modelName, name -> HuggingFaceChatModel.builder()
            .modelId(name)
            .temperature(temperatureProperty.getValue())
            .accessToken(apiKey)
            // .maxNewTokens(maxNewTokensProperty.getValue())
            // .returnFullText(returnFullProperty.getValue())
            // .waitForModel(waitForModelProperty.getValue())
            .build()
        );
        chatModelProperty.setValue(chatModel);
    }
}
