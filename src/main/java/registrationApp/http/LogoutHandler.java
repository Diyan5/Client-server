package registrationApp.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import registrationApp.security.SessionManager;

import java.io.IOException;
import java.util.Map;

public class LogoutHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)) {
            HttpUtil.sendHtml(ex, 405, "<h1>405 Method Not Allowed</h1>");
            return;
        }

        String cookieHeader = ex.getRequestHeaders().getFirst("Cookie");
        Map<String, String> cookies = Cookies.parse(cookieHeader);
        String sid = cookies.get("SESSIONID");
        SessionManager.destroy(sid);

        ex.getResponseHeaders().add(
                "Set-Cookie",
                "SESSIONID=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax"
        );

        ex.getResponseHeaders().set("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        ex.getResponseHeaders().set("Pragma", "no-cache");
        ex.getResponseHeaders().set("Expires", "0");
        ex.getResponseHeaders().set("Vary", "Cookie");

        HttpUtil.redirect(ex, "/");
    }
}
