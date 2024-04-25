package no.kantega.llm.docparse;

import java.io.InputStream;

import org.jboss.logging.Logger;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import jakarta.inject.Inject;

class JSoupDocumentParser implements DocumentParser {
    
    @Inject
    Logger logger;

    @Override
    public Document parse(InputStream inputStream) {
        try {
            var soup = org.jsoup.Jsoup.parse(inputStream, "UTF-8", "");
            return Document.from(soup.body().text());
        } catch (Exception e) {
            logger.warn(e);
            throw new RuntimeException(e);
        }
    }
}
