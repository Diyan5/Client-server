package registrationApp.http;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CookiesUTest {

    @Test
    void parse_nullOrEmpty_returnsEmpty() {
        assertTrue(Cookies.parse(null).isEmpty());
        assertTrue(Cookies.parse("").isEmpty());
    }

    @Test
    void parse_validMultiple_preservesOrder() {
        Map<String, String> m = Cookies.parse("a=1; b=2; c=3");
        assertEquals(3, m.size());
        assertEquals("1", m.get("a"));
        assertEquals("2", m.get("b"));
        assertEquals("3", m.get("c"));
        assertArrayEquals(new String[]{"a","b","c"}, m.keySet().toArray(new String[0]));
    }

    @Test
    void parse_ignoresSegmentsWithoutEquals() {
        Map<String, String> m = Cookies.parse("x=1; invalid; y=2");
        assertEquals(2, m.size());
        assertEquals("1", m.get("x"));
        assertEquals("2", m.get("y"));
        assertNull(m.get("invalid"));
    }
}
