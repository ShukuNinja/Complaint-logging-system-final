package com.complaint.system.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Loads Brevo email settings for OTP verification.
 *
 * Resolution order for each value:
 *   1. Environment variable (e.g. BREVO_API_KEY)  — highest priority
 *   2. email.properties in the working directory  — user-edited, gitignored
 *   3. email.properties on the classpath (optional)
 *   4. Built-in default (name only)
 */
public final class EmailConfig {
    private static final Logger logger = LoggerFactory.getLogger(EmailConfig.class);
    private static final Properties FILE_PROPS = new Properties();

    static {
        boolean loaded = false;
        try {
            Path local = Path.of("email.properties");
            if (Files.exists(local)) {
                try (InputStream in = new FileInputStream(local.toFile())) {
                    FILE_PROPS.load(in);
                    loaded = true;
                    logger.info("Loaded email settings from {}", local.toAbsolutePath());
                }
            }
        } catch (Exception e) {
            logger.warn("Could not read email.properties from working directory", e);
        }
        if (!loaded) {
            try (InputStream in = EmailConfig.class.getResourceAsStream("/email.properties")) {
                if (in != null) {
                    FILE_PROPS.load(in);
                    logger.info("Loaded email settings from classpath");
                }
            } catch (Exception e) {
                logger.warn("Could not read email.properties from classpath", e);
            }
        }
    }

    private EmailConfig() {}

    private static String resolve(String envKey, String propKey, String def) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String prop = FILE_PROPS.getProperty(propKey);
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        return def;
    }

    public static String getApiKey()  { return resolve("BREVO_API_KEY", "brevo.api.key", ""); }
    public static String getFrom()     { return resolve("MAIL_FROM", "mail.from", ""); }
    public static String getFromName() { return resolve("MAIL_FROM_NAME", "mail.from.name", "Complaint Management System"); }
    public static String getMode()     { return resolve("MAIL_MODE", "mail.mode", "live"); }

    /**
     * Dev/testing mode: the OTP is logged instead of emailed, so the flow can
     * be exercised without a Brevo account. Enable with mail.mode=simulate.
     */
    public static boolean isSimulate() {
        return "simulate".equalsIgnoreCase(getMode());
    }

    /** True only when an API key and a verified sender address are both present. */
    public static boolean isConfigured() {
        return !getApiKey().isBlank() && !getFrom().isBlank();
    }
}
