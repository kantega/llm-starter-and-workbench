package no.hal.fx.adapter;

public class AbstractSimpleAdapter<T> implements Adapter<Object> {

    protected final Class<T> clazz;
    protected final T t;

    protected AbstractSimpleAdapter(Class<T> clazz, T t) {
        this.clazz = clazz;
        this.t = t;
    }

    @Override
    public Class<?> forClass() {
        return clazz != null ? clazz : Object.class;
    }

    @Override
    public boolean isFor(Object o) {
        return clazz.isInstance(o) && (t == null || o == t);
    }
}
