package no.kantega.llm.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class LabelMap<L, T> implements Function<T, L> {
    
    private Map<L, T> labels = new HashMap<>();

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
