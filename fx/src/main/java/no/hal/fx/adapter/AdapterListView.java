package no.hal.fx.adapter;

import java.util.function.Consumer;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import no.hal.fx.util.FxUpdater;

public abstract class AdapterListView extends ListView<Object> {
    
    private Consumer<Object> modelSetter;
    
    public AdapterListView(LabelAdapter labelAdapter, ChildrenAdapter childrenAdapter) {
        modelSetter = adapt(this, labelAdapter, childrenAdapter);
    }

    public static Consumer<Object> adapt(ListView<Object> listView, LabelAdapter labelAdapter, ChildrenAdapter childrenAdapter) {
        return adapt(listView, listView.getItems(), labelAdapter, childrenAdapter);
    }

    private static Consumer<Object> adapt(ListView<Object> listView, ObservableList<Object> items, LabelAdapter labelAdapter, ChildrenAdapter childrenAdapter) {
        listView.setCellFactory(lv -> new LabelAdapterListCell<>(new LabelAdapterListCellHelper<>(labelAdapter)));
        return model -> {
            var children = childrenAdapter.getChildren(model);
            FxUpdater.update(items::setAll, children);
        };
    }

    public void setModel(Object model) {
        modelSetter.accept(model);
    }
}
