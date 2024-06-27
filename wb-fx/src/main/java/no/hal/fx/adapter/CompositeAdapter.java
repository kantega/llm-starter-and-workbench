package no.hal.fx.adapter;

import java.util.Iterator;
import java.util.List;

import no.hal.fx.bindings.FxBindings;

public class CompositeAdapter<A extends Adapter<?>> {

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
}
