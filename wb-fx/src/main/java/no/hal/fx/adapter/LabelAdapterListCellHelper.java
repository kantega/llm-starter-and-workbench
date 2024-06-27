package no.hal.fx.adapter;

import javafx.scene.control.Labeled;

public class LabelAdapterListCellHelper<T> {

    private final LabelAdapter labelAdapter;

    public LabelAdapterListCellHelper(LabelAdapter labelAdapter) {
        this.labelAdapter = labelAdapter;
    }

    public void updateItem(Labeled labeled, T item, boolean isEmpty) {
        if (isEmpty) {
            labeled.setText(null);
        } else {
            String text = labelAdapter.isFor(item) ? labelAdapter.getText(item) : null;
            labeled.setText(text != null ? text : String.valueOf(item));
        }
        labeled.setGraphic(null);
    }
}
