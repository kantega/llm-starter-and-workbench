package no.kantega.openai.fx;

import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.function.Consumer;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingSource;
import no.kantega.openai.OpenaiServices;

@Dependent
public class OpenaiChatModelViewController implements BindableView {

    @FXML
    PropertySheet openaiChatModelPropertySheet;

    @FXML
    Button streamingChatModelAction;

    private Property<StreamingChatLanguageModel> streamingChatModelProperty = new SimpleObjectProperty<>();

    private OpenaiServices openaiServices;
    
    @Inject
    public void setOpenaiServices(OpenaiServices openaiServices) {
        this.openaiServices = openaiServices;
        temperatureProperty.setValue(openaiServices.getTemperature());
    }

    private SimpleObjectProperty<OpenAiChatModelName> modelNameProperty = new SimpleObjectProperty<>();
    private DoubleProperty temperatureProperty = new SimpleDoubleProperty(0.8);
    private DoubleProperty topPProperty = new SimpleDoubleProperty(0.9);
    private IntegerProperty seedProperty = new SimpleIntegerProperty(0);

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
        var defaultPropertyEditorFactory = openaiChatModelPropertySheet.getPropertyEditorFactory();
        openaiChatModelPropertySheet.setPropertyEditorFactory(propertySheetItem -> {
            if ("Model name".equals(propertySheetItem.getName())) {
                return createChoiceEditor(propertySheetItem, List.of(OpenAiChatModelName.values()));
            }
            return defaultPropertyEditorFactory.call(propertySheetItem);
        });
        modelNameProperty.setValue(OpenAiChatModelName.GPT_3_5_TURBO);
        openaiChatModelPropertySheet.getItems().addAll(
            new PropertySheetItem<OpenAiChatModelName>("Model name", OpenAiChatModelName.class, modelNameProperty, value -> modelNameProperty.setValue(OpenAiChatModelName.valueOf(value.toString()))),
            new PropertySheetItem<Double>("Temperature", Double.class, temperatureProperty, value -> temperatureProperty.setValue((Double) value)),
            new PropertySheetItem<Double>("Top P", Double.class, topPProperty, value -> topPProperty.setValue((Double) value)),
            new PropertySheetItem<Integer>("Seed", Integer.class, seedProperty, value -> seedProperty.setValue((Integer) value))
        );
        this.bindingSources = List.of(
            // new BindingSource<ChatLanguageModel>(this.chatModelAction, ChatLanguageModel.class, chatModelProperty),
            new BindingSource<StreamingChatLanguageModel>(this.streamingChatModelAction, StreamingChatLanguageModel.class, streamingChatModelProperty)
        );
    }
    
    // @FXML
    // void createAndUpdateChatModel() {
    //     var modelName = modelNameProperty.getValue();
    //     var chatModel = ollamaServices.withChatModelLabel(modelName, name -> OllamaChatModel.builder()
    //         .baseUrl(baseUrlProperty.getValue())
    //         .modelName(name)
    //         .temperature(temperatureProperty.getValue())
    //         .topK(topKProperty.getValue())
    //         .topP(topPProperty.getValue())
    //         .repeatPenalty(repeatPenaltyProperty.getValue())
    //         .seed(seedProperty.getValue())
    //         .numPredict(numPredictProperty.getValue())
    //         .numCtx(numCtxProperty.getValue())
    //         .build()
    //     );
    //     chatModelProperty.setValue(chatModel);
    // }

    @FXML
    void createAndUpdateStreamingChatModel() {
        var modelName = modelNameProperty.getValue();
        var chatModel = openaiServices.withStreamingChatModelLabel(modelName, name -> OpenAiStreamingChatModel.builder()
            .modelName(name)
            .temperature(temperatureProperty.getValue())
            .topP(topPProperty.getValue())
            .seed(seedProperty.getValue())
            .build()
        );
        streamingChatModelProperty.setValue(chatModel);
    }

    public static final <T> PropertyEditor<?> createChoiceEditor(Item property, final List<T> choices ) {
         
        return new AbstractPropertyEditor<T, ComboBox<T>>(property, new ComboBox<T>()) {
            
            { getEditor().getItems().setAll(choices); }
            
            @Override protected ObservableValue<T> getObservableValue() {
                return getEditor().getSelectionModel().selectedItemProperty();
            }

            @Override public void setValue(T value) {
                getEditor().getSelectionModel().select(value);
            }
        };
    }
}
