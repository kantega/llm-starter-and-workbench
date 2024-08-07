package no.kantega.llm.prompts;

import java.util.List;

public interface PromptTemplatesProvider {
    public SimplePromptTemplate from(String text);
    public List<SimplePromptTemplate> getPromptTemplates();
}
