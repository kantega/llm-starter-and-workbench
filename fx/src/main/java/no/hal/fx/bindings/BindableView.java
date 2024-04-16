package no.hal.fx.bindings;

import java.util.Collections;
import java.util.List;

public interface BindableView {
    default List<BindingTarget<?>> getBindingTargets() {
        return Collections.emptyList();
    }
    default List<BindingSource<?>> getBindingSources() {
        return Collections.emptyList();
    }
}
