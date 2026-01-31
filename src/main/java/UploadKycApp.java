import java.io.File;
import org.slf4j.LoggerFactory;

/**
 * Step 2: Upload KYC documents to NATS Object Store.
 *
 * Usage:  ./gradlew run -PmainClass=UploadKycApp
 * Reads:  user_id from state.json (created by Step 1)
 * Next:   ./gradlew run -PmainClass=SubmitKycApp
 */
public class UploadKycApp {
    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println("══════════════════════════════════════════");
        System.out.println("  STEP 2: Upload KYC Documents");
        System.out.println("══════════════════════════════════════════");
        System.out.println();

        State state = State.load();
        String userId = state.require("user_id");

        File idFront = new File("tmp/id-front.txt");
        File idBack  = new File("tmp/id-back.txt");
        File proof   = new File("tmp/proof.txt");

        System.out.println("  User ID: " + userId);
        System.out.println("  Documents:");
        System.out.println("    id-front:  " + (idFront.exists() ? "✓ found" : "✗ not found"));
        System.out.println("    id-back:   " + (idBack.exists()  ? "✓ found" : "– skipped"));
        System.out.println("    proof:     " + (proof.exists()   ? "✓ found" : "✗ not found"));
        System.out.println();

        if (!idFront.exists() && !proof.exists()) {
            System.err.println("  ✗ No documents found. Place at least tmp/id-front.txt or tmp/proof.txt");
            return;
        }

        try (VertexClient client = new VertexClient()) {
            KycDocumentUploader uploader = new KycDocumentUploader(
                client.connection(),
                LoggerFactory.getLogger(KycDocumentUploader.class)
            );

            KycDocumentUploader.KycPayload payload = new KycDocumentUploader.KycPayload() {
                public String userId() { return userId; }
                public File frontViewOfTheIdCard() { return idFront.exists() ? idFront : null; }
                public File backViewOfTheIdCard() { return idBack.exists() ? idBack : null; }
                public File proofOfResidenceDocument() { return proof.exists() ? proof : null; }
            };

            System.out.println("  → Creating KYC dropbox...");
            KycDocumentUploader.UploadResult result = uploader.uploadDocuments(payload);

            // Save to state
            state.set("bucket", result.bucketName())
                 .set("id_document_key", result.idDocument())
                 .set("id_back_key", result.idBack())
                 .set("proof_key", result.proofOfResidence())
                 .save();

            System.out.println();
            System.out.println("  ✓ Bucket:       " + result.bucketName());
            System.out.println("  ✓ ID front key: " + result.idDocument());
            System.out.println("  ✓ ID back key:  " + result.idBack());
            System.out.println("  ✓ Proof key:    " + result.proofOfResidence());
            System.out.println();
            System.out.println("──────────────────────────────────────────");
            System.out.println("  Next: ./gradlew run -PmainClass=SubmitKycApp");
            System.out.println("──────────────────────────────────────────");
            System.out.println();
        }
    }
}
