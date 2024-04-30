package no.kantega.ollama.fx;

import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.function.Consumer;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
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
import no.kantega.ollama.OllamaService;

@Dependent
public class OllamaChatModelViewController implements BindableView {

    @FXML
    PropertySheet ollamaChatModelPropertySheet;

    // @FXML
    // Button chatModelAction;

    @FXML
    Button streamingChatModelAction;

    // private Property<ChatLanguageModel> chatModelProperty = new SimpleObjectProperty<>();
    private Property<StreamingChatLanguageModel> streamingChatModelProperty = new SimpleObjectProperty<>();

    private OllamaService ollamaServices;

    @Inject
    void setOllamaServices(OllamaService ollamaServices) {
        this.ollamaServices = ollamaServices;
        baseUrlProperty.setValue(ollamaServices.getBaseUrl());
    }

    private StringProperty baseUrlProperty = new SimpleStringProperty("http://localhost:11434/");
    private StringProperty modelNameProperty = new SimpleStringProperty();
    private DoubleProperty temperatureProperty = new SimpleDoubleProperty(0.8);
    private IntegerProperty topKProperty = new SimpleIntegerProperty(40);
    private DoubleProperty topPProperty = new SimpleDoubleProperty(0.9);
    private DoubleProperty repeatPenaltyProperty = new SimpleDoubleProperty(1.1);
    private IntegerProperty seedProperty = new SimpleIntegerProperty(0);
    private IntegerProperty numPredictProperty = new SimpleIntegerProperty(128);
    private IntegerProperty numCtxProperty = new SimpleIntegerProperty(2048);

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

    private ObservableList<String> modelNameChoices = FXCollections.observableArrayList();

    void updateChatModelChoices(SequencedCollection<String> modelNames) {
        modelNameChoices.setAll(modelNames);
        modelNameProperty.setValue((modelNames.isEmpty() ? "" : modelNames.getFirst()));
    }

    @FXML
    void initialize() {
        var defaultPropertyEditorFactory = ollamaChatModelPropertySheet.getPropertyEditorFactory();
        ollamaChatModelPropertySheet.setPropertyEditorFactory(propertySheetItem -> {
            if ("Model name".equals(propertySheetItem.getName())) {
                return createChoiceEditor(propertySheetItem, modelNameChoices);
            }
            return defaultPropertyEditorFactory.call(propertySheetItem);
        });
        ollamaChatModelPropertySheet.getItems().addAll(
            // new PropertySheetItem<String>("Base URL", String.class, baseUrlProperty, value -> baseUrlProperty.setValue((String) value)),
            new PropertySheetItem<String>("Model name", String.class, modelNameProperty, value -> modelNameProperty.setValue((String) value)),
            new PropertySheetItem<Double>("Temperature", Double.class, temperatureProperty, value -> temperatureProperty.setValue((Double) value)),
            new PropertySheetItem<Integer>("Top K", Integer.class, topKProperty, value -> topKProperty.setValue((Integer) value)),
            new PropertySheetItem<Double>("Top P", Double.class, topPProperty, value -> topPProperty.setValue((Double) value)),
            new PropertySheetItem<Double>("Repeat penalty", Double.class, repeatPenaltyProperty, value -> repeatPenaltyProperty.setValue((Number) value)),
            new PropertySheetItem<Integer>("Seed", Integer.class, seedProperty, value -> seedProperty.setValue((Integer) value)),
            new PropertySheetItem<Integer>("Token count to predict", Integer.class, numPredictProperty, value -> numPredictProperty.setValue((Integer) value)),
            new PropertySheetItem<Integer>("Token context size", Integer.class, numCtxProperty, value -> numCtxProperty.setValue((Integer) value))
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
        var chatModel = ollamaServices.withStreamingChatModelLabel(modelName, name -> OllamaStreamingChatModel.builder()
            .baseUrl(baseUrlProperty.getValue())
            .modelName(name)
            .temperature(temperatureProperty.getValue())
            .topK(topKProperty.getValue())
            .topP(topPProperty.getValue())
            .repeatPenalty(repeatPenaltyProperty.getValue())
            .seed(seedProperty.getValue())
            .numPredict(numPredictProperty.getValue())
            .numCtx(numCtxProperty.getValue())
            .build()
        );
        streamingChatModelProperty.setValue(chatModel);
    }

    public static final <T> PropertyEditor<?> createChoiceEditor(Item property, final ObservableList<T> choices ) {
         
        return new AbstractPropertyEditor<T, ComboBox<T>>(property, new ComboBox<T>()) {
            
            { getEditor().setItems(choices); }
            
            @Override protected ObservableValue<T> getObservableValue() {
                return getEditor().getSelectionModel().selectedItemProperty();
            }

            @Override public void setValue(T value) {
                getEditor().getSelectionModel().select(value);
            }
        };
    }
}
