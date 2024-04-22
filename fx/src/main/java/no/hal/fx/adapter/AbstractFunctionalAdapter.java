package no.hal.fx.adapter;

import java.util.function.Function;

public class AbstractFunctionalAdapter<T, R> extends AbstractSimpleAdapter<T> {

    private final Function<T, R> fun;

    public AbstractFunctionalAdapter(Class<T> clazz, T t, Function<T, R> fun) {
        super(clazz, t);
        this.fun = fun;
    }

    protected R apply(Object o) {
        return isFor(o) ? fun.apply(clazz.cast(o)) : null;
    }
}
