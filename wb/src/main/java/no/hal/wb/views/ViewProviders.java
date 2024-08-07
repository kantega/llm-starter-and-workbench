package no.hal.wb.views;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.hal.wb.views.markdown.MarkdownViewProvider;
import no.hal.wb.views.markdown.PathResolver;

@ApplicationScoped
public class ViewProviders {
    
    @Inject
    ViewManager viewManager;

    @Produces
    PathResolver pathResolver() {
        return viewManager::resolvePath;
    }

    @Inject
    Instance<MarkdownViewProvider> markdownViewProvider;

    @Produces
    ViewProvider markdownView() {
        return markdownViewProvider.get();
    }
}
