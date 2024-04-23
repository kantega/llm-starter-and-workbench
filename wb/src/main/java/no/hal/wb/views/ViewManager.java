package no.hal.wb.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.panemu.tiwulfx.control.dock.DetachableTab;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.css.Styleable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import no.hal.fx.util.ActionProgressHelper;
import no.hal.wb.storedstate.Configurable;
import no.hal.wb.storedstate.StoredStateManager;

/*
 * Based on https://github.com/panemu/tiwulfx-dock
 * 
 * Also consider
 * - https://github.com/alexbodogit/AnchorFX
 * - https://github.com/RobertBColton/DockFX
 */

@ApplicationScoped
public class ViewManager {
    
    @Inject
    BeanManager beanManager;

    @Inject
    Instance<ViewProvider> viewProviders;

    private String providerId(String id) {
        int pos = id.indexOf("#");
        return (pos > 0 ? id.substring(0, pos) : id);
    }

    private boolean isInstanceId(String id) {
        return id.indexOf("#") >= 0;
    }

    Optional<ViewProvider> findViewProvider(String id) {
        id = providerId(id);
        for (var viewProvider : viewProviders) {
            ViewProvider.Info info = viewProvider.getViewInfo();
            if (info.viewProviderId().equals(id)) {
                return Optional.of(viewProvider);
            }
        }
        return Optional.empty();
    }

    private Map<String, ViewInfo> views = new HashMap<>();

    private DetachableTabPane detachableTabPane;

    public void setDetachableTabPane(DetachableTabPane detachableTabPane) {
        this.detachableTabPane = detachableTabPane;
    }

    @Inject
    StoredStateManager stateStorageManager;

    private ViewProvider createPlaceholderViewProvider(String instanceId, String viewTitle) {
        return new ViewProvider() {
            @Override
            public Info getViewInfo() {
                return new Info(instanceId, viewTitle);
            }
            @Override
            public Instance createView() {
                return new Instance(null, new Pane());
            }
            @Override
            public boolean supportsDuplicate() {
                return false;
            }
            @Override
            public void dispose(Instance instance) {
            }
        };
    }

    ViewInfo addViewUsingPlaceholder(String id) {
        ViewProvider viewProvider = findViewProvider(id).orElseThrow(() -> new IllegalArgumentException("No view provider for " + id));
        if (isInstanceId(id) && views.containsKey(id)) {
            throw new IllegalArgumentException("instanceId " + id + " already in use");
        }
        var n = views.values().stream().filter(vi -> vi.info().equals(viewProvider.getViewInfo())).count();
        String viewTitle = viewProvider.getViewInfo().viewTitle() + " #" + (n + 1);
        var instanceId = (isInstanceId(id) ? id : viewProvider.getViewInfo().viewProviderId() + "#" + n);

        ViewProvider placeholderViewProvider = createPlaceholderViewProvider(instanceId, viewTitle);
        var placeholderView = addView(placeholderViewProvider, instanceId, viewTitle);
        Platform.runLater(() -> {
            new ActionProgressHelper().performAction(placeholderView.viewItem(), () -> {
                return addView(viewProvider, instanceId, viewTitle);
            }, viewInfo -> {
                removeView(placeholderView);
            }, ex -> {
                System.err.println("Error creating " + id + " view: " + ex);
                removeView(placeholderView);
            });
        });
        return placeholderView;
    }

    private void fireViewEvent(ViewEvent event) {
        beanManager.getEvent().fire(event);
    }

    private ViewInfo addView(ViewProvider viewProvider, String instanceId, String viewTitle) {
        ViewProvider.Instance instance = viewProvider.createView();
        var tab = new DetachableTab(viewTitle, instance.viewNode());
        var viewInfo = new ViewInfo(viewProvider.getViewInfo(), instanceId, instance, tab);
        tab.setOnClosed(event -> removeView(viewInfo));
        List<MenuItem> contextMenuItems = new ArrayList<>();
        if (viewProvider.supportsDuplicate()) {
            contextMenuItems.add(createDuplicateMenuItem(viewInfo, detachableTabPane));
        }
        if (! contextMenuItems.isEmpty()) {
            tab.setContextMenu(new ContextMenu(contextMenuItems.toArray(new MenuItem[0])));
        }
        views.put(viewInfo.viewId(), viewInfo);
        Platform.runLater(() -> {
            detachableTabPane.getTabs().add(tab);
            detachableTabPane.getSelectionModel().selectLast();
            fireViewEvent(new ViewEvent.Added(viewInfo));
        });
        return viewInfo;
    }

    ViewInfo addView(String id) {
        ViewProvider viewProvider = findViewProvider(id).orElseThrow(() -> new IllegalArgumentException("No view provider for " + id));
        if (isInstanceId(id) && views.containsKey(id)) {
            throw new IllegalArgumentException("instanceId " + id + " already in use");
        }
        var n = views.values().stream().filter(vi -> vi.info().equals(viewProvider.getViewInfo())).count();
        String viewTitle = viewProvider.getViewInfo().viewTitle() + " #" + (n + 1);
        var instanceId = (isInstanceId(id) ? id : viewProvider.getViewInfo().viewProviderId() + "#" + n);

        return addView(viewProvider, instanceId, viewTitle);
    }
        
    void removeView(ViewInfo viewInfo) {
        findViewProvider(viewInfo.info().viewProviderId())
            .ifPresent(viewProvider -> {
                views.remove(viewInfo.viewId());
                switch (viewInfo.viewItem()) {
                    case Tab tab -> detachableTabPane.getTabs().remove(tab);
                    case null, default -> {
                    }
                }
                viewProvider.dispose(viewInfo.instance());
                stateStorageManager.removeStoreStateForId(viewInfo.viewId());
                fireViewEvent(new ViewEvent.Removed(viewInfo));
            });
    }

    @PreDestroy
    void storeViewState() {
        for (var viewInfo : views.values()) {
            if (viewInfo.instance().controller() instanceof Configurable configurable) {
                stateStorageManager.storeStateForId(viewInfo.viewId(), "view", configurable);
            }
        };
    }

    private MenuItem createDuplicateMenuItem(ViewInfo viewInfo, DetachableTabPane detachableTabPane) {
        var menuItem = new MenuItem("Duplicate");
        menuItem.setOnAction(event -> {
            createView(viewInfo.info().viewProviderId(), detachableTabPane);
        });
        return menuItem;
    }

    public ViewInfo createView(String viewId) {
        return addView(viewId);
    }

    public ViewInfo createView(Object source, DetachableTabPane detachableTabPane) {
        String id = switch (source) {
            case Styleable stylable -> stylable.getId();
            default -> null;
        };
        return createView(extractId(id), detachableTabPane);
    }

    private String extractId(String id) {
        int pos = id.indexOf("__");
        if (pos >= 0) {
            id = id.substring(0, pos + 2);
        }
        return id.replace("_", ".");
    }

    public List<ViewInfo> createInitialViews() {
        return stateStorageManager.getStoredStateIdsForType("view").stream()
            .map(instanceId -> createView(instanceId))
            .toList();
    }

    public List<MenuItem> createViewCreationItems(String textFormat) {
        return viewProviders.stream()
            .map(viewProvider -> {
                var menuItem = new MenuItem(textFormat.formatted(viewProvider.getViewInfo().viewTitle()));
                var providerId = viewProvider.getViewInfo().viewProviderId();
                menuItem.setOnAction(event -> addViewUsingPlaceholder(providerId));
                return menuItem;
            })
            .toList();
    }
}
