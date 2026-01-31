import client.AccountService;
import java.util.concurrent.TimeUnit;

/**
 * Payment Transfer: Send an EFT or RTC payment.
 *
 * Usage:  ./gradlew run -PmainClass=TransferApp
 * Reads:  entity_id from state.json to look up accounts
 *
 * Transfer details are configured below or passed as args:
 *   ./gradlew run -PmainClass=TransferApp --args="<amount_cents> <account_number> <branch_code> <name> <ref>"
 */
public class TransferApp {
    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println("══════════════════════════════════════════");
        System.out.println("  Payment Transfer (EFT/RTC)");
        System.out.println("══════════════════════════════════════════");
        System.out.println();

        State state = State.load();
        String entityId = state.require("entity_id");

        try (VertexClient client = new VertexClient()) {
            AccountService accountService = client.accountService();

            // Step 1: Get accounts for entity
            System.out.println("  → Fetching accounts for entity: " + entityId);
            AccountService.GetAccountsByEntityResponse accounts =
                accountService.get_by_entity(
                    new AccountService.GetAccountsByEntityRequest(entityId)
                ).get(30, TimeUnit.SECONDS);

            if (accounts.accounts() == null || accounts.accounts().isEmpty()) {
                System.out.println("  ✗ No accounts found. Create an account first.");
                return;
            }

            // Use first account
            String accountId = accounts.accounts().get(0).account_id();
            System.out.println("  ✓ Using account: " + accountId);

            // Step 2: Get account details (balance)
            AccountService.GetAccountDetailsResponse details =
                accountService.get_details(
                    new AccountService.GetAccountDetailsRequest(accountId)
                ).get(30, TimeUnit.SECONDS);

            System.out.println("    Account number: " + details.account_number());
            System.out.println("    Balance:        " + formatCents(details.balance()));
            System.out.println("    Available:      " + formatCents(details.available_balance()));
            System.out.println();

            // Step 3: Transfer details (from args or defaults)
            int amount            = argInt(args, 0, 1000);         // R10.00
            String recipientAcct  = arg(args, 1, "1234567890");
            String branchCode     = arg(args, 2, "250655");
            String recipientName  = arg(args, 3, "Test Recipient");
            String ref            = arg(args, 4, "SDK Test Payment");
            boolean rtc           = argBool(args, 5, true);        // RTC by default
            String clientTxId     = java.util.UUID.randomUUID().toString();

            System.out.println("  → Initiating " + (rtc ? "RTC (instant)" : "EFT (batch)") + " transfer:");
            System.out.println("    Amount:    " + formatCentsInt(amount));
            System.out.println("    To:        " + recipientName);
            System.out.println("    Account:   " + recipientAcct);
            System.out.println("    Branch:    " + branchCode);
            System.out.println("    Ref:       " + ref);
            System.out.println("    Client TX: " + clientTxId);
            System.out.println();

            AccountService.TransferEFTRTCRequest transferReq = new AccountService.TransferEFTRTCRequest(
                accountId,
                clientTxId,
                amount,
                ref,
                "sdk-test-" + System.currentTimeMillis(),  // own_ref
                recipientAcct,
                branchCode,
                recipientName,
                rtc,
                null,   // notification_email
                null,   // notification_name
                null,   // beneficiary_id
                0       // beneficiary_version
            );

            System.out.println("  → Sending...");
            AccountService.TransferEFTRTCResponse resp =
                transferReq != null
                    ? accountService.transfer_eft_rtc(transferReq).get(30, TimeUnit.SECONDS)
                    : null;

            // Save to state
            state.set("last_tx_id", resp.tx_id())
                 .set("last_tx_amount", resp.amount())
                 .save();

            System.out.println();
            System.out.println("  ✓ Transaction ID: " + resp.tx_id());
            System.out.println("  ✓ Amount:         " + resp.amount());
            System.out.println();
            System.out.println("══════════════════════════════════════════");
            System.out.println("  Transfer complete.");
            System.out.println("══════════════════════════════════════════");
            System.out.println();
        }
    }

    private static String arg(String[] args, int i, String def) {
        return args.length > i ? args[i] : def;
    }

    private static int argInt(String[] args, int i, int def) {
        return args.length > i ? Integer.parseInt(args[i]) : def;
    }

    private static boolean argBool(String[] args, int i, boolean def) {
        return args.length > i ? Boolean.parseBoolean(args[i]) : def;
    }

    private static String formatCents(String centsStr) {
        try {
            long cents = Long.parseLong(centsStr);
            return String.format("R %.2f", cents / 100.0);
        } catch (Exception e) {
            return centsStr;
        }
    }

    private static String formatCentsInt(int cents) {
        return String.format("R %.2f (%d cents)", cents / 100.0, cents);
    }
}
