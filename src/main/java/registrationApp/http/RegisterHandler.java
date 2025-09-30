package registrationApp.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import registrationApp.db.UsersDao;
import registrationApp.security.PasswordHasher;
import registrationApp.util.Validation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class RegisterHandler implements HttpHandler {
    private final UsersDao users = new UsersDao();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        switch (ex.getRequestMethod().toUpperCase()) {
            case "GET"  -> showForm(ex, null);
            case "POST" -> handlePost(ex);
            default     -> HttpUtil.sendHtml(ex, 405, "<h1>405 Method Not Allowed</h1>");
        }
    }

    private void showForm(HttpExchange ex, String errorMsg) throws IOException {
        String html = ResourceUtil.readText("templates/register.html");
        String errBlock = (errorMsg == null || errorMsg.isBlank())
                ? ""
                : "<p class=\"alert-warning\" style=\"color:#b91c1c;margin:.5rem 0\" role=\"alert\">"
                + escape(errorMsg) + "</p>";

        html = html.replace("<!-- ERROR_REGISTER -->", errBlock);
        HttpUtil.sendHtml(ex, 200, html);
    }

    private void handlePost(HttpExchange ex) throws IOException {
        try {
            Map<String, String> form = HttpUtil.readFormUrlEncoded(ex);
            String name = trim(form.get("name"));
            String email = trim(form.get("email"));
            String password = form.get("password");

            if (!Validation.isName(name))    { showForm(ex, "Invalid name!"); return; }
            if (!Validation.isEmail(email))  { showForm(ex, "Invalid email!"); return; }
            if (!Validation.isStrongPassword(password)) {
                showForm(ex, "Password must be at least 8 chars with lower/UPPER/digit!");
                return;
            }

            Optional<Long> existing = users.findIdByEmail(email);
            if (existing.isPresent()) { showForm(ex, "This email is already registered!"); return; }

            byte[] salt = PasswordHasher.newSalt();
            byte[] hash = PasswordHasher.hash(password.toCharArray(), salt);
            users.insert(name, email, hash, salt);

            HttpUtil.redirect(ex, "/login");
        } catch (SQLException se) {
            se.printStackTrace();
            showForm(ex, "Database error.");
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendHtml(ex, 500, "<h1>500 Internal Server Error</h1>");
        }
    }

    private static String trim(String s) { return s == null ? null : s.trim(); }

    private static String escape(String s) {
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
