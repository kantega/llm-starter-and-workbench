package no.hal.fx.util;

import java.util.Map;
import java.util.function.Function;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseButton;

public class CopyToClipboardActionCreator {
    
    private static <N extends Node> ContextMenu registerContextMenu(N node, Function<N, String> contentProvider) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Copy to clipboard");
        copyItem.setOnAction(event -> {
             var content = contentProvider.apply(node);
            Clipboard.getSystemClipboard().setContent(Map.of(DataFormat.PLAIN_TEXT, content != null ? content : ""));
        });
        contextMenu.getItems().add(copyItem);
        if (node instanceof Control control) {
            control.setContextMenu(contextMenu);
        } else {
            node.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(node, mouseEvent.getSceneX(), mouseEvent.getSceneX());
                }
            });
        }
        return contextMenu;
    }

    public static <C extends Control> void setContextMenu(C control, Function<C, String> contentProvider) {
        ContextMenu contextMenu = registerContextMenu(control, contentProvider);
        control.setContextMenu(contextMenu);
    }

    public static <N extends Node> void setContextMenu(N node, Function<N, String> contentProvider) {
        ContextMenu contextMenu = registerContextMenu(node, contentProvider);
        node.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(node, mouseEvent.getSceneX(), mouseEvent.getSceneX());
            }
        });
    }
}
