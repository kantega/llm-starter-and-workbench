package no.kantega.openai.fx;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;

import dev.langchain4j.model.openai.OpenAiChatModelName;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import no.kantega.llm.ModelManager;
import no.kantega.openai.OpenaiModels;

@Dependent
public class OpenaiChatModelViewController {

    @FXML
    PropertySheet openaiChatModelPropertySheet;

    @Inject
    ModelManager modelManager;

    private OpenaiModels openaiModels;

    @Inject
    void setOpenaiModels(OpenaiModels openaiModels) {
        this.openaiModels = openaiModels;
        temperatureProperty.setValue(openaiModels.getTemperature());
    }

    private SimpleObjectProperty<OpenAiChatModelName> modelNameProperty = new SimpleObjectProperty<>();
    private DoubleProperty temperatureProperty = new SimpleDoubleProperty(0.8);
    private DoubleProperty topPProperty = new SimpleDoubleProperty(0.9);
    private IntegerProperty seedProperty = new SimpleIntegerProperty(0);

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
            new PropertySheetItem<OpenAiChatModelName>("Model name", OpenAiChatModelName.class, modelNameProperty, value -> modelNameProperty.setValue((OpenAiChatModelName) value)),
            new PropertySheetItem<Double>("Temperature", Double.class, temperatureProperty, value -> temperatureProperty.setValue((Double) value)),
            new PropertySheetItem<Double>("Top P", Double.class, topPProperty, value -> topPProperty.setValue((Double) value)),
            new PropertySheetItem<Integer>("Seed", Integer.class, seedProperty, value -> seedProperty.setValue((Integer) value))
        );
    }
    
    @FXML
    void createStreamingChatModel() {
        modelManager.registerModel(new OpenaiModels.StreamingChatModelConfiguration(
            openaiModels.getOpenApiKey(),
            modelNameProperty.getValue(),
            new OpenaiModels.StreamingChatModelOptions(
                temperatureProperty.getValue(),
                topPProperty.getValue(),
                seedProperty.getValue()
            )
        ));
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
