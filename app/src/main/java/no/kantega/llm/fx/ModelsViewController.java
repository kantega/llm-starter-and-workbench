package no.kantega.llm.fx;

import java.util.List;
import java.util.function.Predicate;

import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ListView;
import no.hal.fx.adapter.AdapterListView;
import no.hal.fx.adapter.ChildrenAdapter;
import no.hal.fx.adapter.CompositeLabelAdapter;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindableView;
import no.hal.fx.bindings.BindingSource;
import no.hal.fx.bindings.FxBindings;
import no.kantega.llm.ModelConfiguration;
import no.kantega.llm.ModelManager;

public abstract class ModelsViewController<T> implements BindableView {

    private Class<T> modelClass;

    protected ModelsViewController(Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    @Inject
    ModelManager modelManager;

    @Inject
    Instance<LabelAdapter<?>> labelAdapters;
    private LabelAdapter<T> labelAdapter;

    private List<BindingSource<?>> bindingSources;

    @Override
    public List<BindingSource<?>> getBindingSources() {
        return this.bindingSources;
    }

    private ObservableList<T> models;
    private FilteredList<T> filteredModels;

    private Predicate<T> modelFilter;
    private Predicate<String> labelFilter;

    protected void setModelFilter(Predicate<T> filter) {
        this.modelFilter = filter;
        updateModelPredicate();
    }
    protected void setLabelFilter(String filter) {
        this.labelFilter = label -> label.contains(filter);
        updateModelPredicate();
    }

    private void updateModelPredicate() {
        filteredModels.setPredicate(model -> 
            (modelFilter == null || modelFilter.test(model)) &&
            (labelFilter == null || labelFilter.test(labelAdapter.getText(model)))
        );
    }

    protected void initialize(ListView<T> modelsListView) {
        // setup model list view
        this.models = modelsListView.getItems();
        this.filteredModels = new FilteredList<>(this.models);
        modelsListView.setItems(this.filteredModels);
        this.labelAdapter = CompositeLabelAdapter.of(this.labelAdapters);
        AdapterListView.adapt(modelsListView, this.labelAdapter, ChildrenAdapter.forChildren(this.models));

        // add existing models and listen for new ones
        modelManager.forEachModel(modelClass, models::add);
        modelManager.addListener(new ModelManager.Listener() {
            @Override
            public void modelAdded(ModelConfiguration<?> modelConfiguration, Object model) {
                if (modelClass.isInstance(model)) {
                    Platform.runLater(() -> models.add(modelClass.cast(model)));
                }
            }
            @Override
            public void modelRemoved(ModelConfiguration<?> modelConfiguration, Object model) {
                Platform.runLater(() -> models.remove(model));
            }
        });

        this.bindingSources = List.of(
            new BindingSource<T>(modelsListView, modelClass, FxBindings.selectedItemProperty(modelsListView, modelClass))
        );
    }
}
