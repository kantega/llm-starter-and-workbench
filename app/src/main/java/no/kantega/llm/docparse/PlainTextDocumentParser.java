package no.kantega.llm.docparse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.Optional;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import jakarta.enterprise.context.Dependent;

@Dependent
public class PlainTextDocumentParser implements DocumentParser {

    @Override
    public Document parse(InputStream inputStream) {
        StringBuilder buffer = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(inputStream))) {
            reader.lines().forEach(line -> {
                buffer.append(line);
                buffer.append("\n");
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return postProcess(buffer).map(buf -> new Document(buf.toString())).orElse(null);
    }

    protected Optional<StringBuilder> postProcess(StringBuilder buffer) {
        return Optional.of(buffer);
    }
}
