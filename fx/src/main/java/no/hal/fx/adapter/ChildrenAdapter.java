package no.hal.fx.adapter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import no.hal.fx.bindings.FxBindings;

public interface ChildrenAdapter<T, CT> extends Adapter<T> {
    
    List<CT> getChildren(T t);

    public static <T, CT> ChildrenAdapter<T, CT> forClass(Class<T> clazz, Function<T, List<CT>> childrenFun) {
        return new SimpleChildrenAdapter<T, CT>(clazz, null, childrenFun);
    }
    public static <T, CT> ChildrenAdapter<T, CT> forInstance(T t, Function<T, List<CT>> childrenFun) {
        return new SimpleChildrenAdapter<T, CT>(null, t, childrenFun);
    }
    public static <T, CT> ChildrenAdapter<T, CT> forInstance(T t, List<CT> children) {
        return forInstance(t, it -> children);
    }
    public static <T, CT> ChildrenAdapter<T, CT> forChildren(Iterator<CT> children) {
        List<CT> childList = FxBindings.listOf(children);
        return new ChildrenAdapter<T,CT>() {
            @Override
            public List<CT> getChildren(T t) {
                return childList;
            }
            @Override
            public boolean isFor(Object o) {
                return true;
            }
        };
    }
    public static <CT> ChildrenAdapter<?, CT> forChildren(Iterable<CT> children) {
        return forChildren(children.iterator());
    }
    public static <CT> ChildrenAdapter<?, CT> empty() {
        return forChildren(Collections.emptyList());
    }
}
