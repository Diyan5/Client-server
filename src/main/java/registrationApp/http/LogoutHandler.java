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
        if (sid != null) SessionManager.destroy(sid);

        // kill SESSIONID (HttpOnly)
        ex.getResponseHeaders().add("Set-Cookie",
                "SESSIONID=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax");

        // kill фронтенд флага за логнат
        ex.getResponseHeaders().add("Set-Cookie",
                "LOGGED_IN=; Max-Age=0; Path=/; SameSite=Lax");

        // кратък флаг за „току-що излязъл“ (ползва се от JS guard по желание)
        ex.getResponseHeaders().add("Set-Cookie",
                "JUST_LOGGED_OUT=1; Max-Age=10; Path=/; SameSite=Lax");

        HttpUtil.noCache(ex);
        HttpUtil.redirectSeeOther(ex, "/login?lo=1");
    }


}
