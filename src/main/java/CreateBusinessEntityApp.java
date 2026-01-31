import client.EntityService;
import java.util.concurrent.TimeUnit;

/**
 * Step B1: Create a business entity.
 *
 * Usage:  ./gradlew run -PmainClass=CreateBusinessEntityApp
 * Next:   ./gradlew run -PmainClass=UploadKybApp
 */
public class CreateBusinessEntityApp {
    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println("══════════════════════════════════════════");
        System.out.println("  STEP B1: Create Business Entity");
        System.out.println("══════════════════════════════════════════");
        System.out.println();

        TestDataGenerator gen = new TestDataGenerator();
        TestDataGenerator.BusinessData biz = gen.generateBusiness();

        System.out.println("  Generated test business:");
        System.out.println("    Company:      " + biz.companyName());
        System.out.println("    Trading as:   " + biz.tradingName());
        System.out.println("    Reg number:   " + biz.registrationNumber());
        System.out.println("    Email:        " + biz.email());
        System.out.println();

        try (VertexClient client = new VertexClient()) {
            EntityService entityService = client.entityService();

            System.out.println("  → Creating business entity...");

            EntityService.CreateEntityRequest req = new EntityService.CreateEntityRequest(
                biz.companyName(),            // name
                biz.tradingName(),            // trading_name
                null,                         // tax_number
                biz.registrationNumber(),     // registration_number
                "business",                   // entity_type
                null,                         // purpose
                biz.email(),
                biz.country(),                // country
                null,                         // first_name
                null,                         // last_name
                null,                         // phone_number
                null,                         // gender
                null,                         // dob
                null,                         // id_number
                null,                         // id_type
                null,                         // id_issue_date
                null,                         // id_issue_expiry_date
                null,                         // city
                null,                         // residency
                null,                         // title
                null,                         // permit_number
                null                          // address
            );

            EntityService.CreateEntityResponse resp =
                entityService.create(req).get(30, TimeUnit.SECONDS);

            String entityId = resp.id();

            // Save to state
            State.load()
                .set("business_entity_id", entityId)
                .set("business_name", biz.companyName())
                .set("business_email", biz.email())
                .set("registration_number", biz.registrationNumber())
                .save();

            System.out.println();
            System.out.println("  ✓ Entity ID:  " + entityId);
            System.out.println();
            System.out.println("──────────────────────────────────────────");
            System.out.println("  Next: ./gradlew run -PmainClass=UploadKybApp");
            System.out.println("──────────────────────────────────────────");
            System.out.println();
        }
    }
}
