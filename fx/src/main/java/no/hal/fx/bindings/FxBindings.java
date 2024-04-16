package no.hal.fx.bindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.SelectionModel;

public class FxBindings {
    
    public static <T> void addAll(Collection<? super T> col, Iterator<T> items) {
        while (items.hasNext()) {
            col.add(items.next());
        }
    }
    public static <T> void addAll(Collection<? super T> col, Iterable<T> items) {
        addAll(col, items.iterator());
    }

    public static <T> void setAll(Collection<? super T> col, Iterator<T> items) {
        col.clear();
        addAll(col, items);
    }
    public static <T> void setAll(Collection<? super T> col, Iterable<T> items) {
        setAll(col, items.iterator());
    }

    public static <T> List<T> listOf(Iterator<T> items) {
        List<T> list = new ArrayList<>();
        setAll(list, items);
        return list;
    }
    public static <T> List<T> listOf(Iterable<T> items) {
        return listOf(items.iterator());
    }

    public static <T> Function<Object, T> whenInstanceof(Class<T> clazz) {
        return item -> clazz.isInstance(item) ? (T) item : null;
    }
    public static <T> ObservableValue<T> whenInstanceof(ObservableValue<? super T> value, Class<T> clazz) {
        return value.map(whenInstanceof(clazz));
    }

    public static <T> ObservableValue<T> selectedItemProperty(SelectionModel<?> selectionModel, Class<T> clazz) {
        return selectionModel.selectedItemProperty().map(FxBindings.whenInstanceof(clazz));
    }
}
