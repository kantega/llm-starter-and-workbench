package no.hal.fx.adapter;

import java.util.Collection;
import java.util.Iterator;

import javafx.scene.image.Image;
import no.hal.fx.bindings.FxBindings;

public class CompositeLabelAdapter<T> extends CompositeAdapter<LabelAdapter<?>> implements LabelAdapter<T> {

    private CompositeLabelAdapter(Collection<LabelAdapter<?>> labelAdapters) {
        super(labelAdapters);
    }
    private CompositeLabelAdapter(LabelAdapter<?>... labelAdapters) {
        super(labelAdapters);
    }
    
    public static <T> CompositeLabelAdapter<T> of(Iterable<LabelAdapter<?>> labelAdapters) {
        return new CompositeLabelAdapter<T>(FxBindings.listOf(labelAdapters));
    }
    public static <T> CompositeLabelAdapter<T> of(LabelAdapter<?>... labelAdapters) {
        return new CompositeLabelAdapter<T>(labelAdapters);
    }
    public static <T> CompositeLabelAdapter<T> of(Iterator<LabelAdapter<?>> labelAdapters) {
        return new CompositeLabelAdapter<T>(FxBindings.listOf(labelAdapters));
    }

    @Override
    public String getText(T t) {
        for (var labelAdapter : adapters) {
            if (labelAdapter.isFor(t)) {
                String text = ((LabelAdapter) labelAdapter).getText(t);
                if (text != null) {
                    return text;
                }
            }
        }
        return null;
    }

    @Override
    public Image getImage(T t) {
        for (var labelAdapter : adapters) {
            if (labelAdapter.isFor(t)) {
                var image = ((LabelAdapter) labelAdapter).getImage(t);
                if (image != null) {
                    return image;
                }
            }
        }
        return null;
    }
}
