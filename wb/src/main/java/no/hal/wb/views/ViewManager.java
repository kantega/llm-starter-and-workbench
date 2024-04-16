package no.hal.wb.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.panemu.tiwulfx.control.dock.DetachableTabPane;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import javafx.css.Styleable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import no.hal.wb.storedstate.Configurable;
import no.hal.wb.storedstate.StoredStateManager;

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

    @Inject
    StoredStateManager stateStorageManager;

    ViewInfo addView(String id, DetachableTabPane detachableTabPane) {
        ViewProvider viewProvider = findViewProvider(id).orElseThrow(() -> new IllegalArgumentException("No view provider for " + id));
        if (isInstanceId(id) && views.containsKey(id)) {
            throw new IllegalArgumentException("instanceId " + id + " already in use");
        }
        ViewProvider.Instance instance = viewProvider.createView();
        var n = views.values().stream().filter(vi -> vi.info().equals(viewProvider.getViewInfo())).count();
        String viewTitle = viewProvider.getViewInfo().viewTitle() + " #" + (n + 1);
        var tab = detachableTabPane.addTab(viewTitle, instance.viewNode());
        var instanceId = (isInstanceId(id) ? id : viewProvider.getViewInfo().viewProviderId() + "#" + n);
        var viewInfo = new ViewInfo(viewProvider.getViewInfo(), instanceId, instance, () -> detachableTabPane.getTabs().remove(tab));
        tab.setOnClosed(event -> removeView(viewInfo));
        tab.setContextMenu(new ContextMenu(
            createDuplicateMenuItem(viewInfo, detachableTabPane)
        ));
        views.put(viewInfo.viewId(), viewInfo);
        detachableTabPane.getSelectionModel().selectLast();
        CDI.current().getBeanManager().getEvent().fire(new ViewEvent.Added(viewInfo));
        return viewInfo;
    }
        
    void removeView(ViewInfo viewInfo) {
        findViewProvider(viewInfo.info().viewProviderId())
            .ifPresent(viewProvider -> {
                views.remove(viewInfo.viewId());
                viewInfo.disposer().run();
                viewProvider.dispose(viewInfo.instance());
                stateStorageManager.removeStoreStateForId(viewInfo.viewId());
                beanManager.getEvent().fire(new ViewEvent.Removed(viewInfo));
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

    public ViewInfo createView(String viewId, DetachableTabPane detachableTabPane) {
        return addView(viewId, detachableTabPane);
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

    public List<ViewInfo> createInitialViews(DetachableTabPane detachableTabPane) {
        return stateStorageManager.getStoredStateIdsForType("view").stream()
            .map(instanceId -> createView(instanceId, detachableTabPane))
            .toList();
    }
}
