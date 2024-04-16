package no.hal.fx.adapter;

import java.util.Collection;
import java.util.List;

public class CompositeChildrenAdapter extends CompositeAdapter<ChildrenAdapter> implements ChildrenAdapter {

    private CompositeChildrenAdapter(Collection<ChildrenAdapter> childrenAdapters) {
        super(childrenAdapters);
    }
    private CompositeChildrenAdapter(ChildrenAdapter... childrenAdapters) {
        super(childrenAdapters);
    }
    
    public static CompositeChildrenAdapter of(Collection<ChildrenAdapter> childrenAdapters) {
        return new CompositeChildrenAdapter(childrenAdapters);
    }
    public static CompositeChildrenAdapter of(ChildrenAdapter... childrenAdapters) {
        return new CompositeChildrenAdapter(childrenAdapters);
    }

    @Override
    public List<? extends Object> getChildren(Object o) {
        return getAll(o, adapter -> adapter.getChildren(o));
    }
}
