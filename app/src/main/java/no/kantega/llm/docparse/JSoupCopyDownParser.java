package no.kantega.llm.docparse;

import org.jsoup.select.Elements;

import io.github.furstenheim.CopyDown;
import jakarta.enterprise.context.Dependent;

@Dependent
class JSoupCopyDownParser extends AbstractJSoupDocumentParser {
    
    private CopyDown html2markdownConverter = new io.github.furstenheim.CopyDown();

    @Override
    public String convertToText(Elements elements) {
        return html2markdownConverter.convert(elements.outerHtml());
    }
}
