package no.hal.fx.adapter;

import java.util.function.Function;

import javafx.scene.image.Image;

public interface LabelAdapter<T> extends Adapter<T> {
    
    String getText(T t);
    
    default Image getImage(T t) {
        return null;
    }

    public static <T> LabelAdapter<T> forClass(Class<T> clazz, Function<T, String> textFun) {
        return new SimpleLabelAdapter<T>(clazz, null, textFun);
    }
    public static <T> LabelAdapter<T> forInstance(T t, Function<T, String> textFun) {
        return new SimpleLabelAdapter<T>(null, t, textFun);
    }
    public static <T> LabelAdapter<T> forInstance(T t, String text) {
        return forInstance(t, it -> text);
    }
}
