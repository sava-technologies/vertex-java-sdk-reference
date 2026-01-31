import client.EntityService;
import client.UserService;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Step 1: Create a personal entity and user with random SA test data.
 *
 * Usage:  ./gradlew run -PmainClass=CreateEntityApp
 * Next:   ./gradlew run -PmainClass=UploadKycApp
 */
public class CreateEntityApp {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println("══════════════════════════════════════════");
        System.out.println("  STEP 1: Create Personal Entity");
        System.out.println("══════════════════════════════════════════");
        System.out.println();

        TestDataGenerator gen = new TestDataGenerator();
        TestDataGenerator.PersonData p = gen.generate();

        System.out.println("  Generated test person:");
        System.out.println("    Name:     " + p.fullName());
        System.out.println("    Gender:   " + p.gender());
        System.out.println("    DOB:      " + p.dob());
        System.out.println("    ID:       " + p.idNumber());
        System.out.println("    Phone:    " + p.phone());
        System.out.println("    Email:    " + p.email());
        System.out.println("    City:     " + p.city() + ", " + p.province());
        System.out.println("    Address:  " + p.streetAddress());
        System.out.println();

        try (VertexClient client = new VertexClient()) {
            EntityService entityService = client.entityService();
            UserService userService = client.userService();

            System.out.println("  → Creating personal entity...");

            EntityService.CreateEntityRequest entityReq = new EntityService.CreateEntityRequest(
                p.fullName(),                       // name
                p.fullName(),                       // trading_name
                null,                               // tax_number
                null,                               // registration_number
                "personal",                         // entity_type
                "personal",                         // purpose
                p.email(),
                p.country(),                        // country
                p.firstName(),
                p.lastName(),
                p.phone(),
                p.gender(),
                p.dob().atStartOfDay().format(ISO), // dob
                p.idNumber(),
                p.idType(),                         // id_type
                p.idIssue().atStartOfDay().format(ISO),
                p.idExpiry().atStartOfDay().format(ISO),
                p.city(),
                p.residency(),
                p.title(),
                null,                               // permit_number
                new EntityService.Address(
                    p.streetAddress(), "", p.city(), p.province(), p.postalCode()
                )
            );

            EntityService.CreateEntityResponse resp =
                entityService.create(entityReq).get(30, TimeUnit.SECONDS);

            String entityId = resp.id();
            String userId = resp.user_id();

            if (userId == null || userId.isBlank() || !resp.user_ready()) {
                System.out.println("  → Creating user...");
                UserService.CreateUserRequest userReq = new UserService.CreateUserRequest(
                    p.firstName(), p.lastName(), p.email(), entityId,
                    p.gender(), p.dob().atStartOfDay().format(ISO), p.country(),
                    p.city(), p.residency(), p.idNumber(), p.idType(),
                    p.idIssue().atStartOfDay().format(ISO),
                    p.idExpiry().atStartOfDay().format(ISO),
                    p.phone(), p.title(), false, null
                );
                UserService.CreateUserResponse userResp =
                    userService.create(userReq).get(30, TimeUnit.SECONDS);
                userId = userResp.userId();
            }

            // Save to state
            State.load()
                .set("entity_id", entityId)
                .set("user_id", userId)
                .set("email", p.email())
                .set("name", p.fullName())
                .save();

            System.out.println();
            System.out.println("  ✓ Entity ID:  " + entityId);
            System.out.println("  ✓ User ID:    " + userId);
            System.out.println();
            System.out.println("──────────────────────────────────────────");
            System.out.println("  Next: ./gradlew run -PmainClass=UploadKycApp");
            System.out.println("──────────────────────────────────────────");
            System.out.println();
        }
    }
}
