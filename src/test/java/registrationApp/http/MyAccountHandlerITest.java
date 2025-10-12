package registrationApp.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import registrationApp.db.Db;
import registrationApp.db.UsersDao;
import registrationApp.security.PasswordHasher;
import registrationApp.security.SessionManager;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестът приема, че:
 *  - имаш Test/Http harness, което стартира HttpServer + Router (наследи подходящото – напр. HttpAndDbHarness).
 *  - в resources има templates/my-account.html с плейсхолдърите {{name}}, {{email}} и <!-- SUCCESS_ACCOUNT -->.
 * Ако нямаш HttpAndDbHarness, смени наследяването на твоя сървърен harness (напр. TestHttpServerHarness).
 */
public class MyAccountHandlerITest extends HttpAndDbHarness {

    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void cleanUsers() throws Exception {
        try (var c = Db.get(); var st = c.createStatement()) {
            st.executeUpdate("DELETE FROM users");              // или TRUNCATE ако имаш право
            st.executeUpdate("ALTER TABLE users AUTO_INCREMENT = 1");
        }
    }

    @Test
    void post_updatesProfileAndPassword_redirects_thenGetShowsSuccessAndNewValues() throws Exception {
        // seed: потребител + сесия
        var dao = new UsersDao();
        byte[] salt = PasswordHasher.newSalt();
        byte[] hash = PasswordHasher.hash("Aa123456".toCharArray(), salt);
        long id = dao.insert("Diyan", "d@ex.com", hash, salt);
        String sid = SessionManager.create(id, "Diyan", "d@ex.com");

        // изпращаме POST към /my-account с нови стойности
        String body = form(
                "name", "New Name",
                "email", "new@ex.com",
                "password", "Bb123456"
        );

        var post = HttpRequest.newBuilder(URI.create(url("/my-account")))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", "SESSIONID=" + sid)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var postRes = client.send(post, HttpResponse.BodyHandlers.discarding());
        assertEquals(303, postRes.statusCode());

        String location = postRes.headers().firstValue("Location").orElseThrow(); // "/my-account?ok=1"

        var getRes = client.send(
                HttpRequest.newBuilder(URI.create(url(location)))
                        .header("Cookie", "SESSIONID=" + sid) // важен е за достъп
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getRes.statusCode());
        String html = getRes.body();
        assertTrue(html.contains("Changes saved."), "Очаквах SUCCESS съобщение");
        assertTrue(html.contains("value=\"New Name\""));
        assertTrue(html.contains("value=\"new@ex.com\""));

        // проверка в DB: записан е новият email и паролата се верифицира
        var userOpt = dao.findByEmail("new@ex.com");
        assertTrue(userOpt.isPresent(), "User с нов имейл липсва");
        var u = userOpt.get();
        assertTrue(PasswordHasher.verify("Bb123456".toCharArray(), u.passSalt(), u.passHash()),
                "Новата парола не е записана коректно");
    }

    // ---- helpers ----
    private static String form(String... kv) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < kv.length; i += 2) {
            if (b.length() > 0) b.append('&');
            b.append(URLEncoder.encode(kv[i], StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(kv[i + 1], StandardCharsets.UTF_8));
        }
        return b.toString();
    }
}
