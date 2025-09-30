package registrationApp.security;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager {

    public static final class Session {
        public long userId;
        public String name;
        public String email;
        public long expiresAt;
    }

    private static final SecureRandom RNG = new SecureRandom();
    private static final Map<String, Session> store = new ConcurrentHashMap<>();
    private static final long TTL_MILLIS = 30 * 60 * 1000; // 30 мин.

    private static String newId() {
        byte[] b = new byte[16];
        RNG.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    public static String create(long userId, String name, String email) {
        String sid = newId();
        Session s = new Session();
        s.userId = userId;
        s.name = name;
        s.email = email;
        s.expiresAt = System.currentTimeMillis() + TTL_MILLIS;
        store.put(sid, s);
        return sid;
    }

    public static Session get(String sid) {
        if (sid == null) return null;
        Session s = store.get(sid);
        if (s == null) return null;
        if (s.expiresAt < System.currentTimeMillis()) { store.remove(sid); return null; }
        return s;
    }

    public static void destroy(String sid) {
        if (sid != null) store.remove(sid);
    }
}
