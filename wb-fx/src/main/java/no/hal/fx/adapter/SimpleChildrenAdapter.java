package no.hal.fx.adapter;

import java.util.List;
import java.util.function.Function;

public class SimpleChildrenAdapter<T, CT> extends AbstractFunctionalAdapter<T, List<CT>> implements ChildrenAdapter<T, CT> {

    public SimpleChildrenAdapter(Class<T> clazz, T t, Function<T, List<CT>> childrenFun) {
        super(clazz, t, childrenFun);
    }

    @Override
    public List<CT> getChildren(Object o) {
        return apply(o);
    }
}
