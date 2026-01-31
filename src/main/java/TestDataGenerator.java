import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Generates realistic South African test data for entity creation.
 * Produces valid SA ID numbers (with Luhn check digit), real SA names,
 * cities, phone numbers, and addresses.
 */
public class TestDataGenerator {
    private final Random rng;

    // South African first names (mixed cultures)
    private static final String[] MALE_NAMES = {
        "Thabo", "Sipho", "Johan", "Bongani", "Pieter",
        "Mandla", "David", "Kabelo", "Andries", "Tshepo",
        "Lwazi", "Henk", "Musa", "Francois", "Nhlanhla"
    };

    private static final String[] FEMALE_NAMES = {
        "Naledi", "Zanele", "Liesl", "Nomsa", "Annika",
        "Thandi", "Marelize", "Lerato", "Busisiwe", "Carina",
        "Palesa", "Ingrid", "Nompumelelo", "Elana", "Ayanda"
    };

    private static final String[] SURNAMES = {
        "Nkosi", "Van der Merwe", "Dlamini", "Botha", "Mokoena",
        "Pretorius", "Zulu", "Mthembu", "Du Plessis", "Mahlangu",
        "Khumalo", "Steyn", "Ndlovu", "Joubert", "Maseko"
    };

    private static final String[] TITLES_MALE = { "Mr" };
    private static final String[] TITLES_FEMALE = { "Ms", "Mrs" };

    // SA cities with province and postal code range
    private static final String[][] CITIES = {
        // { city, province, postal_code }
        { "Cape Town",     "Western Cape",    "8001" },
        { "Johannesburg",  "Gauteng",         "2001" },
        { "Pretoria",      "Gauteng",         "0002" },
        { "Durban",        "KwaZulu-Natal",   "4001" },
        { "Port Elizabeth", "Eastern Cape",    "6001" },
        { "Bloemfontein",  "Free State",      "9301" },
        { "Polokwane",     "Limpopo",         "0700" },
        { "Nelspruit",     "Mpumalanga",      "1200" },
        { "Kimberley",     "Northern Cape",   "8300" },
        { "Stellenbosch",  "Western Cape",    "7600" },
    };

    private static final String[] STREETS = {
        "Main Road", "Voortrekker Street", "Church Street", "Long Street",
        "Nelson Mandela Drive", "Jan Smuts Avenue", "Bree Street",
        "Commissioner Street", "Eloff Street", "Rivonia Road"
    };

    public TestDataGenerator() {
        this.rng = new Random();
    }

    public TestDataGenerator(long seed) {
        this.rng = new Random(seed);
    }

    public record PersonData(
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        String gender,
        String title,
        LocalDate dob,
        String idNumber,
        String idType,
        LocalDate idIssue,
        LocalDate idExpiry,
        String city,
        String province,
        String postalCode,
        String country,
        String residency,
        String streetAddress
    ) {}

    /**
     * Generates a complete random South African person for entity creation.
     */
    public PersonData generate() {
        boolean male = rng.nextBoolean();
        String firstName = pick(male ? MALE_NAMES : FEMALE_NAMES);
        String lastName  = pick(SURNAMES);
        String gender    = male ? "male" : "female";
        String title     = pick(male ? TITLES_MALE : TITLES_FEMALE);

        // DOB: age between 18 and 60
        LocalDate today = LocalDate.now();
        int age = 18 + rng.nextInt(43);
        LocalDate dob = today.minusYears(age)
                             .minusDays(rng.nextInt(365));

        String idNumber = generateSaIdNumber(dob, male);

        // ID issue/expiry
        LocalDate idIssue  = dob.plusYears(16);
        LocalDate idExpiry = idIssue.plusYears(10);

        // City
        String[] cityData = pick(CITIES);
        String city     = cityData[0];
        String province = cityData[1];
        String postal   = cityData[2];

        // Contact
        String suffix = String.format("%07d", rng.nextInt(10_000_000));
        String phone  = "+27" + pick(new String[]{"6", "7", "8"}) + suffix;
        String email  = "sdk-" + System.currentTimeMillis() + "-"
                       + rng.nextInt(1000) + "@test.nexus.example";

        // Address
        int streetNum = 1 + rng.nextInt(200);
        String street = streetNum + " " + pick(STREETS);

        return new PersonData(
            firstName, lastName, firstName + " " + lastName,
            email, phone, gender, title,
            dob, idNumber, "National", idIssue, idExpiry,
            city, province, postal, "ZAF", "ZAF", street
        );
    }

    /**
     * Generates a valid 13-digit SA ID number.
     * Format: YYMMDD SSSS C A Z
     *   YYMMDD = date of birth
     *   SSSS   = gender sequence (0000-4999 female, 5000-9999 male)
     *   C      = citizenship (0 = SA citizen)
     *   A      = filler (8)
     *   Z      = Luhn check digit
     */
    public String generateSaIdNumber(LocalDate dob, boolean male) {
        String yymmdd = dob.format(DateTimeFormatter.ofPattern("yyMMdd"));
        int seq = male ? 5000 + rng.nextInt(5000) : rng.nextInt(5000);
        String first12 = yymmdd + String.format("%04d", seq) + "0" + "8";
        int check = luhnCheckDigit(first12);
        return first12 + check;
    }

    private static int luhnCheckDigit(String digits) {
        int sum = 0;
        // Process from rightmost digit (index 11) to leftmost (index 0)
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            // Odd positions from right (0-indexed from right: positions 1,3,5...) get doubled
            if ((digits.length() - i) % 2 == 0) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
        }
        return (10 - (sum % 10)) % 10;
    }

    // South African business name parts
    private static final String[] BIZ_PREFIXES = {
        "Kganya", "Ubuntu", "Protea", "Springbok", "Baobab",
        "Kudu", "Impala", "Indalo", "Thula", "Langa"
    };

    private static final String[] BIZ_SUFFIXES = {
        "Holdings", "Trading", "Logistics", "Solutions", "Capital",
        "Ventures", "Technologies", "Construction", "Financial Services", "Mining"
    };

    public record BusinessData(
        String companyName,
        String tradingName,
        String registrationNumber,
        String email,
        String country
    ) {}

    /**
     * Generates realistic South African business entity data.
     * @param registrationNumber a valid 12-digit CIPC registration number
     */
    public BusinessData generateBusiness(String registrationNumber) {
        String prefix = pick(BIZ_PREFIXES);
        String suffix = pick(BIZ_SUFFIXES);
        String companyName = prefix + " " + suffix + " (Pty) Ltd";
        String tradingName = prefix + " " + suffix;

        String regNum = registrationNumber;

        String email = "sdk-biz-" + System.currentTimeMillis() + "-"
                     + rng.nextInt(1000) + "@test.nexus.example";

        return new BusinessData(companyName, tradingName, regNum, email, "ZAF");
    }

    @SuppressWarnings("unchecked")
    private <T> T pick(T[] arr) {
        return arr[rng.nextInt(arr.length)];
    }
}
