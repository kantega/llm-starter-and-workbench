package no.kantega.ollama.rest;

import java.time.LocalDateTime;
import java.util.List;

public record LanguageModel(String name, LocalDateTime modified_at, long size, String digest, Details details) {
    
    public String baseName() {
        return name.substring(0, name.indexOf(':'));
    }

    public String tag() {
        return name.substring(name.indexOf(':') + 1);
    }

    public record Details(String format, String family, List<String> families, String parameter_size, String quantization_level) {
    }

    public record Info(String modelfile, String parameters, String template, Details details) {
    }
}
