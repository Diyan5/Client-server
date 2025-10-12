package registrationApp.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationUTest {

    @Test
    void testIsEmail_AllBranches() {
        assertFalse(Validation.isEmail(null));
        String tooLong = "a".repeat(250) + "@aa.com";
        assertTrue(tooLong.length() > 255);
        assertFalse(Validation.isEmail(tooLong));
        assertTrue(Validation.isEmail("user@example.com"));
        assertFalse(Validation.isEmail("user@domain"));
    }

    @Test
    void testIsName_AllBranches() {
        assertFalse(Validation.isName(null));
        assertTrue(Validation.isName("  Иван  "));
        assertFalse(Validation.isName("A"));
        assertTrue(Validation.isName("Al"));
        String hundred = "a".repeat(100);
        assertTrue(Validation.isName(hundred));
        String tooLong = "a".repeat(101);
        assertFalse(Validation.isName(tooLong));
        assertFalse(Validation.isName("Iv@n"));
    }

    @Test
    void testIsStrongPassword_AllBranches() {
        assertFalse(Validation.isStrongPassword(null));
        assertFalse(Validation.isStrongPassword("Aa1bbbb"));
        assertFalse(Validation.isStrongPassword("AAAAAAA1"));
        assertFalse(Validation.isStrongPassword("aaaaaaa1"));
        assertFalse(Validation.isStrongPassword("Aaaaaaaa"));
        assertTrue(Validation.isStrongPassword("Abcdefg1"));
    }
}
