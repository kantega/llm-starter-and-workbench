package no.kantega.llm.fx;

import java.io.InputStream;

import org.jboss.logging.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

// https://www.basicsbehind.com/2014/08/06/extract-text-webpage/

@Dependent
@Named("boilerpipe")
public class BoilerpipeDocumentParser implements DocumentParser {

    @Inject
    Logger logger;

    @Override
    public Document parse(InputStream inputStream) {
            String content;
            try {
                final TextDocument doc = new BoilerpipeSAXInput(new InputSource(inputStream)).getTextDocument();
                content = CommonExtractors.ARTICLE_EXTRACTOR.getText(doc);
            } catch (BoilerpipeProcessingException | SAXException ex) {
                logger.warn(ex);
                throw new RuntimeException(ex);
            }
            return Document.from(content);
    }
}
