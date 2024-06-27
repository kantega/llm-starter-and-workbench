package no.kantega.llm.docparse;

import dev.langchain4j.data.document.DocumentParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

@ApplicationScoped
public class DocumentParsers {
    
    @Inject
    Provider<PlainTextDocumentParser> plainTextDocumentParserProvider;
    
    @Produces
    @Named("plain-text")
    public DocumentParser createPlainTextDocumentParser() {
        return plainTextDocumentParserProvider.get();
    }

    @Inject
    Provider<JSoupDocumentParser> jSoupDocumentParserProvider;
    
    @Produces
    @Named("jsoup+text")
    public DocumentParser createJSoupDocumentParser() {
        return jSoupDocumentParserProvider.get();
    }

    @Inject
    Provider<JSoupCopyDownParser> jSoupCopyDownParserProvider;

    @Produces
    @Named("jsoup+copydown")
    public DocumentParser createJSoupCopyDownDocumentParser() {
        return jSoupCopyDownParserProvider.get();
    }
}
