package no.kantega.llm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class ModelManager {
    
    private Map<ModelConfiguration<?>, Object> models = new HashMap<>();
    private Map<Object, ModelConfiguration<?>> modelConfigurations = new HashMap<>();

    public <T> void forEachModel(Class<T> modelClass, Consumer<T> consumer) {
        models.values().stream().filter(modelClass::isInstance).forEach(model -> consumer.accept(modelClass.cast(model)));
    }

    public <T> Optional<T> getModel(ModelConfiguration<T> modelConfiguration) {
        return (models.containsKey(modelConfiguration) ? Optional.of((T) models.get(modelConfiguration)) : Optional.empty());
    }
    
    public <T> ModelConfiguration<T> getModelConfiguration(T model) {
        return (ModelConfiguration<T>) modelConfigurations.get(model);
    }
    public <T> String getModelName(T model) {
        return getModelConfiguration(model).modelName();
    }

    private <T> void registerModel(ModelConfiguration<T> modelConfiguration, T model) {
        models.put(modelConfiguration, model);
        modelConfigurations.put(model, modelConfiguration);
        System.out.println("Registered model: " + modelConfiguration);
    }

    public <T> T registerModel(ModelConfiguration<T> modelConfiguration) {
        if (! models.containsKey(modelConfiguration)) {
            T model = modelConfiguration.buildModel();
            models.put(modelConfiguration, model);
            modelConfigurations.put(model, modelConfiguration);
            fireModelAdded(modelConfiguration, model);
        }
        return (T) models.get(modelConfiguration);
    }

    public void unregisterModel(Object model) {
        ModelConfiguration<?> modelConfiguration = modelConfigurations.get(model);
        if (modelConfiguration != null) {
            models.remove(modelConfiguration);
            modelConfigurations.remove(model);
            fireModelRemoved(modelConfiguration, modelConfiguration);
        }
    }

    public void unregisterModel(ModelConfiguration<?> modelConfiguration) {
        Object model = models.get(modelConfiguration);
        models.remove(modelConfiguration);
        modelConfigurations.remove(model);
    }

    //

    @Inject
    void initializeModels(Instance<List<ModelConfiguration<?>>> modelConfigurations) {
        modelConfigurations.forEach(configs -> configs.forEach(this::registerModel));
    }

    //
    
    public interface Listener {
        public void modelAdded(ModelConfiguration<?> modelConfiguration, Object model);
        public void modelRemoved(ModelConfiguration<?> modelConfiguration, Object model);
    }

    private Collection<Listener> listeners = new ArrayList<>();

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void fireModelAdded(ModelConfiguration<?> modelConfiguration, Object model) {
        for (Listener listener : listeners) {
            listener.modelAdded(modelConfiguration, model);
        }
    }

    private void fireModelRemoved(ModelConfiguration<?> modelConfiguration, Object model) {
        for (Listener listener : listeners) {
            listener.modelRemoved(modelConfiguration, model);
        }
    }
}
