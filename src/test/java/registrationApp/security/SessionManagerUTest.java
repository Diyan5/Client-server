package registrationApp.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerUTest {

    @Test
    void create_returnsUrlSafeId_and_getReturnsStoredSession() {
        String sid = SessionManager.create(42L, "Diyan", "diyan@example.com");
        assertNotNull(sid);
        assertFalse(sid.isBlank());
        // Base64 URL без padding – 16 байта -> 22 символа
        assertEquals(22, sid.length());
        assertFalse(sid.contains("="));
        assertTrue(sid.matches("^[A-Za-z0-9_-]+$"));

        SessionManager.Session s = SessionManager.get(sid);
        assertNotNull(s);
        assertEquals(42L, s.userId);
        assertEquals("Diyan", s.name);
        assertEquals("diyan@example.com", s.email);
        assertTrue(s.expiresAt > System.currentTimeMillis());
    }

    @Test
    void get_withNullOrUnknown_returnsNull() {
        assertNull(SessionManager.get(null));
        assertNull(SessionManager.get("__missing__"));
    }

    @Test
    void expiredSession_isRemoved_andSubsequentGetIsNull() {
        String sid = SessionManager.create(1L, "A", "a@a.com");
        SessionManager.Session s = SessionManager.get(sid);
        assertNotNull(s);

        // изкуствено изтекла
        s.expiresAt = System.currentTimeMillis() - 1;

        // първо get ще я изтрие и върне null
        assertNull(SessionManager.get(sid));
        // и второ get пак е null (вече липсва от store)
        assertNull(SessionManager.get(sid));
    }

    @Test
    void destroy_removesSession() {
        String sid = SessionManager.create(7L, "X", "x@x.com");
        assertNotNull(SessionManager.get(sid));

        SessionManager.destroy(sid);
        assertNull(SessionManager.get(sid));
        // идемпотентно
        SessionManager.destroy(sid);
        assertNull(SessionManager.get(sid));
    }

    @Test
    void multipleCreates_generateDifferentIds() {
        String sid1 = SessionManager.create(1L, "A", "a@a.com");
        String sid2 = SessionManager.create(2L, "B", "b@b.com");
        assertNotEquals(sid1, sid2);
    }
}
