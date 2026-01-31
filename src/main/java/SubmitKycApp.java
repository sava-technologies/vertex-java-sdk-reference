import client.UserService;
import java.util.concurrent.TimeUnit;

/**
 * Step 3: Submit uploaded KYC document keys to the Vertex API.
 *
 * Usage:  ./gradlew run -PmainClass=SubmitKycApp
 * Reads:  user_id, id_document_key, proof_key from state.json (created by Steps 1 & 2)
 */
public class SubmitKycApp {
    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println("══════════════════════════════════════════");
        System.out.println("  STEP 3: Submit KYC");
        System.out.println("══════════════════════════════════════════");
        System.out.println();

        State state = State.load();
        String userId        = state.require("user_id");
        String idDocumentKey = state.require("id_document_key");
        String proofKey      = state.require("proof_key");

        System.out.println("  User ID:     " + userId);
        System.out.println("  ID document: " + idDocumentKey);
        System.out.println("  Proof:       " + proofKey);
        System.out.println();

        try (VertexClient client = new VertexClient()) {
            UserService userService = client.userService();

            System.out.println("  → Submitting KYC...");

            UserService.UploadKYCDocumentsRequest req =
                new UserService.UploadKYCDocumentsRequest(userId, idDocumentKey, proofKey);

            UserService.UploadKYCDocumentsResponse resp =
                userService.upload_kyc_documents(req).get(30, TimeUnit.SECONDS);

            // Save to state
            state.set("kyc_submitted", "true")
                 .set("kyc_result", resp.message())
                 .save();

            System.out.println();
            System.out.println("  ✓ KYC submitted: " + resp.success());
            System.out.println("  ✓ Message:       " + resp.message());
            System.out.println();
            System.out.println("══════════════════════════════════════════");
            System.out.println("  KYC onboarding complete.");
            System.out.println("══════════════════════════════════════════");
            System.out.println();
        }
    }
}
