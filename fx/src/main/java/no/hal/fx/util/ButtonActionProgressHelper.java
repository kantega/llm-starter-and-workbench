package no.hal.fx.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Callback;

public class ButtonActionProgressHelper {

    private ProgressIndicator progressIndicator = new ProgressIndicator();

    public <T> void onAction(Button button, Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onFailure) {
        button.setOnAction(event -> performAction(button, task, onSuccess, onFailure));
    }
    public <T> void onAction(Button button, Callable<T> task, Consumer<T> onSuccess) {
        onAction(button, task, onSuccess, null);
    }

    public <T> void performAction(Object source, Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onFailure) {
        Runnable resetGraphics = null;
        if (source instanceof Event event) {
            source = event.getSource();
        }
        if (source instanceof Labeled labeled) {
            var insets = labeled.getInsets();
            var prefSize = labeled.getHeight() - insets.getTop() - insets.getBottom();
            progressIndicator.setPrefSize(prefSize, prefSize);
            var oldGraphics = labeled.getGraphic();
            resetGraphics = () -> labeled.setGraphic(oldGraphics);
            labeled.setGraphic(progressIndicator);
        }
        Runnable finishRunnable = resetGraphics;
        Thread.ofVirtual().start(() -> {
            try {
                T result = task.call();
                Platform.runLater(() -> onSuccess.accept(result));
            } catch (Exception e) {
                if (onFailure != null) {
                    Platform.runLater(() -> onFailure.accept(e));
                }
            } finally {
                if (finishRunnable != null) {
                    Platform.runLater(finishRunnable);
                }
            }
        });
    }
    public <T> void performAction(ActionEvent event, Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onFailure) {
        performAction(event.getSource(), task, onSuccess, onFailure);
    }

    public <T> void performStreamingAction(Object source, Consumer<Callback<Boolean, Void>> streamingTask) {
        Runnable resetGraphics = null;
        if (source instanceof Labeled labeled) {
            var insets = labeled.getInsets();
            var prefSize = labeled.getHeight() - insets.getTop() - insets.getBottom();
            progressIndicator.setPrefSize(prefSize, prefSize);
            var oldGraphics = labeled.getGraphic();
            resetGraphics = () -> labeled.setGraphic(oldGraphics);
            labeled.setGraphic(progressIndicator);
        }
        Runnable finishRunnable = resetGraphics;
        streamingTask.accept(success -> {
            if (success != null && finishRunnable != null) {
                Platform.runLater(finishRunnable);
            }
            return null;
        });
    }
}
