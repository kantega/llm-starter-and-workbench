package no.hal.fx.adapter;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import no.hal.fx.util.FxUpdater;

public class AdapterTreeView extends TreeView<Object> {
    
    private Consumer<Object> modelSetter;
    
    public AdapterTreeView(LabelAdapter labelAdapter, ChildrenAdapter childrenAdapter) {
        modelSetter = adapt(this, labelAdapter, childrenAdapter);
    }
    
    public static Consumer<Object> adapt(TreeView<Object> treeView, LabelAdapter labelAdapter, ChildrenAdapter childrenAdapter) {
        treeView.setShowRoot(false);
        treeView.setCellFactory(tv -> new LabelAdapterTreeCell<Object>(new LabelAdapterListCellHelper<>(labelAdapter)));
        return model -> {
            treeView.setRoot(new TreeItem<Object>(model));
            List<? extends Object> children = childrenAdapter.getChildren(model);
            treeView.getRoot().getChildren().addAll(children.stream().map(child -> createExpandableTreeItem(child, childrenAdapter)).toList());
        };
    }

    public void setModel(Object model) {
        modelSetter.accept(model);
    }

    private static TreeItem<Object> createExpandableTreeItem(Object item, ChildrenAdapter childrenAdapter) {
        TreeItem<Object> treeItem = new TreeItem<Object>(item) {
            public boolean isLeaf() {
                return false;
            };
        };
        treeItem.addEventHandler(TreeItem.branchExpandedEvent(), event -> expandedChanged(event.getSource(), childrenAdapter));
        return treeItem;
    }

    private static void asyncAddTreeItems(TreeItem<Object> parentItem, Supplier<List<? extends Object>> childrenSupplier, Function<Object, TreeItem<Object>> treeItemCreator) {
        parentItem.getChildren().add(new TreeItem<Object>("..."));
        Thread.ofVirtual().start(() -> {
            var children = childrenSupplier.get();
            var treeItems = children.stream().map(treeItemCreator).toList();
            FxUpdater.update(parentItem.getChildren()::setAll, treeItems);
        });
    }

    private static void expandedChanged(TreeItem<Object> source, ChildrenAdapter childrenAdapter) {
        if (source.isExpanded() && source.getChildren().isEmpty()) {
            asyncAddTreeItems(source, () -> childrenAdapter.getChildren(source.getValue()), item -> createExpandableTreeItem(item, childrenAdapter));
        }
    }
}
