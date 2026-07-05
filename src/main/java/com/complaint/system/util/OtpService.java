package com.complaint.system.util;

import java.security.SecureRandom;

/** Generates one-time verification codes. */
public final class OtpService {
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Minutes a generated code stays valid. */
    public static final long EXPIRY_MINUTES = 10;

    private OtpService() {}

    /** Returns a random 6-digit numeric code (zero-padded). */
    public static String generateCode() {
        int code = RANDOM.nextInt(1_000_000); // 0 .. 999999
        return String.format("%06d", code);
    }
}
