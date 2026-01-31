import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamOptions;
import io.nats.client.ObjectStore;
import io.nats.client.ObjectStoreManagement;
import io.nats.client.ObjectStoreOptions;
import io.nats.client.api.ObjectMeta;
import io.nats.client.api.ObjectStoreConfiguration;
import io.nats.client.api.StorageType;
import io.nats.client.impl.Headers;

/**
 * Uploads KYB documents to NATS Object Store.
 *
 * Handles both business-level documents and per-director documents.
 * Bucket name: kyb_dropbox_<entity_id>
 *
 * Like KycDocumentUploader, this explicitly creates the Object Store bucket
 * because the Java NATS SDK does not auto-create buckets.
 */
public class KybDocumentUploader {
    private final Connection natsConnection;
    private final Logger log;

    public KybDocumentUploader(Connection natsConnection, Logger log) {
        this.natsConnection = natsConnection;
        this.log = log;
    }

    public record UploadResult(
        String bucketName,
        List<String> businessDocKeys,
        Map<String, List<String>> directorDocKeys
    ) {}

    /**
     * Uploads business documents and per-director documents to the KYB dropbox.
     *
     * @param entityId       The business entity ID
     * @param businessDocs   Map of doc_type → File for business-level docs
     *                       (e.g. "business_registration", "proof_of_address", "tax_verification")
     * @param directorDocs   Map of director_id → (Map of doc_type → File)
     *                       (e.g. "director_id_front", "director_id_back", "director_selfie", "director_proof_of_address")
     */
    public UploadResult uploadDocuments(
            String entityId,
            Map<String, File> businessDocs,
            Map<String, Map<String, File>> directorDocs) throws IOException {

        log.info("Uploading KYB documents for entity {}", entityId);

        String bucketName = "kyb_dropbox_" + entityId;
        long timestamp = Instant.now().getEpochSecond();

        JetStreamOptions jso = JetStreamOptions.builder()
            .requestTimeout(Duration.ofSeconds(10))
            .build();

        ObjectStore objectStore;
        try {
            objectStore = ensureObjectStore(bucketName, jso, entityId);
            log.info("KYB bucket ready: {}", objectStore.getBucketName());
        } catch (JetStreamApiException e) {
            log.error("KYB bucket setup failed: {}", e.getMessage());
            throw new IOException("Failed to create/find KYB Object Store bucket", e);
        }

        // Upload business documents
        List<String> businessKeys = new ArrayList<>();
        for (var entry : businessDocs.entrySet()) {
            String key = entry.getKey() + "_" + timestamp;
            uploadFile(objectStore, key, entry.getValue());
            businessKeys.add(key);
        }

        // Upload per-director documents
        Map<String, List<String>> directorKeys = new LinkedHashMap<>();
        for (var dirEntry : directorDocs.entrySet()) {
            String directorId = dirEntry.getKey();
            List<String> keys = new ArrayList<>();
            for (var docEntry : dirEntry.getValue().entrySet()) {
                String key = docEntry.getKey() + "_" + directorId + "_" + timestamp;
                uploadFile(objectStore, key, docEntry.getValue());
                keys.add(key);
            }
            directorKeys.put(directorId, keys);
        }

        return new UploadResult(bucketName, businessKeys, directorKeys);
    }

    private void uploadFile(ObjectStore objectStore, String key, File file) throws IOException {
        Headers headers = new Headers()
            .add("original_filename", file.getName());

        ObjectMeta fileMeta = ObjectMeta.builder(key)
            .headers(headers)
            .build();

        try (FileInputStream fis = new FileInputStream(file)) {
            log.info("Uploading: {}", key);
            objectStore.put(fileMeta, fis);
        } catch (JetStreamApiException | NoSuchAlgorithmException e) {
            throw new IOException("Failed to upload KYB document: " + key, e);
        }
    }

    private ObjectStore ensureObjectStore(String bucketName, JetStreamOptions jso, String entityId)
            throws IOException, JetStreamApiException {
        ObjectStoreOptions oso = ObjectStoreOptions.builder(jso).build();
        ObjectStoreManagement osm = natsConnection.objectStoreManagement(oso);

        try {
            osm.getStatus(bucketName);
        } catch (JetStreamApiException missingOrOther) {
            ObjectStoreConfiguration osc = ObjectStoreConfiguration.builder(bucketName)
                .description("KYB document dropbox for entity " + entityId)
                .maxBucketSize(100L * 1024 * 1024)
                .storageType(StorageType.File)
                .replicas(1)
                .ttl(Duration.ofDays(90))
                .build();

            try {
                osm.create(osc);
            } catch (JetStreamApiException createErr) {
                osm.getStatus(bucketName);
            }
        }

        return natsConnection.objectStore(bucketName, oso);
    }
}
