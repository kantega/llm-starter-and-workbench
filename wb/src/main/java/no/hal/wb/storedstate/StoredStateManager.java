package no.hal.wb.storedstate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import no.hal.wb.views.ViewEvent;

@ApplicationScoped
public class StoredStateManager {
    
    private final static String USER_HOME = System.getProperty("user.home", System.getenv("HOME"));

    @ConfigProperty(name = "wb.storedstate.foldername", defaultValue = "storedstate")
    private String folderName;

    private Path getStoredStateFolderPath() {
        return Path.of(USER_HOME, ".wb", folderName);
    }

    @Inject
    ObjectMapper objectMapper;

    private Path pathForId(String id) {
        return Path.of(id.replaceAll("[^\\w\\d]", "_") + ".json");
    }

    private JsonNode loadRawStoredState(Path path) {
        Path storedStatePath = getStoredStateFolderPath().resolve(path);
        try {
            return objectMapper.readTree(storedStatePath.toFile());
        } catch (IOException ioex) {
            return null;
        }
    }

    private String typeOf(JsonNode json) {
        return json.get("type").asText();
    }

    private String idOf(JsonNode json) {
        return json.get("id").asText();
    }

    private JsonNode storedStateOf(JsonNode json) {
        return json.get("storedstate");
    }

    private Map<String, JsonNode> defaultStoredState = null;

    private Map<String, JsonNode> getDefaultStoredState() {
        if (this.defaultStoredState == null) {
            this.defaultStoredState = new HashMap<>();
            var url = getClass().getResource("/defaultStoredState.json");
            if (url != null) {
                try {
                    var json = objectMapper.readTree(url.openStream());
                    if (json instanceof ArrayNode arrayNode) {
                        for (var child : arrayNode) {
                            this.defaultStoredState.put(idOf(child), child);
                        }
                    }
                } catch (IOException e) {
                }
            }
        }
        return defaultStoredState;
    }

    private Map<String, JsonNode> getDefaultStoredState(String type) {
        Map<String, JsonNode> storedState = getDefaultStoredState();
        return storedState.entrySet().stream()
            .filter(entry -> type == null || type.equals(typeOf(entry.getValue())))
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> storedStateOf(entry.getValue())));
    }

    private Map<String, JsonNode> getStoredStateForType(String type) {
        Map<String, JsonNode> storedState = getDefaultStoredState(type);
        var files = getStoredStateFolderPath().toFile().listFiles();
        if (files != null) {
            storedState = new HashMap<>(storedState);
            storedState.putAll(Stream.of(files).map(File::toPath).map(this::loadRawStoredState)
                .filter(json -> type.equals(typeOf(json)))
                .collect(Collectors.toMap(this::idOf, this::storedStateOf))
            );
        }
        return storedState;
    }
    public Collection<String> getStoredStateIdsForType(String type) {
        return getStoredStateForType(type).keySet();
    }

    public JsonNode getStoredStateForId(String id, String type) {
        var json = loadRawStoredState(pathForId(id));
        if (json == null) {
            json = getDefaultStoredState().get(id);
        }
        if (json == null) {
            return null;
        }
        var stateType = typeOf(json);
        if (type != null && (! type.equals(stateType))) {
            throw new IllegalArgumentException("Stored state type mismatch, wanted " + type + ", but got " + stateType);
        };
        var stateId = idOf(json);
        if (! id.equals(stateId)) {
            throw new IllegalArgumentException("Stored state id mismatch, wanted " + id + ", but got " + stateId);
        };
        return storedStateOf(json);
    }
    public JsonNode getStoredStateForId(String id) {
        return getStoredStateForId(id, null);
    }

    public void storeStateForId(String id, String type, JsonNode storedState) {
        Path storedStatePath = getStoredStateFolderPath().resolve(id + ".json");
        try {
            Files.createDirectories(storedStatePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var json = objectMapper.createObjectNode();
        json.put("type", type);
        json.put("id", id);
        json.set("storedstate", storedState);
        try {
            objectMapper.writeValue(storedStatePath.toFile(), json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void storeStateForId(String id, String type, Configurable configurable) {
        storeStateForId(id, type, configurable.getConfiguration());
    }

    public void removeStoreStateForId(String id) {
        Path storedStatePath = getStoredStateFolderPath().resolve(pathForId(id));
        try {
            Files.deleteIfExists(storedStatePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onEvent(@Observes ViewEvent.Added event) {
        var viewInfo = event.viewInfo();
        if (viewInfo.instance().controller() instanceof Configurable configurable) {
            var json = getStoredStateForId(viewInfo.viewId());
            configurable.configure(json);
        }
    }
    
    public void onEvent(@Observes ViewEvent.Removed event) {
        var viewInfo = event.viewInfo();
        if (viewInfo.instance().controller() instanceof Configurable) {
            removeStoreStateForId(event.viewInfo().viewId());
        }
    }
}
