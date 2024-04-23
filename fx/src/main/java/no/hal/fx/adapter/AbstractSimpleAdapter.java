package no.hal.fx.adapter;

public class AbstractSimpleAdapter<T> {

    protected final Class<T> clazz;
    protected final T t;

    protected AbstractSimpleAdapter(Class<T> clazz, T t) {
        this.clazz = clazz;
        this.t = t;
    }

    public boolean isFor(Object o) {
        return (clazz == null || clazz.isInstance(o)) && (t == null || o == t);
    }
}
