package no.hal.fx.adapter;

import java.util.function.Function;

public class SimpleLabelAdapter<T> extends AbstractFunctionalAdapter<T, String> implements LabelAdapter<T> {

    public SimpleLabelAdapter(Class<T> clazz, T t, Function<T, String> textFun) {
        super(clazz, t, textFun);
    }

    @Override
    public String getText(Object o) {
        return apply(o);
    }
}
