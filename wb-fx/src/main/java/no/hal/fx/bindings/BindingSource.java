package no.hal.fx.bindings;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

public record BindingSource<T>(Node sourceNode, Class<T> sourceClass, ObservableValue<T> sourceValue) {
}
