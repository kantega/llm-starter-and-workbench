package no.hal.wb.storedstate;

import com.fasterxml.jackson.databind.JsonNode;

public interface Configurable {

    /**
     * Configures with the provided configuration. 
     *
     * @param configuration
     */
    void configure(JsonNode configuration);

    /**
     * Returns the current configuration.
     *
     * @return the current configuration
     */
    default JsonNode getConfiguration() {
        return null;
    }
}
