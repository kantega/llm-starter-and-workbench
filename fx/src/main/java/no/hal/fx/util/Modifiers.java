package no.hal.fx.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class Modifiers {
    
    private static Map<String, KeyCombination.ModifierValue> modifierValues = Map.of(
        "-", KeyCombination.ModifierValue.UP,
        "+", KeyCombination.ModifierValue.DOWN,
        "~", KeyCombination.ModifierValue.ANY
    );
    private static Map<String, KeyCode> modifierKeys = Map.of(
        "SHIFT", KeyCode.SHIFT,
        "CONTROL", KeyCode.CONTROL,
        "ALT", KeyCode.ALT,
        "META", KeyCode.META
    );

    private final Map<KeyCode, KeyCombination.ModifierValue> modifierStates;

    private Modifiers(Map<KeyCode, KeyCombination.ModifierValue> modifierStates) {
        this.modifierStates = modifierStates;
    }

    public static Modifiers of(String modifiers) {
        Map<KeyCode, KeyCombination.ModifierValue> modifierStates = new HashMap<>();
        String[] tokens = modifiers.splitWithDelimiters("[\\+\\-\\~]", -1);
        KeyCombination.ModifierValue mv = null;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.isEmpty());
            else if (modifierValues.containsKey(token)) {
                if (mv != null) {
                    throw new IllegalArgumentException("Invalid modifiers format: " + modifiers);
                }
                mv = modifierValues.get(token);
            } else {
                KeyCode keyCode = modifierKeys.get(token);
                if (keyCode == null) {
                    throw new IllegalArgumentException("Unknown modifier key: " + token);
                }
                modifierStates.put(keyCode, mv != null ? mv : KeyCombination.ModifierValue.DOWN);
                mv = null;
            }
        }
        return new Modifiers(modifierStates);
    }

    private static boolean match(KeyCombination.ModifierValue modifierValue, Boolean modifier) {
        if (modifierValue == KeyCombination.ModifierValue.ANY) {
            return true;
        } else if (modifier == null) {
            return false;
        } else {
            return modifierValue == (modifier ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP);
        }
    }

    public boolean match(Boolean shiftDown, Boolean controlDown, Boolean altDown, Boolean metaDown) {
        for (var modifier : modifierStates.entrySet()) {
            Boolean modifierDown = switch (modifier.getKey()) {
                case SHIFT -> shiftDown;
                case CONTROL -> controlDown;
                case ALT -> altDown;
                case META -> metaDown;
                default -> null;
            };
            if (! match(modifier.getValue(), modifierDown)) {
                return false;
            }
        }
        return true;
    }

    public boolean match(KeyEvent event) {
        return match(event.isShiftDown(), event.isControlDown(), event.isAltDown(), event.isMetaDown());
    }

    public boolean match(MouseEvent event) {
        return match(event.isShiftDown(), event.isControlDown(), event.isAltDown(), event.isMetaDown());
    }
}
