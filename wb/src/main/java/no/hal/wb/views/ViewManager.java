package no.hal.wb.views;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.panemu.tiwulfx.control.dock.DetachableTab;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import no.hal.fx.util.ActionProgressHelper;
import no.hal.wb.storedstate.Configurable;
import no.hal.wb.storedstate.StoredStateManager;
import no.hal.wb.views.ViewModel.ContainerItem;
import no.hal.wb.views.ViewModel.Item;
import no.hal.wb.views.markdown.MarkdownViewController;
import no.hal.wb.views.markdown.MarkdownViewProvider;
import no.hal.wb.views.markdown.PathResolver;

/*
 * Based on https://github.com/panemu/tiwulfx-dock
 * 
 * Also consider
 * - https://github.com/alexbodogit/AnchorFX
 * - https://github.com/RobertBColton/DockFX
 */

@ApplicationScoped
public class ViewManager implements Configurable, PathResolver {
    
    @Inject
    BeanManager beanManager;

    @Inject
    Instance<ViewProvider> viewProviders;

    @Inject
    ObjectMapper objectMapper;

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

    private ViewModel viewModel;

    public ViewModel getViewModel() {
        return viewModel;
    }    

    private Pane viewModelRoot;
    private DetachableTabPane detachableTabPane;

    public void initialize(Pane viewModelContainer, ContainerItem<?> containerItem) {
        this.viewModelRoot = viewModelContainer;
        this.viewModel = ViewModel.of(viewModelContainer, containerItem);
        ViewModel.ContainerItem<DetachableTabPane> tabPaneContainer = viewModel.findContainerItem(viewModel.getRootItem(), ViewModel.ContainerType.TabPaneContainer.class);
        this.detachableTabPane = tabPaneContainer.itemNode();
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
            public Instance createView(JsonNode configuration) {
                return new Instance(null, new Pane(), configuration);
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

    @Inject
    Logger logger;

    ViewInfo addViewUsingPlaceholder(String id, JsonNode configuration) {
        try {
            System.out.println("addViewUsingPlaceholder(...):" + objectMapper.writeValueAsString(configuration));
        } catch (JsonProcessingException e) {
        }
        ViewProvider viewProvider = findViewProvider(id).orElseThrow(() -> new IllegalArgumentException("No view provider for " + id));
        if (isInstanceId(id) && viewModel.containsView(id)) {
            throw new IllegalArgumentException("instanceId " + id + " already in use");
        }
        var n = viewModel.viewCount(viewProvider.viewProviderId());
        String viewTitle = viewProvider.getViewInfo().viewTitle() + " #" + (n + 1);
        var instanceId = (isInstanceId(id) ? id : viewProvider.getViewInfo().viewProviderId() + "#" + n);

        ViewProvider placeholderViewProvider = createPlaceholderViewProvider(instanceId, viewTitle);
        var placeholderView = addView(placeholderViewProvider, instanceId, viewTitle, configuration);
        Platform.runLater(() -> {
            new ActionProgressHelper().performAction(placeholderView.viewItem(), () -> {
                return addView(viewProvider, instanceId, viewTitle, configuration);
            }, viewInfo -> {
                removeView(placeholderView);
            }, ex -> {
                logger.error("Error creating " + id + " view: " + ex, ex);
                removeView(placeholderView);
            });
        });
        return placeholderView;
    }

    private void fireViewEvent(ViewEvent event) {
        beanManager.getEvent().fire(event);
    }

    private ViewInfo addView(ViewProvider viewProvider, String instanceId, String viewTitle, JsonNode configuration) {
        ViewProvider.Instance instance = viewProvider.createView(configuration);
        var tab = new DetachableTab(viewTitle, instance.viewNode());
        var viewInfo = new ViewInfo(viewProvider.getViewInfo(), instanceId, instance, tab);
        tab.setOnClosed(event -> removeView(viewInfo));
        List<MenuItem> contextMenuItems = new ArrayList<>();
        createDuplicateMenuItem(viewProvider, viewInfo, instance.configuration()).ifPresent(contextMenuItems::add);
        createInfoMenuItem(viewProvider, viewInfo).ifPresent(contextMenuItems::add);
        if (! contextMenuItems.isEmpty()) {
            tab.setContextMenu(new ContextMenu(contextMenuItems.toArray(new MenuItem[contextMenuItems.size()])));
        }
        viewModel.registerView(viewInfo);
        Platform.runLater(() -> {
            detachableTabPane.getTabs().add(tab);
            detachableTabPane.getSelectionModel().selectLast();
            fireViewEvent(new ViewEvent.Added(viewInfo));
        });
        return viewInfo;
    }

    ViewInfo addView(String id, JsonNode configuration) {
        ViewProvider viewProvider = findViewProvider(id).orElseThrow(() -> new IllegalArgumentException("No view provider for " + id));
        if (isInstanceId(id) && viewModel.containsView(id)) {
            throw new IllegalArgumentException("instanceId " + id + " already in use");
        }
        var n = viewModel.viewCount(viewProvider.viewProviderId());
        String viewTitle = viewProvider.getViewInfo().viewTitle() + " #" + (n + 1);
        var instanceId = (isInstanceId(id) ? id : viewProvider.getViewInfo().viewProviderId() + "#" + n);
        return addView(viewProvider, instanceId, viewTitle, configuration);
    }
        
    void removeView(ViewInfo viewInfo) {
        findViewProvider(viewInfo.info().viewProviderId())
            .ifPresent(viewProvider -> {
                viewModel.unregisterView(viewInfo);
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
        for (var viewInfo : viewModel) {
            if (viewInfo.instance().controller() instanceof Configurable configurable) {
                stateStorageManager.storeStateForId(viewInfo.viewId(), "view", configurable);
            }
        };
        stateStorageManager.storeStateForId("viewModel", "viewModel", this);
    }

    private Optional<MenuItem> createDuplicateMenuItem(ViewProvider viewProvider, ViewInfo viewInfo, JsonNode configuration) {
        if (! viewProvider.supportsDuplicate()) {
            return Optional.empty();
        }
        var menuItem = new MenuItem("Duplicate");
        menuItem.setOnAction(event -> {
            createView(viewInfo.info().viewProviderId(), configuration);
        });
        return Optional.of(menuItem);
    }

    // PathResolver

    @Override
    public URI resolvePath(URI path) {
        var viewProvider = findViewProvider(path.getScheme()).orElse(null);
        if (viewProvider == null) {
            return path;
        }
        try {
            var url = viewProvider.getClass().getResource("/markdown" + path.getPath());
            return url != null ? url.toURI() : null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private Optional<MenuItem> createInfoMenuItem(ViewProvider viewProvider, ViewInfo viewInfo) {
        var markdownResource = viewProvider.viewProviderId() + ":" + "/" + viewProvider.viewProviderId() + ".md";
        if (resolvePath(URI.create(markdownResource)) == null) {
            return Optional.empty();
        }
        var configuration = MarkdownViewController.configuration(null, markdownResource.toString());
        var menuItem = new MenuItem("Info");
        menuItem.setOnAction(event -> {
            var markdownViewInfo = createView(MarkdownViewProvider.VIEW_ID, configuration);
            if (markdownViewInfo.instance().controller() instanceof MarkdownViewController markdownViewController) {
                markdownViewController.setPathResolver(this);
                // markdownViewController.setHostServices(hostServices);
            }
        });
        return Optional.of(menuItem);
    }

    public ViewInfo createView(String viewId, JsonNode configuration) {
        return addView(viewId, configuration);
    }

    // public ViewInfo createView(Object source) {
    //     String id = switch (source) {
    //         case Styleable stylable -> stylable.getId();
    //         default -> String.valueOf(source);
    //     };
    //     int pos = id.indexOf("__");
    //     if (pos >= 0) {
    //         id = id.substring(0, pos + 2);
    //     }
    //     id = id.replace("_", ".");
    //     return createView(id, null);
    // }

    public List<ViewInfo> createInitialViews() {
        return stateStorageManager.getStoredStateForType("view").entrySet().stream()
            .map(entry -> createView(entry.getKey(), entry.getValue()))
            .toList();
    }

    public Map<String, List<MenuItem>> createViewCreationMenuItems(String textFormat) {
        var viewByCategory = viewProviders.stream()
            .collect(Collectors.groupingBy(viewProvider -> {
                var category = viewProvider.getViewInfo().viewCategory();
                return (category != null ? category : "");
            }));
        Map<String, List<MenuItem>> categoryMenuItems = new HashMap<>();
        for (var category : viewByCategory.keySet()) {
            categoryMenuItems.put(category, createViewCreationItems(viewByCategory.get(category), textFormat));
        }
        return categoryMenuItems;
    }

    public List<MenuItem> createViewCreationItems(List<ViewProvider> viewProviders, String textFormat) {
        return viewProviders.stream()
            .map(viewProvider -> {
                var menuItem = new MenuItem(textFormat.formatted(viewProvider.getViewInfo().viewTitle()));
                var providerId = viewProvider.getViewInfo().viewProviderId();
                menuItem.setOnAction(event -> {
                    addViewUsingPlaceholder(providerId, null);
                });
                return menuItem;
            })
            .toList();
    }

    // implemnent Configurable

    @Override
    public JsonNode getConfiguration() {
        var derivedViewModel = viewModel.deriveModel(viewModelRoot);
        System.out.println(derivedViewModel);
        return objectMapper.valueToTree(derivedViewModel);
    }

    @Override
    public void configure(JsonNode configuration) {
        try {
            var derivedViewModel = objectMapper.treeToValue(configuration, Item.class);
            System.out.println(derivedViewModel);
        } catch (Exception e) {
            logger.warn("Exception converting configuration to view model: " + e, e);
        }
    }
}
