package no.hal.fx.adapter;

public interface Acceptor<T> {
    default boolean isFor(Object o) {
        return true;
    }
}
