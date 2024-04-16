package no.hal.fx.adapter;

import java.util.Collection;
import java.util.Iterator;

import javafx.scene.image.Image;
import no.hal.fx.bindings.FxBindings;

public class CompositeLabelAdapter extends CompositeAdapter<LabelAdapter> implements LabelAdapter {

    private CompositeLabelAdapter(Collection<LabelAdapter> labelAdapters) {
        super(labelAdapters);
    }
    private CompositeLabelAdapter(LabelAdapter... labelAdapters) {
        super(labelAdapters);
    }
    
    public static CompositeLabelAdapter of(Iterable<LabelAdapter> labelAdapters) {
        return new CompositeLabelAdapter(FxBindings.listOf(labelAdapters));
    }
    public static CompositeLabelAdapter of(LabelAdapter... labelAdapters) {
        return new CompositeLabelAdapter(labelAdapters);
    }
    public static CompositeLabelAdapter of(Iterator<LabelAdapter> labelAdapters) {
        return new CompositeLabelAdapter(FxBindings.listOf(labelAdapters));
    }

    @Override
    public String getText(Object o) {
        return getFirst(o, adapter -> adapter.getText(o)).orElse(o.toString());
    }

    @Override
    public Image getImage(Object o) {
        for (var labelAdapter : adapters) {
            if (labelAdapter.isFor(o)) {
                var image = labelAdapter.getImage(o);
                if (image != null) {
                    return image;
                }
            }
        }
        return null;
    }
}
