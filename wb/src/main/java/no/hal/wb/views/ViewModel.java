package no.hal.wb.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.panemu.tiwulfx.control.dock.DetachableTabPane;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;

public class ViewModel implements Iterable<ViewInfo> {

    public sealed interface Item<N extends Node> permits ContainerItem, ViewItem {
        N itemNode();
    }

    public record ContainerItem<N extends Node>(N itemNode, ContainerType<N> type, List<Item<?>> children) implements Item<N> {
        public ContainerItem(ContainerType<N> type, Item<?>... children) {
            this(null, type, List.of(children));
        }
    }
    public record ViewItem<N extends Node>(N itemNode, ViewInfo viewInfo) implements Item<N> {
    }

    public sealed interface ContainerType<N extends Node> {
        public record SplitPaneContainer(Orientation orientation) implements ContainerType<SplitPane> {
        }
        public record TabPaneContainer() implements ContainerType<DetachableTabPane> {
        }
    }

    private Node rootNode;
    
    private ViewModel(Pane viewModelContainer, ContainerItem<?> containerItem) {
        var viewNode = createViewStructure(containerItem);
        viewModelContainer.getChildren().add(viewNode);
        this.rootNode = viewNode;
    }

    public static ViewModel of(Pane viewModelContainer, ContainerItem<?> containerItem) {
        return new ViewModel(viewModelContainer, containerItem);
    }

    //

    private Map<String, ViewInfo> views = new HashMap<>();

    @Override
    public Iterator<ViewInfo> iterator() {
        return views.values().iterator();
    }

    public boolean containsView(String viewId) {
        return views.containsKey(viewId);
    }

    public int viewCount(String viewProviderId) {
        return (int) views.values().stream().filter(vi -> vi.info().viewProviderId().equals(viewProviderId)).count();
    }

    void registerView(ViewInfo viewInfo) {
        views.put(viewInfo.viewId(), viewInfo);
    }
    void unregisterView(ViewInfo viewInfo) {
        views.put(viewInfo.viewId(), viewInfo);
    }

    //

    public Item<?> getRootItem() {
        return deriveModel(rootNode);
    }

    private Item<? extends Node> deriveModel(Node node) {
        return switch (node) {
            case SplitPane splitPane -> new ContainerItem<SplitPane>(splitPane,
                new ContainerType.SplitPaneContainer(splitPane.getOrientation()), deriveModel(splitPane.getItems())
            );
            case DetachableTabPane tabPane -> new ContainerItem<DetachableTabPane>(tabPane,
                new ContainerType.TabPaneContainer(),
                deriveModel(tabPane.getTabs().stream().map(Tab::getContent).toList())
            );
            default -> {
                var viewInfo = getViewInfo(node);
                if (viewInfo != null) {
                    yield new ViewItem<Node>(node, viewInfo);
                }
                List<Node> children = switch (node) {
                    case Parent parent -> parent.getChildrenUnmodifiable();
                    default -> List.of();
                };
                var items = deriveModel(children);
                if (items.size() == 1) {
                    yield items.get(0);
                }
                yield items.stream().filter(item -> item instanceof ContainerItem).findFirst().orElse(
                    items.stream().findFirst().orElse(null)
                );
            }
        };
    }

    private ViewInfo getViewInfo(Node node) {
        for (var viewInfo : this) {
            if (viewInfo.instance().viewNode() == node) {
                return viewInfo;
            }
        }
        return null;
    }

    public <N extends Node> N createViewStructure(ContainerItem<N> containerItem) {
        return switch (containerItem.type()) {
            case ViewModel.ContainerType.SplitPaneContainer splitPaneContainer -> {
                var splitPane = new SplitPane();
                splitPane.setOrientation(splitPaneContainer.orientation());
                splitPane.getItems().addAll(createViewStructure(containerItem.children()));
                yield (N) splitPane;
            }
            case ViewModel.ContainerType.TabPaneContainer tabPaneContainer -> {
                yield (N) new DetachableTabPane();
            }
        };
    }
    
    public List<Node> createViewStructure(List<Item<?>> items) {
        var childNodes = new ArrayList<Node>();
        for (var childItem : items) {
            if (childItem instanceof ContainerItem<?> childContainerItem) {
                childNodes.add(createViewStructure(childContainerItem));
            }
        }
        return childNodes;
    }

    private List<Item<? extends Node>> deriveModel(List<? extends Node> children) {
        var items = new ArrayList<Item<?>>();
        for (var child : children) {
            var item = deriveModel(child);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    public <N extends Node> ContainerItem<N> findContainerItem(Class<? extends ContainerType<N>> containerType) {
        return findContainerItem(getRootItem(), containerType);
    }

    public <N extends Node> ContainerItem<N> findContainerItem(Item<?> root, Class<? extends ContainerType<N>> containerType) {
        return switch (root) {
            case ContainerItem<?> containerItem when containerType.isInstance(containerItem.type()) -> (ContainerItem<N>) containerItem;
            case ContainerItem<?> containerItem -> containerItem.children().stream()
                .map(child -> findContainerItem(child, containerType))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
            default -> null;
        };
    }
}
