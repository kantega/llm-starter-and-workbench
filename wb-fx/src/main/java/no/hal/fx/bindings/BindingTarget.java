package no.hal.fx.bindings;

import javafx.beans.property.Property;
import javafx.scene.Node;

public record BindingTarget<T>(Node targetNode, Class<T> targetClass, Property<T> targetProperty) {
}
