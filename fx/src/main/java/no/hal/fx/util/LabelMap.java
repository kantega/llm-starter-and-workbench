package no.hal.fx.util;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public class LabelMap<L, T> implements Function<T, L> {
    
    private Map<L, T> labels = new WeakHashMap<>();

    public T getLabeled(L label, Function<L, T> creator) {
        return labels.computeIfAbsent(label, creator);
    }

    public L getLabel(T labeled) {
        for (var entry : labels.entrySet()) {
            if (entry.getValue() == labeled) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public L apply(T t) {
        return getLabel(t);
    }
}
