package no.hal.fx.adapter;

import javafx.scene.control.ListCell;

public class LabelAdapterListCell<T> extends ListCell<T> {

    private final LabelAdapterListCellHelper<T> labelAdapterListCellHelper;

    public LabelAdapterListCell(LabelAdapterListCellHelper<T> labelAdapterListCellHelper) {
        this.labelAdapterListCellHelper = labelAdapterListCellHelper;
    }

    @Override
    public void updateItem(T item, boolean isEmpty) {
        super.updateItem(item, isEmpty);
        labelAdapterListCellHelper.updateItem(this, item, isEmpty);
    }
}
