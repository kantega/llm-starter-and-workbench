package no.kantega.llm.prompts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.langchain4j.spi.prompt.PromptTemplateFactory.Template;

public class SimplePromptTemplate implements Template {

    private static final String DEFAULT_VARIABLE_FORMAT = "{{%s}}";

    private final String template;
    private final String variableFormat;
    private final Set<String> variables;

    public SimplePromptTemplate(String template, String variableFormat) {
        this.template = template;
        this.variableFormat = variableFormat;
        this.variables = new HashSet<>();
        Pattern variablePattern = Pattern.compile("\\Q" + variableFormat.replace("%s", "\\E(\\p{Alpha}[\\p{Alnum}_]*)\\Q") + "\\E"); 
        Matcher matcher = variablePattern.matcher(template);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
    }

    public SimplePromptTemplate(String template) {
        this(template, DEFAULT_VARIABLE_FORMAT);
    }

    public String getTemplate() {
        return template;
    }

    public Collection<String> getVariables() {
        return Collections.unmodifiableCollection(variables);
    }

    @Override
    public String render(Map<String, Object> variableValues) {
        for (String variable : this.variables) {
            if (! variableValues.containsKey(variable)) {
                throw new IllegalArgumentException("Value for the variable '" + variable + "' is missing");
            }
        }
        String result = template;
        for (Map.Entry<String, Object> entry : variableValues.entrySet()) {
            result = result.replace(variableFormat.formatted(entry.getKey()), entry.getValue().toString());
        }
        return result;
    }

    private String name = null;
    private String description = null;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
