package no.hal.fx.adapter;

import java.util.function.Consumer;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import no.hal.fx.util.FxUpdater;

public abstract class AdapterListView<T, CT> extends ListView<CT> {
    
    private Consumer<T> modelSetter;
    
    public AdapterListView(LabelAdapter<CT> labelAdapter, ChildrenAdapter<T, CT> childrenAdapter) {
        modelSetter = adapt(this, labelAdapter, childrenAdapter);
    }

    public static <CT> void adapt(ListView<CT> listView, LabelAdapter<CT> labelAdapter) {
        listView.setCellFactory(lv -> new LabelAdapterListCell<>(new LabelAdapterListCellHelper<>(labelAdapter)));
    }

    public static <T, CT> Consumer<T> adapt(ListView<CT> listView, LabelAdapter<CT> labelAdapter, ChildrenAdapter<T, CT> childrenAdapter) {
        return adapt(listView, labelAdapter, listView.getItems(), childrenAdapter);
    }

    private static <T, CT> Consumer<T> adapt(ListView<CT> listView, LabelAdapter<CT> labelAdapter, ObservableList<CT> items, ChildrenAdapter<T, CT> childrenAdapter) {
        adapt(listView, labelAdapter);
        return model -> {
            var children = childrenAdapter.getChildren(model);
            FxUpdater.update(items::setAll, children);
        };
    }

    public void setModel(T model) {
        modelSetter.accept(model);
    }
}
