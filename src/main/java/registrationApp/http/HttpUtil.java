package registrationApp.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpUtil {

    public static Map<String, String> readFormUrlEncoded(HttpExchange ex) throws IOException {
        int contentLength = 0;
        String len = ex.getRequestHeaders().getFirst("Content-Length");
        if (len != null) {
            try { contentLength = Integer.parseInt(len); } catch (NumberFormatException ignored) {}
        }
        String body = readBody(ex, contentLength);
        return parseUrlEncoded(body);
    }

    private static String readBody(HttpExchange ex, int contentLength) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            byte[] buf = is.readAllBytes();
            return new String(buf, StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseUrlEncoded(String body) {
        Map<String, String> map = new LinkedHashMap<>();
        if (body == null || body.isEmpty()) return map;
        String[] pairs = body.split("&");
        for (String p : pairs) {
            int i = p.indexOf('=');
            String k = i >= 0 ? p.substring(0, i) : p;
            String v = i >= 0 ? p.substring(i + 1) : "";
            k = URLDecoder.decode(k, StandardCharsets.UTF_8);
            v = URLDecoder.decode(v, StandardCharsets.UTF_8);
            map.put(k, v);
        }
        return map;
    }

    public static void sendHtml(HttpExchange ex, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    public static void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().set("Location", location);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }
}
