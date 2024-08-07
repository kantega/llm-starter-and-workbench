package no.kantega.llm.prompts;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimplePromptTemplateTest {
    
    @Test
    public void testDefaultSimplePromptTemplate() {
        SimplePromptTemplate template = new SimplePromptTemplate("Hello, {{name}}!");
        Assertions.assertEquals("Hello, World!", template.render(Map.of("name", "World")));
    }

    @Test
    public void testSimplePromptTemplate() {
        SimplePromptTemplate template = new SimplePromptTemplate("{greeting}, {name}!", "{%s}");
        Assertions.assertEquals("Hello, World!", template.render(Map.of("greeting", "Hello", "name", "World")));
    }

    @Test
    public void testSimplePromptTemplateWierdVariable() {
        SimplePromptTemplate template = new SimplePromptTemplate("{var_name1}", "{%s}");
        Assertions.assertEquals("Hello", template.render(Map.of("var_name1", "Hello")));    
    }
}
