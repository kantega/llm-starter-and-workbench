package no.kantega.llm.docparse;

import dev.langchain4j.data.document.DocumentParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

@ApplicationScoped
public class JSoupParsers {
    
    @Produces
    @Named("jsoup+text")
    public DocumentParser createJSoupDocumentParser() {
        return new JSoupDocumentParser();
    }

    @Produces
    @Named("jsoup+copydown")
    public DocumentParser createJSoupCopyDownDocumentParser() {
        return new JSoupCopyDownParser();
    }
}
