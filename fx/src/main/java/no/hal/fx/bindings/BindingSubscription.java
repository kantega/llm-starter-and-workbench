package no.hal.fx.bindings;

import javafx.util.Subscription;

public record BindingSubscription(BindingSource<?> source, BindingTarget<?> target, Subscription subscription) {    
}
