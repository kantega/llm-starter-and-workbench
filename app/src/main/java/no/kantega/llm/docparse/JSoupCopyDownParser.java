package no.kantega.llm.docparse;

import java.io.InputStream;

import org.jboss.logging.Logger;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import io.github.furstenheim.CopyDown;
import jakarta.inject.Inject;

class JSoupCopyDownParser implements DocumentParser {
    
    private CopyDown html2markdownConverter = new io.github.furstenheim.CopyDown();

    @Inject
    Logger logger;

    @Override
    public Document parse(InputStream inputStream) {
        try {
            var soup = org.jsoup.Jsoup.parse(inputStream, "UTF-8", "");
            var markdown = html2markdownConverter.convert(soup.body().outerHtml());
            return Document.from(markdown);
        } catch (Exception e) {
            logger.warn(e);
            throw new RuntimeException(e);
        }
    }
}
