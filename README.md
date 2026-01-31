# Nexus BaaS - Java SDK Reference

A working Java reference project for connecting to and interacting with the Nexus BaaS (Banking-as-a-Service) API. This project demonstrates the full onboarding lifecycle for both personal and business entities, including KYC/KYB document upload and payment transfers.

Built and tested against the staging environment.

## What This Solves

If you're integrating with the Nexus API in Java, this repo answers:

- **How do I connect?** WebSocket connection to NATS with credentials and TLS (`VertexClient.java`)
- **How do I upload KYC documents?** The Java NATS SDK does **not** auto-create Object Store buckets (unlike TypeScript/C#). `KycDocumentUploader.java` handles bucket creation explicitly before uploading
- **How do I upload KYB documents?** Same bucket creation issue, plus per-director document handling with correct naming conventions (`KybDocumentUploader.java`)
- **What jnats version should I use?** `2.20.6`. Version 2.20.3 has a broken WebSocket implementation (tagged "DO NOT USE" on GitHub). Versions 2.21.0+ use `$JS.API.DIRECT.GET` for Object Store metadata which partner accounts don't have permission for

## Prerequisites

- **Java 21+** (`brew install openjdk@21`)
- **Credentials**: A `.env` file with your NATS credentials, JWT, and partner ID

## Setup

1. Clone this repo

2. Create a `.env` file in this directory:

```
JWT=<your_jwt_token>
NATS_CREDS=<path_to_your_.creds_file>
PARTNER_ID=<your_partner_uuid>
NATS_SERVER=wss://hermes.sava.africa:443
OPENTLS=true
```

3. For KYC testing, place documents in `tmp/`:
```
tmp/id-front.txt    (or .pdf, .jpg, .png)
tmp/id-back.txt     (optional)
tmp/proof.txt
```

4. For KYB testing, place documents in `tmp/`:
```
tmp/business_registration.txt
tmp/proof_of_address.txt
tmp/tax_verification.txt
tmp/director_id_front.txt
tmp/director_id_back.txt
tmp/director_selfie.txt
tmp/director_proof_of_address.txt
```

## Workflows

### Personal Entity Onboarding (KYC)

```bash
# Step 1: Create a personal entity with random SA test data (valid ID numbers, real names)
./gradlew run -PmainClass=CreateEntityApp

# Step 2: Upload KYC documents to NATS Object Store
./gradlew run -PmainClass=UploadKycApp

# Step 3: Submit KYC for verification
./gradlew run -PmainClass=SubmitKycApp
```

### Business Entity Onboarding (KYB)

```bash
# Step B1: Create a business entity (requires real CIPC registration number)
./gradlew run -PmainClass=CreateBusinessEntityApp

# Step B2: Fetch directors from Transunion, upload business + director docs
./gradlew run -PmainClass=UploadKybApp

# Step B3: Submit KYB for verification
./gradlew run -PmainClass=SubmitKybApp
```

### Payment Transfer (EFT/RTC)

```bash
# Uses entity from Step 1, fetches accounts, sends transfer
./gradlew run -PmainClass=TransferApp

# Or with custom args: amount_cents account_number branch_code name reference
./gradlew run -PmainClass=TransferApp --args="1000 1234567890 250655 'Test Recipient' 'Payment ref'"
```

## How It Works

Each step saves its output (entity IDs, user IDs, document keys) to `state.json`, so subsequent steps pick up where the last one left off. No manual ID copying between steps.

## Project Structure

```
src/main/java/
  VertexClient.java            # Connection setup, .env config, service factories
  State.java                   # Shared state between steps (state.json)
  TestDataGenerator.java       # Generates realistic SA test data (valid ID numbers with Luhn check)

  # Personal entity flow
  CreateEntityApp.java         # Step 1: Create personal entity + user
  UploadKycApp.java            # Step 2: Upload KYC documents
  SubmitKycApp.java            # Step 3: Submit KYC

  # Business entity flow
  CreateBusinessEntityApp.java # Step B1: Create business entity
  UploadKybApp.java            # Step B2: Fetch directors, upload business + director docs
  SubmitKybApp.java            # Step B3: Submit KYB

  # Payments
  TransferApp.java             # EFT/RTC payment transfer

  # Document uploaders
  KycDocumentUploader.java     # Object Store bucket creation + KYC upload
  KybDocumentUploader.java     # Object Store bucket creation + KYB upload

  client/                      # Generated API client layer
    EntityService.java         # Entity CRUD (4 methods)
    UserService.java           # User management (4 methods)
    AccountService.java        # Accounts + transfers (7 methods)
    KYBService.java            # KYB verification (5 methods)
    CardService.java           # Card lifecycle (9 methods)
    ServiceException.java      # NATS service error handling
```

## Key Integration Notes

### Connection

```java
// VertexClient handles everything: .env loading, WebSocket, TLS
try (VertexClient client = new VertexClient()) {
    EntityService entityService = client.entityService();
    // ... use the service
}
```

The connection requires:
- `wss://` with explicit port `:443` (jnats defaults to 4222 without it)
- `.opentls()` for staging (incomplete certificate chain in Java's trust store)
- `.credentialPath()` pointing to your `.creds` file

### KYC/KYB Document Upload (Java-Specific)

The Java NATS SDK does **not** auto-create Object Store buckets. If you call `connection.objectStore("bucket_name")` on a bucket that doesn't exist, it throws an exception. You must create the bucket first:

```java
ObjectStoreManagement osm = connection.objectStoreManagement(options);
ObjectStoreConfiguration config = ObjectStoreConfiguration.builder(bucketName)
    .storageType(StorageType.File)
    .replicas(1)
    .ttl(Duration.ofDays(90))
    .build();
osm.create(config);

// Now you can use the bucket
ObjectStore os = connection.objectStore(bucketName, options);
os.put(fileMeta, inputStream);
```

Both `KycDocumentUploader.java` and `KybDocumentUploader.java` handle this with an `ensureObjectStore()` method that checks for existence first, creates if missing, and handles race conditions.

### KYB Director Documents

When creating a business entity, the API fetches directors from Transunion via the registration number. Each director must receive **unique document uploads** (compliance requirement). The naming convention:

```
Business docs:  business_registration_<timestamp>
                proof_of_address_<timestamp>
                tax_verification_<timestamp>

Director docs:  director_id_front_<director_id>_<timestamp>
                director_id_back_<director_id>_<timestamp>
                director_selfie_<director_id>_<timestamp>
                director_proof_of_address_<director_id>_<timestamp>
```

### jnats Version

Use **`2.20.6`** specifically.

| Version | Issue |
|---------|-------|
| 2.20.3 | Broken WebSocket (`404 Not Found`). Tagged "DO NOT USE" on GitHub |
| 2.20.6 | Working WebSocket + compatible Object Store API |
| 2.21.0+ | Uses `$JS.API.DIRECT.GET` for Object Store metadata. Partner accounts don't have this permission, causing `Permissions Violation` errors |

## Dependencies

```groovy
dependencies {
    implementation 'io.nats:jnats:2.20.6'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.0'
    implementation 'org.slf4j:slf4j-simple:2.0.12'
}
```
