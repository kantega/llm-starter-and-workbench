package no.hal.fx.bindings;

import java.util.List;

public interface BindingsTarget {
    List<BindingTarget<?>> getBindingTargets();
}
