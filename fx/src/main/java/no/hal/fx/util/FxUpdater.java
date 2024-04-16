package no.hal.fx.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.application.Platform;

public class FxUpdater {
    
    public static void update(Runnable updater) {
        if (Platform.isFxApplicationThread()) {
            updater.run();
        } else {
            Platform.runLater(updater);
        }
    }

    public static <T> void update(Consumer<T> updater, T arg) {
        if (Platform.isFxApplicationThread()) {
            updater.accept(arg);
        } else {
            Platform.runLater(() -> updater.accept(arg));
        }
    }

    public static <T, U> void update(BiConsumer<T, U> updater, T arg1, U arg2) {
        if (Platform.isFxApplicationThread()) {
            updater.accept(arg1, arg2);
        } else {
            Platform.runLater(() -> updater.accept(arg1, arg2));
        }
    }
}
