package registrationApp.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import registrationApp.db.UsersDao;
import registrationApp.security.PasswordHasher;
import registrationApp.security.SessionManager;
import registrationApp.util.Validation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class MyAccountHandler implements HttpHandler {
    private final UsersDao users = new UsersDao();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod().toUpperCase();
        if ("GET".equals(method)) {
            handleGet(ex, null, successFromQuery(ex));
        } else if ("POST".equals(method)) {
            handlePost(ex);
        } else {
            HttpUtil.sendHtml(ex, 405, "<h1>405 Method Not Allowed</h1>");
        }
    }

    private String successFromQuery(HttpExchange ex) {
        String q = ex.getRequestURI().getQuery();
        return (q != null && q.contains("ok=1")) ? "Changes saved." : null;
    }

    private void handleGet(HttpExchange ex, String errorMsg, String successMsg) throws IOException {
        SessionManager.Session s = requireSessionOrRedirect(ex);
        if (s == null) return; // вече redirect

        HttpUtil.noCache(ex);
        String html = ResourceUtil.readText("templates/my-account.html");

        String errBlock = (errorMsg == null || errorMsg.isBlank())
                ? ""
                : "<p class=\"alert alert-warning\" role=\"alert\">" + escape(errorMsg) + "</p>";

        String okBlock = (successMsg == null || successMsg.isBlank())
                ? ""
                : "<p class=\"alert alert-success\" role=\"alert\">" + escape(successMsg) + "</p>";

        html = html.replace("<!-- ERROR_ACCOUNT -->", errBlock)
                .replace("<!-- SUCCESS_ACCOUNT -->", okBlock)
                .replace("{{name}}",  escape(s.name))
                .replace("{{email}}", escape(s.email));

        HttpUtil.sendHtml(ex, 200, html);
    }

    private void handlePost(HttpExchange ex) throws IOException {
        SessionManager.Session s = requireSessionOrRedirect(ex);
        if (s == null) return;

        try {
            Map<String,String> form = HttpUtil.readFormUrlEncoded(ex);
            String name = trim(form.get("name"));
            String email = trim(form.get("email"));
            String password = form.get("password"); // може да е null / ""

            // Валидации
            if (!Validation.isName(name))   { handleGet(ex, "Invalid name!", null); return; }
            if (!Validation.isEmail(email)) { handleGet(ex, "Invalid email!", null); return; }
            if (password != null && !password.isBlank() && !Validation.isStrongPassword(password)) {
                handleGet(ex, "Password must be at least 8 chars with lower/UPPER/digit!", null);
                return;
            }

            // Проверка за дублиран email (ако е сменен)
            if (!email.equalsIgnoreCase(s.email)) {
                Optional<Long> otherId = users.findIdByEmail(email);
                if (otherId.isPresent() && otherId.get() != s.userId) {
                    handleGet(ex, "This email is already registered!", null);
                    return;
                }
            }

            // Запис
            users.updateProfile(s.userId, name, email);
            if (password != null && !password.isBlank()) {
                byte[] salt = PasswordHasher.newSalt();
                byte[] hash = PasswordHasher.hash(password.toCharArray(), salt);
                users.updatePassword(s.userId, hash, salt);
            }

            // Обнови текущата сесия (консистентност)
            s.name = name;
            s.email = email;

            HttpUtil.redirectSeeOther(ex, "/my-account?ok=1");
        } catch (SQLException se) {
            se.printStackTrace();
            handleGet(ex, "Database error.", null);
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendHtml(ex, 500, "<h1>500 Internal Server Error</h1>");
        }
    }

    private SessionManager.Session requireSessionOrRedirect(HttpExchange ex) throws IOException {
        String cookieHeader = ex.getRequestHeaders().getFirst("Cookie");
        Map<String, String> cookies = Cookies.parse(cookieHeader);
        String sid = cookies.get("SESSIONID");
        SessionManager.Session s = SessionManager.get(sid);
        if (s == null) {
            HttpUtil.noCache(ex);
            HttpUtil.redirectSeeOther(ex, "/login");
            return null;
        }
        return s;
    }

    private static String trim(String s) { return s == null ? null : s.trim(); }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 8);
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
