package no.hal.fx.adapter;

import java.util.function.Function;

import javafx.scene.image.Image;

@FunctionalInterface
public interface LabelAdapter extends Adapter<Object> {
    
    @Override
    default Class<? extends Object> forClass() {
        return Object.class;
    }

    String getText(Object o);
    
    default Image getImage(Object o) {
        return null;
    }

    public static <T> LabelAdapter forClass(Class<T> clazz, Function<T, String> textFun) {
        return new SimpleLabelAdapter<T>(clazz, null, textFun);
    }
    public static <T> LabelAdapter forInstance(T t, Function<T, String> textFun) {
        return new SimpleLabelAdapter<T>((Class<T>) t.getClass(), t, textFun);
    }
    public static <T> LabelAdapter forInstance(T t, String text) {
        return forInstance(t, it -> text);
    }
}
