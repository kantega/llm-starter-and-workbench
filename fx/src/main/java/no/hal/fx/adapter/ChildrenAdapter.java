package no.hal.fx.adapter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import no.hal.fx.bindings.FxBindings;

@FunctionalInterface
public interface ChildrenAdapter extends Adapter<Object> {
    
    @Override
    default Class<? extends Object> forClass() {
        return Object.class;
    }

    List<? extends Object> getChildren(Object o);

    public static <T, CT> ChildrenAdapter forClass(Class<T> clazz, Function<T, List<CT>> childrenFun) {
        return new SimpleChildrenAdapter<T, CT>(clazz, null, childrenFun);
    }
    public static <T, CT> ChildrenAdapter forInstance(T t, Function<T, List<CT>> childrenFun) {
        return new SimpleChildrenAdapter<T, CT>((Class<T>) t.getClass(), t, childrenFun);
    }
    public static <T, CT> ChildrenAdapter forInstance(T t, List<CT> children) {
        return forInstance(t, it -> children);
    }
    public static <CT> ChildrenAdapter forChildren(Iterator<CT> children) {
        List<CT> childList = FxBindings.listOf(children);
        return it -> childList;
    }
    public static <CT> ChildrenAdapter forChildren(Iterable<CT> children) {
        return forChildren(children.iterator());
    }
    public static <CT> ChildrenAdapter empty() {
        return forChildren(Collections.emptyList());
    }
}
