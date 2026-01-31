import client.KYBService;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Step B3: Submit uploaded KYB document keys to the Vertex API.
 *
 * Usage:  ./gradlew run -PmainClass=SubmitKybApp
 * Reads:  business_entity_id, kyb_business_doc_keys, kyb_director_doc_keys from state.json
 */
public class SubmitKybApp {
    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println("══════════════════════════════════════════");
        System.out.println("  STEP B3: Submit KYB Documents");
        System.out.println("══════════════════════════════════════════");
        System.out.println();

        State state = State.load();
        String entityId      = state.require("business_entity_id");
        String bizKeysStr    = state.require("kyb_business_doc_keys");
        String dirKeysStr    = state.require("kyb_director_doc_keys");

        // Parse business doc keys
        List<String> businessDocKeys = Arrays.asList(bizKeysStr.split(","));

        // Parse director doc keys: "dirId1:key1,key2;dirId2:key1,key2"
        Map<String, List<String>> directorDocs = new LinkedHashMap<>();
        for (String dirBlock : dirKeysStr.split(";")) {
            if (dirBlock.isBlank()) continue;
            String[] parts = dirBlock.split(":", 2);
            String directorId = parts[0];
            List<String> keys = Arrays.asList(parts[1].split(","));
            directorDocs.put(directorId, keys);
        }

        System.out.println("  Entity ID: " + entityId);
        System.out.println();
        System.out.println("  Business documents (" + businessDocKeys.size() + "):");
        for (String key : businessDocKeys) {
            System.out.println("    • " + key);
        }
        System.out.println();
        System.out.println("  Director documents:");
        for (var entry : directorDocs.entrySet()) {
            System.out.println("    Director " + entry.getKey() + " (" + entry.getValue().size() + " docs):");
            for (String key : entry.getValue()) {
                System.out.println("      • " + key);
            }
        }
        System.out.println();

        try (VertexClient client = new VertexClient()) {
            KYBService kybService = client.kybService();

            System.out.println("  → Submitting KYB...");

            KYBService.SubmitDocumentsRequest req = new KYBService.SubmitDocumentsRequest(
                entityId,
                businessDocKeys,
                directorDocs
            );

            kybService.submit(req).get(30, TimeUnit.SECONDS);

            state.set("kyb_submitted", "true").save();

            System.out.println();
            System.out.println("  ✓ KYB submitted for verification");
            System.out.println();
            System.out.println("══════════════════════════════════════════");
            System.out.println("  KYB onboarding complete.");
            System.out.println("══════════════════════════════════════════");
            System.out.println();
        }
    }
}
