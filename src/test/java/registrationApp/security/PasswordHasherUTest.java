package registrationApp.security;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherUTest {

    @Test
    void newSalt_hasCorrectLength_andLooksRandom() {
        byte[] s1 = PasswordHasher.newSalt();
        byte[] s2 = PasswordHasher.newSalt();
        assertEquals(PasswordHasher.SALT_LENGTH_BYTES, s1.length);
        assertEquals(PasswordHasher.SALT_LENGTH_BYTES, s2.length);
        // изключително малко вероятно да са еднакви – добър индикатор за случайност
        assertFalse(Arrays.equals(s1, s2));
    }

    @Test
    void hash_isDeterministic_forSameInput() {
        char[] pw = "Aa123456".toCharArray();
        byte[] salt = PasswordHasher.newSalt();

        byte[] h1 = PasswordHasher.hash(pw, salt);
        byte[] h2 = PasswordHasher.hash(pw, salt);

        assertArrayEquals(h1, h2);
        assertEquals(PasswordHasher.KEY_LENGTH_BITS / 8, h1.length);
    }

    @Test
    void hash_changes_whenSaltChanges_orPasswordChanges() {
        char[] pw1 = "Aa123456".toCharArray();
        char[] pw2 = "Aa1234567".toCharArray();
        byte[] s1 = PasswordHasher.newSalt();
        byte[] s2 = PasswordHasher.newSalt();

        byte[] h_pw_change = PasswordHasher.hash(pw2, s1);
        byte[] h_salt_change = PasswordHasher.hash(pw1, s2);
        byte[] base = PasswordHasher.hash(pw1, s1);

        assertFalse(Arrays.equals(base, h_pw_change));
        assertFalse(Arrays.equals(base, h_salt_change));
    }

    @Test
    void verify_returnsTrue_forCorrectPassword_andFalseOtherwise() {
        char[] pw = "Aa123456".toCharArray();
        byte[] salt = PasswordHasher.newSalt();
        byte[] hash = PasswordHasher.hash(pw, salt);

        assertTrue(PasswordHasher.verify("Aa123456".toCharArray(), salt, hash));
        assertFalse(PasswordHasher.verify("wrongPass".toCharArray(),  salt, hash));

        // различна дължина на очаквания хеш -> constantTimeEquals връща false
        byte[] wrongLen = Arrays.copyOf(hash, hash.length - 1);
        assertFalse(PasswordHasher.verify("Aa123456".toCharArray(), salt, wrongLen));
    }
}
