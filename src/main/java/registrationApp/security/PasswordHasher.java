package registrationApp.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public final class PasswordHasher {
    public static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    public static final int ITERATIONS = 120_000;
    public static final int KEY_LENGTH_BITS = 256;      // 32B
    public static final int SALT_LENGTH_BYTES = 16;     // 16B

    private static final SecureRandom RNG = new SecureRandom();

    public static byte[] newSalt() {
        byte[] s = new byte[SALT_LENGTH_BYTES];
        RNG.nextBytes(s);
        return s;
    }

    public static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM)
                    .generateSecret(spec)
                    .getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Password hash error", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verify(char[] password, byte[] salt, byte[] expectedHash) {
        byte[] actual = hash(password, salt);
        boolean ok = constantTimeEquals(actual, expectedHash);
        Arrays.fill(actual, (byte) 0);
        return ok;
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) diff |= (a[i] ^ b[i]);
        return diff == 0;
    }
}
