package no.hal.fx.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CompositeChildrenAdapter<T, CT> extends CompositeAdapter<ChildrenAdapter<?, ?>> implements ChildrenAdapter<T, CT> {

    private CompositeChildrenAdapter(Collection<ChildrenAdapter<?, ?>> childrenAdapters) {
        super(childrenAdapters);
    }
    private CompositeChildrenAdapter(ChildrenAdapter<?, ?>... childrenAdapters) {
        super(childrenAdapters);
    }
    
    public static <T, CT> CompositeChildrenAdapter<T, CT> of(Collection<ChildrenAdapter<?, ?>> childrenAdapters) {
        return new CompositeChildrenAdapter<T, CT>(childrenAdapters);
    }
    public static <T, CT> CompositeChildrenAdapter<T, CT> of(ChildrenAdapter<?, ?>... childrenAdapters) {
        return new CompositeChildrenAdapter<T, CT>(childrenAdapters);
    }

    @Override
    public List<CT> getChildren(T t) {
        List<CT> all = null;
        for (var childrenAdapter : adapters) {
            if (childrenAdapter.isFor(t)) {
                if (all == null) {
                    all = new ArrayList<>();
                }
                var children = ((ChildrenAdapter) childrenAdapter).getChildren(t);
                if (children != null) {
                    all.addAll(children);
                }
            }
        }
        return all;
    }
}
