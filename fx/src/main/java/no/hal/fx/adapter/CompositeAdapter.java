package no.hal.fx.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import no.hal.fx.bindings.FxBindings;

public class CompositeAdapter<A extends Acceptor<?>> {

    protected List<A> adapters;

    protected CompositeAdapter(Iterator<A> adapters) {
        setAdapters(adapters);
    }
    protected CompositeAdapter(Iterable<A> adapters) {
        setAdapters(adapters);
    }
    protected CompositeAdapter(A... adapters) {
        this(List.of(adapters));
    }
    protected void setAdapters(Iterable<A> adapters) {
        setAdapters(adapters.iterator());
    }
    protected void setAdapters(Iterator<A> adapters) {
        this.adapters = FxBindings.listOf(adapters);
    }

    public boolean isFor(Object o) {
        return adapters.stream().anyMatch(adapter -> adapter.isFor(o));
    }

    protected <T> Optional<T> getFirst(Object o, Function<A, T> relation) {
        for (var adapter : adapters) {
            if (adapter.isFor(o)) {
                var related = relation.apply(adapter);
                if (related != null) {
                    return Optional.of(related);
                }
            }
        }
        return Optional.empty();
    }

    protected <T> List<T> getAll(Object o, Function<A, Collection<T>> relation) {
        List<T> all = null;
        for (var adapter : adapters) {
            if (adapter.isFor(o)) {
                if (all == null) {
                    all = new ArrayList<>();
                }
                var related = relation.apply(adapter);
                if (related != null) {
                    all.addAll(related);
                }
            }
        }
        return all;
    }
}
