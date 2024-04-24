package no.hal.fx.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.layout.Region;
import javafx.util.Callback;

public class ActionProgressHelper {

    private ProgressIndicator progressIndicator = new ProgressIndicator();

    public <T> void onAction(Button button, Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onFailure) {
        button.setOnAction(event -> performAction(button, task, onSuccess, onFailure));
    }
    public <T> void onAction(Button button, Callable<T> task, Consumer<T> onSuccess) {
        onAction(button, task, onSuccess, null);
    }

    private Runnable prepareAction(Object object, Region graphics) {
        return prepareAction(object, graphics, 1.0);
    }

    private Runnable prepareAction(Object object, Region graphics, double widthFactor) {
        if (object instanceof Labeled labeled) {
            var insets = labeled.getInsets();
            var heightSubscription = labeled.heightProperty().subscribe(height -> {
                var prefSize = labeled.getHeight() - insets.getTop() - insets.getBottom() - 1.0;
                graphics.setPrefSize(prefSize * widthFactor, prefSize);
            });
            var oldGraphics = labeled.getGraphic();
            labeled.setGraphic(graphics);
            if (labeled instanceof ButtonBase button) {
                button.setDisable(true);
            }
            return () -> {
                heightSubscription.unsubscribe();
                labeled.setGraphic(oldGraphics);
                if (labeled instanceof ButtonBase button) {
                    button.setDisable(false);
                }
            };
        } else if (object instanceof Tab tab) {
            var oldGraphics = tab.getGraphic();
            graphics.setPrefSize(20.0 * widthFactor, 20.0);
            tab.setGraphic(graphics);
            return () -> {
                tab.setGraphic(oldGraphics);
            };
        }
        return null;
    }

    public <T> void performAction(Object source, Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onFailure) {
        if (source instanceof Event event) {
            source = event.getSource();
        }
        Runnable finishRunnable = prepareAction(source, progressIndicator);
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

    public <T> void performActions(Object source, int n, Consumer<Callback<Integer, Void>> task) {
        progressIndicator.setProgress(0.0);
        Runnable finishRunnable = prepareAction(source, progressIndicator, 4.0);
        Thread.ofVirtual().start(() -> {
            try {
                task.accept(i -> {
                    if (i == null) {
                        Platform.runLater(finishRunnable);
                    } else {
                        Platform.runLater(() -> progressIndicator.setProgress(i / (double) n));
                    }
                    return null;
                });
            } catch (Exception ex) {
                finishRunnable.run();
            }
        });
    }

    public <T> void performStreamingAction(Object source, Consumer<Callback<Boolean, Void>> streamingTask) {
        Runnable finishRunnable = prepareAction(source, progressIndicator);
        try {
            streamingTask.accept(success -> {
                if (success != null && finishRunnable != null) {
                    Platform.runLater(finishRunnable);
                }
                return null;
            });
        } catch (Exception ex) {
            finishRunnable.run();
        }
    }
}
