package registrationApp.http;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Cookies {

    public static Map<String,String> parse(String cookieHeader) {
        Map<String,String> m = new LinkedHashMap<>();
        if (cookieHeader == null || cookieHeader.isEmpty()) return m;
        String[] parts = cookieHeader.split(";");
        for (String p : parts) {
            String s = p.trim();
            int i = s.indexOf('=');
            if (i > 0) m.put(s.substring(0,i), s.substring(i+1));
        }
        return m;
    }
}
