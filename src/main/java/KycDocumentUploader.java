import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;

import io.nats.client.Connection;
import io.nats.client.impl.Headers;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamOptions;
import io.nats.client.ObjectStore;
import io.nats.client.ObjectStoreManagement;
import io.nats.client.ObjectStoreOptions;
import io.nats.client.api.ObjectMeta;
import io.nats.client.api.ObjectStoreConfiguration;
import io.nats.client.api.StorageType;

public class KycDocumentUploader {
    private final Connection natsConnection;
    private final Logger log;

    public KycDocumentUploader(Connection natsConnection, Logger log) {
        this.natsConnection = natsConnection;
        this.log = log;
    }

    public String uploadKycDocs(KycPayload kycPayload) throws IOException {
        UploadResult result = uploadDocuments(kycPayload);
        UploadKYCDocumentsRequest uploadReq = new UploadKYCDocumentsRequest(
            result.userId(),
            result.idDocument(),
            result.proofOfResidence()
        );

        return submitKycDocs(uploadReq);
    }

    public UploadResult uploadDocuments(KycPayload kycPayload) throws IOException {
        log.info("Uploading KYC documents for user account creation");

        String userId = kycPayload.userId();
        String bucketName = "kyc_dropbox_" + userId;
        long timestamp = Instant.now().getEpochSecond();

        File frontFile = kycPayload.frontViewOfTheIdCard();
        File backFile = kycPayload.backViewOfTheIdCard();
        File proofFile = kycPayload.proofOfResidenceDocument();

        String frontName = frontFile == null ? null : "kyc_id_front_" + userId + "_" + timestamp;
        String backName = backFile == null ? null : "kyc_id_back_" + userId + "_" + timestamp;
        String proofName = proofFile == null ? null : "kyc_proof_of_residence_" + userId + "_" + timestamp;

        Map<String, File> userDocuments = new LinkedHashMap<>();
        if (frontFile != null) {
            userDocuments.put(frontName, frontFile);
        }
        if (backFile != null) {
            userDocuments.put(backName, backFile);
        }
        if (proofFile != null) {
            userDocuments.put(proofName, proofFile);
        }

        if (userDocuments.isEmpty()) {
            throw new IllegalArgumentException("At least one document is required for upload.");
        }

        JetStreamOptions jso = JetStreamOptions.builder()
            .requestTimeout(Duration.ofSeconds(10))
            .build();

        ObjectStore objectStore;
        try {
            objectStore = ensureObjectStore(bucketName, jso, userId);
            log.info("KYC bucket ready: {}", objectStore.getBucketName());
        } catch (JetStreamApiException e) {
            log.error("KYC bucket setup failed: {}", e.getMessage());
            throw new IOException("Failed to create/find Object Store bucket", e);
        }

        boolean completeUpload = userDocuments.entrySet().stream().allMatch(entry -> {
            Headers headers = new Headers()
                .add("original_filename", entry.getValue().getName());

            ObjectMeta fileMeta = ObjectMeta.builder(entry.getKey())
                .headers(headers)
                .build();

            try (FileInputStream fis = new FileInputStream(entry.getValue())) {
                log.info("Uploading the file: {}", entry.getKey());
                objectStore.put(fileMeta, fis);
                return true;
            } catch (IOException | JetStreamApiException | NoSuchAlgorithmException e) {
                log.error("Failed to upload KYC document: {}", e.getMessage());
                return false;
            }
        });

        if (!completeUpload) {
            throw new IOException("Upload failed. Something went wrong uploading the documents.");
        }

        return new UploadResult(userId, bucketName, frontName, backName, proofName);
    }

    /**
     * Ensures the KYC Object Store bucket exists before use.
     *
     * Unlike the TypeScript/C# NATS SDKs, the Java SDK does NOT auto-create
     * Object Store buckets. connection.objectStore() only binds to an existing
     * bucket. You must use objectStoreManagement().create() to provision
     * the bucket first.
     *
     * This method:
     * 1. Checks if the bucket exists via getStatus()
     * 2. If not found, creates it with the standard KYC config
     * 3. Handles race conditions where another request creates it concurrently
     * 4. Returns a bound ObjectStore handle ready for put/get operations
     */
    private ObjectStore ensureObjectStore(String bucketName, JetStreamOptions jso, String userId)
            throws IOException, JetStreamApiException {
        ObjectStoreOptions oso = ObjectStoreOptions.builder(jso).build();
        ObjectStoreManagement osm = natsConnection.objectStoreManagement(oso);

        try {
            osm.getStatus(bucketName);
        } catch (JetStreamApiException missingOrOther) {
            ObjectStoreConfiguration osc = ObjectStoreConfiguration.builder(bucketName)
                .description("KYC document dropbox for user " + userId)
                .maxBucketSize(100L * 1024 * 1024) // 100 MB
                .storageType(StorageType.File)
                .replicas(1)
                .ttl(Duration.ofDays(90))
                .build();

            try {
                osm.create(osc);
            } catch (JetStreamApiException createErr) {
                // Allow for a race where another client created the bucket.
                osm.getStatus(bucketName);
            }
        }

        return natsConnection.objectStore(bucketName, oso);
    }

    // Replace with your actual implementation.
    private String submitKycDocs(UploadKYCDocumentsRequest req) {
        return "OK";
    }

    public static final class UploadResult {
        private final String userId;
        private final String bucketName;
        private final String idDocument;
        private final String idBack;
        private final String proofOfResidence;

        public UploadResult(
            String userId,
            String bucketName,
            String idDocument,
            String idBack,
            String proofOfResidence
        ) {
            this.userId = userId;
            this.bucketName = bucketName;
            this.idDocument = idDocument;
            this.idBack = idBack;
            this.proofOfResidence = proofOfResidence;
        }

        public String userId() {
            return userId;
        }

        public String bucketName() {
            return bucketName;
        }

        public String idDocument() {
            return idDocument;
        }

        public String idBack() {
            return idBack;
        }

        public String proofOfResidence() {
            return proofOfResidence;
        }
    }

    // Placeholder interfaces for example compilation; replace with your actual types.
    public interface KycPayload {
        String userId();
        File backViewOfTheIdCard();
        File frontViewOfTheIdCard();
        File proofOfResidenceDocument();
    }

    public static final class UploadKYCDocumentsRequest {
        private final String userId;
        private final String idDocument;
        private final String proofOfResidence;

        public UploadKYCDocumentsRequest(String userId, String idDocument, String proofOfResidence) {
            this.userId = userId;
            this.idDocument = idDocument;
            this.proofOfResidence = proofOfResidence;
        }

        public String userId() {
            return userId;
        }

        public String id_document() {
            return idDocument;
        }

        public String proof_of_residence() {
            return proofOfResidence;
        }
    }
}
