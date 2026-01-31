import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.AccountService;
import client.EntityService;
import client.KYBService;
import client.UserService;

/**
 * Shared connection and configuration for all Vertex SDK operations.
 * Reads configuration from .env file.
 */
public class VertexClient implements AutoCloseable {
    private final Connection connection;
    private final Config config;

    public VertexClient() throws Exception {
        this.config = Config.load();
        this.config.validate();

        Options.Builder builder = new Options.Builder()
            .server(config.server)
            .credentialPath(config.credsPath);
        if (config.openTls) {
            builder.opentls();
        }

        this.connection = Nats.connect(builder.build());
        System.out.println("✓ Connected to Vertex API (" + config.server + ")");
    }

    public Connection connection() { return connection; }
    public String jwt() { return config.jwt; }
    public String partnerId() { return config.partnerId; }

    public EntityService entityService() {
        return new EntityService(connection, config.jwt, List.of(config.partnerId));
    }

    public UserService userService() {
        return new UserService(connection, config.jwt, List.of(config.partnerId));
    }

    public AccountService accountService() {
        return new AccountService(connection, config.jwt, List.of(config.partnerId));
    }

    public KYBService kybService() {
        return new KYBService(connection, config.jwt, List.of(config.partnerId));
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    public static class Config {
        public String server;
        public String credsPath;
        public String jwt;
        public String partnerId;
        public boolean openTls;

        static Config load() {
            Map<String, String> env = loadEnvFile();
            Config cfg = new Config();
            cfg.server = env.getOrDefault("NATS_SERVER", "wss://hermes.sava.africa:443");
            cfg.credsPath = env.get("NATS_CREDS");
            cfg.jwt = env.get("JWT");
            cfg.partnerId = env.get("PARTNER_ID");
            cfg.openTls = "true".equalsIgnoreCase(env.get("OPENTLS"));
            return cfg;
        }

        void validate() {
            if (credsPath == null || credsPath.isBlank())
                throw new IllegalStateException("NATS_CREDS not set in .env");
            if (jwt == null || jwt.isBlank())
                throw new IllegalStateException("JWT not set in .env");
            if (partnerId == null || partnerId.isBlank())
                throw new IllegalStateException("PARTNER_ID not set in .env");
        }

        private static Map<String, String> loadEnvFile() {
            Map<String, String> env = new HashMap<>();
            File envFile = new File(".env");
            if (!envFile.exists()) {
                throw new IllegalStateException(".env file not found. Copy .env.example to .env and fill in your values.");
            }
            try (BufferedReader br = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq > 0) {
                        env.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read .env file", e);
            }
            return env;
        }
    }
}
