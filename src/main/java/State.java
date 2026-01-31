import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple shared state between SDK test steps.
 * Stored as state.json in the project root.
 */
public class State {
    private static final File STATE_FILE = new File("state.json");
    private static final ObjectMapper mapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    private final Map<String, String> data;

    private State(Map<String, String> data) {
        this.data = data;
    }

    public static State load() {
        if (!STATE_FILE.exists()) {
            return new State(new LinkedHashMap<>());
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> data = mapper.readValue(STATE_FILE, LinkedHashMap.class);
            return new State(data);
        } catch (IOException e) {
            return new State(new LinkedHashMap<>());
        }
    }

    public State set(String key, String value) {
        if (value != null) {
            data.put(key, value);
        }
        return this;
    }

    public String get(String key) {
        return data.get(key);
    }

    public String require(String key) {
        String value = data.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Missing '" + key + "' in state.json. Did you run the previous step?");
        }
        return value;
    }

    public void save() {
        try {
            mapper.writeValue(STATE_FILE, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write state.json", e);
        }
    }
}
