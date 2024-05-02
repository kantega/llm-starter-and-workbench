package no.kantega.huggingface.fx;

import java.util.Optional;
import java.util.function.Consumer;

import org.controlsfx.control.PropertySheet;

import dev.langchain4j.model.huggingface.HuggingFaceModelName;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import no.kantega.huggingface.HuggingfaceModels;
import no.kantega.llm.ModelManager;

@Dependent
public class HuggingfaceChatModelViewController {

    @FXML
    PropertySheet huggingfaceChatModelPropertySheet;

    // private StringProperty baseUrlProperty = new SimpleStringProperty("http://localhost:11434/");
    private StringProperty modelNameProperty = new SimpleStringProperty(HuggingFaceModelName.TII_UAE_FALCON_7B_INSTRUCT);
    private DoubleProperty temperatureProperty = new SimpleDoubleProperty(0.8);
    // private IntegerProperty maxNewTokensProperty = new SimpleIntegerProperty(128);
    // private BooleanProperty returnFullProperty = new SimpleBooleanProperty(true);
    // private BooleanProperty waitForModelProperty = new SimpleBooleanProperty(true);

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
    }

    @Inject
    ModelManager modelManager;
    
    @Inject
    HuggingfaceModels huggingfaceModels;

    @FXML
    void createChatModel() {
        modelManager.registerModel(new HuggingfaceModels.ChatModelConfiguration(
            huggingfaceModels.getApiKey(),
            modelNameProperty.getValue(),
            new HuggingfaceModels.StreamingChatModelOptions(
                temperatureProperty.getValue()
            )
        ));
    }
}
