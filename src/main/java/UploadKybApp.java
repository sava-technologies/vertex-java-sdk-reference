import client.KYBService;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

/**
 * Step B2: Fetch KYB directors, then upload business + director documents.
 *
 * Usage:  ./gradlew run -PmainClass=UploadKybApp
 * Reads:  business_entity_id from state.json (created by Step B1)
 * Next:   ./gradlew run -PmainClass=SubmitKybApp
 *
 * Place test documents in the tmp/ directory:
 *   tmp/business_registration.pdf  (or .txt for testing)
 *   tmp/proof_of_address.pdf
 *   tmp/tax_verification.pdf
 *   tmp/director_id_front.pdf      (reused per director for testing)
 *   tmp/director_id_back.pdf
 *   tmp/director_selfie.pdf
 *   tmp/director_proof_of_address.pdf
 */
public class UploadKybApp {
    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println("══════════════════════════════════════════");
        System.out.println("  STEP B2: Upload KYB Documents");
        System.out.println("══════════════════════════════════════════");
        System.out.println();

        State state = State.load();
        String entityId = state.require("business_entity_id");

        System.out.println("  Entity ID: " + entityId);
        System.out.println();

        try (VertexClient client = new VertexClient()) {
            KYBService kybService = client.kybService();

            // Step 1: Get KYB to find directors
            System.out.println("  → Fetching KYB info (directors from Transunion)...");
            KYBService.GetKYBResponse kyb =
                kybService.get(new KYBService.GetKYBRequest(entityId))
                    .get(30, TimeUnit.SECONDS);

            System.out.println("    KYB state: " + kyb.state());

            List<KYBService.Director> directors = kyb.directors();
            if (directors == null || directors.isEmpty()) {
                System.out.println("  ⚠ No directors found. KYB may not be ready yet.");
                System.out.println("    This can happen if the registration number hasn't been");
                System.out.println("    processed by Transunion yet. Try again shortly.");

                state.set("kyb_state", kyb.state() != null ? kyb.state() : "no_directors")
                     .save();
                return;
            }

            System.out.println("    Directors found: " + directors.size());
            for (KYBService.Director d : directors) {
                System.out.println("      • " + d.name() + " (ID: " + d.id() + ")");
            }
            System.out.println();

            // Step 2: Locate test documents
            // Business docs
            File bizReg   = findDoc("tmp/business_registration");
            File bizProof = findDoc("tmp/proof_of_address");
            File bizTax   = findDoc("tmp/tax_verification");

            // Director docs (same test files reused per director for testing)
            File dirFront = findDoc("tmp/director_id_front");
            File dirBack  = findDoc("tmp/director_id_back");
            File dirSelfie = findDoc("tmp/director_selfie");
            File dirProof = findDoc("tmp/director_proof_of_address");

            System.out.println("  Business documents:");
            System.out.println("    registration:   " + status(bizReg));
            System.out.println("    proof_of_addr:  " + status(bizProof));
            System.out.println("    tax_verif:      " + status(bizTax));
            System.out.println("  Director documents:");
            System.out.println("    id_front:       " + status(dirFront));
            System.out.println("    id_back:        " + status(dirBack));
            System.out.println("    selfie:         " + status(dirSelfie));
            System.out.println("    proof_of_addr:  " + status(dirProof));
            System.out.println();

            // Build business doc map
            Map<String, File> businessDocs = new LinkedHashMap<>();
            if (bizReg != null)   businessDocs.put("business_registration", bizReg);
            if (bizProof != null) businessDocs.put("proof_of_address", bizProof);
            if (bizTax != null)   businessDocs.put("tax_verification", bizTax);

            if (businessDocs.isEmpty()) {
                System.err.println("  ✗ No business documents found in tmp/");
                System.err.println("    Create at least: tmp/business_registration.txt");
                return;
            }

            // Build per-director doc map (each director gets unique uploads)
            Map<String, Map<String, File>> directorDocs = new LinkedHashMap<>();
            for (KYBService.Director d : directors) {
                Map<String, File> docs = new LinkedHashMap<>();
                if (dirFront != null)  docs.put("director_id_front", dirFront);
                if (dirBack != null)   docs.put("director_id_back", dirBack);
                if (dirSelfie != null) docs.put("director_selfie", dirSelfie);
                if (dirProof != null)  docs.put("director_proof_of_address", dirProof);

                if (!docs.isEmpty()) {
                    directorDocs.put(d.id(), docs);
                }
            }

            // Step 3: Upload
            System.out.println("  → Creating KYB dropbox and uploading...");
            KybDocumentUploader uploader = new KybDocumentUploader(
                client.connection(),
                LoggerFactory.getLogger(KybDocumentUploader.class)
            );

            KybDocumentUploader.UploadResult result =
                uploader.uploadDocuments(entityId, businessDocs, directorDocs);

            // Save to state
            state.set("kyb_bucket", result.bucketName());

            // Save business doc keys as comma-separated
            state.set("kyb_business_doc_keys",
                String.join(",", result.businessDocKeys()));

            // Save director doc keys as directorId:key1,key2;directorId2:key1,key2
            String dirKeysStr = result.directorDocKeys().entrySet().stream()
                .map(e -> e.getKey() + ":" + String.join(",", e.getValue()))
                .collect(Collectors.joining(";"));
            state.set("kyb_director_doc_keys", dirKeysStr);

            // Save director IDs for submit step
            String dirIds = directors.stream()
                .map(KYBService.Director::id)
                .collect(Collectors.joining(","));
            state.set("kyb_director_ids", dirIds);

            state.save();

            System.out.println();
            System.out.println("  ✓ Bucket: " + result.bucketName());
            System.out.println("  ✓ Business docs uploaded: " + result.businessDocKeys().size());
            for (String key : result.businessDocKeys()) {
                System.out.println("      " + key);
            }
            for (var entry : result.directorDocKeys().entrySet()) {
                System.out.println("  ✓ Director " + entry.getKey() + ": " + entry.getValue().size() + " docs");
                for (String key : entry.getValue()) {
                    System.out.println("      " + key);
                }
            }
            System.out.println();
            System.out.println("──────────────────────────────────────────");
            System.out.println("  Next: ./gradlew run -PmainClass=SubmitKybApp");
            System.out.println("──────────────────────────────────────────");
            System.out.println();
        }
    }

    /** Finds a document with common extensions (.pdf, .txt, .jpg, .png) */
    private static File findDoc(String basePath) {
        for (String ext : new String[]{".pdf", ".txt", ".jpg", ".png"}) {
            File f = new File(basePath + ext);
            if (f.exists()) return f;
        }
        return null;
    }

    private static String status(File f) {
        return f != null ? "✓ found (" + f.getName() + ")" : "– not found";
    }
}
