package no.kantega.prompthubdeepsetai;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import no.kantega.llm.prompts.PromptTemplatesProvider;
import no.kantega.llm.prompts.SimplePromptTemplate;

@ApplicationScoped
public class PrompthubTemplatesProvider implements PromptTemplatesProvider {

    @RestClient
    PrompthubApi api;

    private List<SimplePromptTemplate> prompthubTemplates = null;

    @Override
    public List<SimplePromptTemplate> getPromptTemplates() {
        if (prompthubTemplates == null) {
            prompthubTemplates = api.getPromptTemplates().stream()
                .map(this::toSimplePromptTemplate)
                .toList();
        }
        return prompthubTemplates;
    }

    @Override
    public SimplePromptTemplate from(String text) {
        return new SimplePromptTemplate(text, "{%s}");
    }

    private SimplePromptTemplate toSimplePromptTemplate(PrompthubApi.PrompthubTemplate template) {
        var simpleTemplate = from(template.text());
        simpleTemplate.setName(template.name());
        simpleTemplate.setDescription(template.description());
        return simpleTemplate;
    }
}
