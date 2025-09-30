package registrationApp.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import registrationApp.security.SessionManager;

import java.io.IOException;
import java.util.Map;

public class HomeHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.sendHtml(ex, 405, "<h1>405 Method Not Allowed</h1>");
            return;
        }

        String cookieHeader = ex.getRequestHeaders().getFirst("Cookie");
        Map<String, String> cookies = Cookies.parse(cookieHeader);
        String sid = cookies.get("SESSIONID");
        SessionManager.Session s = SessionManager.get(sid);

        if (s == null) {
            HttpUtil.noCache(ex);
            HttpUtil.redirectSeeOther(ex, "/login");
            return;
        }

        HttpUtil.noCache(ex);
        String html = ResourceUtil.readText("templates/home.html");
        html = html.replace("{{username}}", escape(s.name));
        HttpUtil.sendHtml(ex, 200, html);
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            out.append(switch (c) {
                case '<' -> "&lt;";
                case '>' -> "&gt;";
                case '&' -> "&amp;";
                case '"' -> "&quot;";
                case '\'' -> "&#39;";
                default -> String.valueOf(c);
            });
        }
        return out.toString();
    }
}
