package no.kantega.llm.docparse;

import java.io.InputStream;

import org.jboss.logging.Logger;
import org.jsoup.select.Elements;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import jakarta.inject.Inject;

public class AbstractJSoupDocumentParser implements DocumentParser {
    
    @Inject
    Logger logger;

    private String contentSelector;

    public void setContentSelector(String contentSelector) {
        this.contentSelector = contentSelector;
    }

    @Override
    public Document parse(InputStream inputStream) {
        try {
            var soup = org.jsoup.Jsoup.parse(inputStream, "UTF-8", "");
            var content = selectContent(soup);
            return Document.from(content.text());
        } catch (Exception e) {
            logger.warn(e);
            throw new RuntimeException(e);
        }
    }

    public Elements selectContent(org.jsoup.nodes.Document document) {
        var content = document.select("body");
        if (contentSelector != null && (! contentSelector.isBlank())) {
            content = content.select(contentSelector);
        }
        return content;
    }

    public String convertToText(Elements elements) {
        return elements.text();
    }
}
